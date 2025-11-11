package com.swp.project.service;

import com.swp.project.dto.AdminSettingDto;
import com.swp.project.entity.Setting;
import com.swp.project.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class SettingService {

    private final SettingRepository settingRepository;

    public String getShopName() {
        return Objects.requireNonNull(settingRepository.findById("shop_name").orElse(null)).getValue();
    }

    public String getShopAddress() {
        return Objects.requireNonNull(settingRepository.findById("shop_address").orElse(null)).getValue();
    }

    public String getShopPhone() {
        return Objects.requireNonNull(settingRepository.findById("shop_phone").orElse(null)).getValue();
    }

    public String getShopEmail() {
        return Objects.requireNonNull(settingRepository.findById("shop_email").orElse(null)).getValue();
    }
    public String getShopSlogan(){
        return Objects.requireNonNull(settingRepository.findById("shop_slogan").orElse(null)).getValue();
    }

    public String getShopFacebook(){
        return Objects.requireNonNull(settingRepository.findById("shop_facebook").orElse(null)).getValue();
    }
    public String getShopInstagram(){
        return Objects.requireNonNull(settingRepository.findById("shop_instagram").orElse(null)).getValue();
    }
    public void updateSetting(AdminSettingDto ad){
        settingRepository.save(new Setting("shop_name", ad.getShopName()));
        settingRepository.save(new Setting("shop_address", ad.getShopAddress()));
        settingRepository.save(new Setting("shop_phone", ad.getShopPhone()));
        settingRepository.save(new Setting("shop_email", ad.getShopEmail()));
        settingRepository.save(new Setting("shop_slogan", ad.getShopSlogan()));
        settingRepository.save(new Setting("shop_facebook", ad.getShopFacebook()));
        settingRepository.save(new Setting("shop_instagram", ad.getShopInstagram()));
    }
}