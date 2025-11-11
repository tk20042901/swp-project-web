package com.swp.project.controller;

import com.swp.project.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@RequiredArgsConstructor
@ControllerAdvice
public class GlobalModelAttributes {
    private final SettingService settingService;

    @ModelAttribute("shopName")
    public String shopName() {
        return settingService.getShopName();
    }
    @ModelAttribute("shopEmail")
    public String shopEmail() {
        return settingService.getShopEmail();
    }
    @ModelAttribute("shopAddress")
    public String shopAddress() {
        return settingService.getShopAddress();
    }
    @ModelAttribute("shopPhone")
    public String shopPhone() {
        return settingService.getShopPhone();
    }
    @ModelAttribute("shopSlogan")
    public String shopSlogan() {
        return settingService.getShopSlogan();
    }
    @ModelAttribute("shopFacebook")
    public String shopFacebook() {
        return settingService.getShopFacebook();
    }
    @ModelAttribute("shopInstagram")
    public String shopInstagram() {
        return settingService.getShopInstagram();
    }

}
