package com.swp.project.config;

import com.swp.project.filter.LoginRequestValidationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final LoginRequestValidationFilter loginRequestValidationFilter;

    private static final String HOME_URL = "/";//nên thêm trang error

    private static final String[] ALL_ALLOWED = {
            "/",                    // trang chủ
            "/search-product/**",   // tìm kiếm sản phẩm
            "/product/**",          // chi tiết sản phẩm
            "/login/**",            // đăng nhập
            "/register/**",         // đăng ký
            "/verify-otp/**",       // xác thực otp
            "/css/**",              // css tĩnh
            "/js/**",               // js tĩnh
            "/images/**",           // hình ảnh tĩnh
            "/forgot-password/**",  // quên mật khẩu
            "/webhook/**",          // webhook
            "/ai/**" ,              // chatbot ai
            "/product-category-display/**" ,// danh mục sản phẩm
            "/product-category-sorting/**" ,  // sắp xếp sản phẩm theo danh mục
            "/api/categories/**",   // api danh mục sản phẩm
            "/api/products/**"  // api sản phẩm
    };

    private static final String[] ADMIN_ALLOWED = {
            "/admin/**"
    };

    private static final String[] MANAGER_ALLOWED = {
            "/manager/**"
    };

    private static final String[] SELLER_ALLOWED = {
            "/seller/**"
    };

    private static final String[] SHIPPER_ALLOWED = {
            "/shipper/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(i -> i
                        .usernameParameter("email")
                        .failureHandler(loginFailureHandler())
                        .defaultSuccessUrl(HOME_URL, true)
                )
                .oauth2Login(i -> i
                        .failureHandler(oauth2FailureHandler())
                        .defaultSuccessUrl(HOME_URL, true)
                )
                .addFilterBefore(loginRequestValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(i -> i
                        .requestMatchers(ALL_ALLOWED).permitAll()
                        .requestMatchers(ADMIN_ALLOWED).hasAuthority("Admin")
                        .requestMatchers(MANAGER_ALLOWED).hasAuthority("Manager")
                        .requestMatchers(SELLER_ALLOWED).hasAuthority("Seller")
                        .requestMatchers(SHIPPER_ALLOWED).hasAuthority("Shipper")
                        .anyRequest().authenticated()
                ).csrf(csrf -> csrf
                        .ignoringRequestMatchers("/webhook")
                )
                .exceptionHandling(i -> i
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> response.sendRedirect(HOME_URL))
                );

        return http.build();
    }

    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof BadCredentialsException
                    || exception instanceof UsernameNotFoundException) {
                response.sendRedirect("/login?incorrect_email_or_password");
            } else if (exception instanceof DisabledException) {
                response.sendRedirect("/login?account_disabled");
            } else {
                response.sendRedirect("/login?unknown_error");
            }
        };
    }

    public AuthenticationFailureHandler oauth2FailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof OAuth2AuthenticationException authEx) {
                String error = authEx.getError().getErrorCode();
                if (error.equals("account_disabled")) {
                    response.sendRedirect("/login?account_disabled");
                } else {
                    response.sendRedirect("/login?unknown_error");
                }
            } else {
                response.sendRedirect("/login?unknown_error");
            }
        };
    }
}
