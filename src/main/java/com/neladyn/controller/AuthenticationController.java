package com.neladyn.controller;

import com.neladyn.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class AuthenticationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    private AuthenticationService authenticationService;

    @Autowired
    private AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
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
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        httpServletResponse.addCookie(cookie);
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
}
