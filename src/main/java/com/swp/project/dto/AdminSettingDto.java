package com.swp.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdminSettingDto {
    @NotBlank
    private String shopName;

    @NotBlank
    private String shopAddress;

    @NotBlank
    private String shopPhone;

    @NotBlank
    @Email
    private String shopEmail;

    @NotBlank
    private String shopSlogan;

    @NotBlank
    @URL
    private String shopFacebook;

    @NotBlank
    @URL
    private String shopInstagram;
}
