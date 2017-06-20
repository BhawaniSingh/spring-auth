package com.neladyn.controller;

import com.neladyn.domain.UserDetails;
import com.neladyn.service.AuthenticationService;
import com.neladyn.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class AuthenticationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    private AuthenticationService authenticationService;
    private UserService userService;

    private String REDIRECT_URI;

    @Autowired
    private AuthenticationController(
            AuthenticationService authenticationService,
            UserService userService,
            @Value("${REDIRECT_URI}") String REDIRECT_URI) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.REDIRECT_URI = REDIRECT_URI;
    }

    @GetMapping("/api/login")
    public ResponseEntity<?> facebookLogin(
            @RequestParam("code") String code,
            @RequestParam("state") String state, HttpServletResponse httpServletResponse) throws IOException {
        return authenticationService.login(code, state, httpServletResponse);
    }

    @GetMapping("/api/auth")
    public boolean isAuthenticated(@CookieValue(value = "access_token", required = false) String access_token) {
        return access_token != null && authenticationService.userIsAuthenticated(access_token);
    }

    @GetMapping("/api/logout")
    public ResponseEntity logout(HttpServletResponse httpServletResponse) {
        httpServletResponse.addCookie(getRemoveAccessTokenCookie());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/unregister")
    public ResponseEntity unregister(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Cookie access_token = WebUtils.getCookie(httpServletRequest, "access_token");
        if (access_token == null) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        UserDetails userDetails = authenticationService.getUserDetailsFromAccessToken(access_token.getValue());
        userService.deleteUser(userDetails.getEmail());
        httpServletResponse.addCookie(getRemoveAccessTokenCookie());
        httpServletResponse.sendRedirect(REDIRECT_URI);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/getLoginUri")
    public String getLoginUri() {
        return authenticationService.getLoginUrl();
    }

    @GetMapping("/api/test")
    public ResponseEntity<?> getTest() {
        LOGGER.info("Get test");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/admin/user")
    public ResponseEntity<?> getAdminResource() {
        LOGGER.info("Get admin resource");
        return ResponseEntity.ok().build();
    }

    private Cookie getRemoveAccessTokenCookie() {
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        return cookie;
    }
}
