package com.neladyn.service;

import com.neladyn.domain.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final String REDIRECT_URI;
    private final String APP_ID;
    private final String APP_SECRET;

    private RestTemplate restTemplate;
    private String cachedAppAccessToken;

    private RedisService redisService;
    private UserService userService;

    @Autowired
    public AuthenticationService(
            @Value("${REDIRECT_URI}") String REDIRECT_URI,
            @Value("${APP_ID}") String APP_ID,
            @Value("${APP_SECRET}") String APP_SECRET,
            RedisService redisService,
            UserService userService) {
        this.REDIRECT_URI = REDIRECT_URI;
        this.APP_ID = APP_ID;
        this.APP_SECRET = APP_SECRET;

        this.redisService = redisService;
        this.userService = userService;

        restTemplate = new RestTemplate();
    }

    public ResponseEntity<?> login(String code, String state, HttpServletResponse httpServletResponse) throws IOException {
        // Optional: Verify state (csrf) token

        AccessToken accessToken;
        try {
            accessToken = getAccessTokenFromCode(code);
        } catch (RuntimeException e) {
            return ResponseEntity.status(Integer.parseInt(e.getMessage())).build();
        }

        LOGGER.info("Access token = {}", accessToken);

        String appAccessToken;
        try {
            appAccessToken = getCachedAppAccessToken();
        } catch (RuntimeException e) {
            return ResponseEntity.status(Integer.parseInt(e.getMessage())).build();
        }

        AccessTokenData accessTokenData = inspectAccessToken(accessToken.getAccess_token(), appAccessToken);
        LOGGER.info("Verify token = {}", accessTokenData);
        if (!accessTokenData.isIs_valid() || accessTokenData.getApp_id() != Long.valueOf(APP_ID)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails;
        try {
            userDetails = getUserDetailsFromAccessToken(accessToken.getAccess_token());
        } catch (RuntimeException e) {
            return ResponseEntity.status(Integer.parseInt(e.getMessage())).build();
        }

        LOGGER.info("User is authenticated: {}", userDetails);

        Cookie cookie = new Cookie("access_token", accessToken.getAccess_token());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge((int) accessToken.getExpires_in());
        httpServletResponse.addCookie(cookie);
        httpServletResponse.sendRedirect(REDIRECT_URI);

        // Check if email is in UserDB. If not, create user.
        User user = userService.getUser(userDetails.getEmail());
        if (user == null) {
            LOGGER.info("Creating user {}", user);
            userService.createUser(userDetails);
        } else if (!user.isEnabled()) {
            // User exist in db but is not enabled
            LOGGER.info("User is disabled {}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        redisService.setValue(accessToken.getAccess_token(), userDetails.getEmail(), (int) accessToken.getExpires_in());
        return ResponseEntity.ok().build();
    }

    private String genCSRF() {
        return UUID.randomUUID().toString();
    }

    public boolean userIsAuthenticated(String access_token) {
        AccessTokenData accessTokenData;
        try {
            accessTokenData = inspectAccessToken(access_token, getCachedAppAccessToken());
        } catch (RuntimeException e) {
            LOGGER.warn(e.getMessage());
            return false;
        }

        return !(!accessTokenData.isIs_valid() || accessTokenData.getApp_id() != Long.valueOf(APP_ID));
    }

    public boolean userIsAuthenticatedRedis(String access_token) {
        LOGGER.info("Checking {}", access_token);
        String userid = redisService.getValue(access_token);
        if (userid == null) {
            LOGGER.info("Either redis cached access token is not valid or access token is in fact not valid. Check with fb");
            boolean isAuthenticated = userIsAuthenticated(access_token);
            LOGGER.info("{}", isAuthenticated);

            if (isAuthenticated) {
                UserDetails userDetails = getUserDetailsFromAccessToken(access_token);
                redisService.setValue(access_token, userDetails.getEmail(), 5000000);
                LOGGER.info("Setting new access token for {} : {}", userDetails, access_token);
                return true;
            }
            return false;

        }
        return true;
    }

    public AccessTokenData inspectAccessToken(String accessToken, String appAccessToken) {
        Map<String, String> urlparams = new HashMap<>();
        urlparams.put("input_token", accessToken);
        urlparams.put("access_token", appAccessToken);
        try {
            return restTemplate.getForObject("https://graph.facebook.com/debug_token?input_token={input_token}&access_token={access_token}", Data.class, urlparams).getData();
        } catch (HttpStatusCodeException exception) {
            LOGGER.warn(exception.getResponseBodyAsString());
            throw new RuntimeException(String.valueOf(exception.getStatusCode()));
        }
    }

    public AccessToken getAccessTokenFromCode(String code) {
        Map<String, String> urlparams = new HashMap<>();
        urlparams.put("client_id", APP_ID);
        urlparams.put("redirect_uri", "https://localhost:8445/index.html/");
        urlparams.put("client_secret", APP_SECRET);
        urlparams.put("code", code);

        try {
            return restTemplate.getForObject("https://graph.facebook.com/oauth/access_token?client_id={client_id}&code={code}&client_secret={client_secret}&redirect_uri={redirect_uri}", AccessToken.class, urlparams);
        } catch (HttpStatusCodeException exception) {
            LOGGER.warn(exception.getResponseBodyAsString());
            throw new RuntimeException(String.valueOf(exception.getStatusCode()));
        }
    }

    public UserDetails getUserDetailsFromAccessToken(String accessToken) {

        Map<String, String> urlparams = new HashMap<>();
        urlparams.put("access_token", accessToken);
        urlparams.put("fields", "id,name,email");
        LOGGER.info("Retrieving user details with {} and {}", accessToken, urlparams);
        try {
            return restTemplate.getForObject("https://graph.facebook.com/v2.9/me/?access_token={access_token}&fields={fields}", UserDetails.class, urlparams);
        } catch (HttpStatusCodeException exception) {
            LOGGER.warn(exception.getResponseBodyAsString());
            throw new RuntimeException(String.valueOf(exception.getStatusCode()));
        }
    }

    public String getCachedAppAccessToken() {
        if (cachedAppAccessToken == null) {
            cachedAppAccessToken = getAppAccessToken();
        }
        return cachedAppAccessToken;
    }

    public String getAppAccessToken() {
        Map<String, String> urlparams = new HashMap<>();
        urlparams.put("client_id", APP_ID);
        urlparams.put("client_secret", APP_SECRET);
        LOGGER.info("Retrieving app access token");

        try {
            String json = restTemplate.getForObject("https://graph.facebook.com/oauth/access_token?client_id={client_id}&client_secret={client_secret}&grant_type=client_credentials", String.class, urlparams);

            return new JSONObject(json).getString("access_token");
        } catch (HttpStatusCodeException exception) {
            LOGGER.warn(exception.getResponseBodyAsString());
            throw new RuntimeException(String.valueOf(exception.getStatusCode()));
        }
    }

    public String getLoginUrl() {
        return "https://www.facebook.com/v2.9/dialog/oauth?client_id=" + APP_ID + "&redirect_uri=" + REDIRECT_URI + "&state=" + genCSRF();
    }
}
