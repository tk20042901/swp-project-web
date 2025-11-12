package com.swp.project.filter;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public record CaptchaValidationFilter(String recaptchaSecret) implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            if (!verifyCaptcha(req.getParameter("g-recaptcha-response"))) {
                res.sendRedirect(req.getServletPath() + "?invalid_captcha");
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean verifyCaptcha(String recaptchaResponse) {
        if (recaptchaResponse == null) {
            return false;
        }
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", recaptchaResponse);
        var body = new RestTemplate().postForObject("https://www.google.com/recaptcha/api/siteverify", params,
                Map.class);
        return body != null && body.get("success") != null && (boolean)body.get("success");
    }
}
