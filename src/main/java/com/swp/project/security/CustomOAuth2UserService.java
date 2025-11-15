package com.swp.project.security;

import com.swp.project.entity.user.Customer;
import com.swp.project.service.user.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final CustomerService customerService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        String email = super.loadUser(request).getAttribute("email");
        Customer customer = customerService.getCustomerByEmail(email);
        if(customer == null) {
            customer = customerService.registerWithGoogle(email);
        } else if (!customer.isEnabled()){
            throw new OAuth2AuthenticationException("account_disabled");
        }
        return customer;
    }

}
