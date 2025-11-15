package com.swp.project.dto;

import java.time.LocalDate;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateManagerDto {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu phải có độ dài từ 6 đến 50 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    @Pattern(regexp = "^([\\p{L}\\p{N}.\\- ])+$", message = "Tên không hợp lệ")
    private String fullname;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate birthDate;

    @NotBlank(message = "CMND/CCCD không được để trống")
    @Size(max = 50, message = "CMND/CCCD không được vượt quá 50 ký tự")
    private String cId;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    @Size(max = 100, message = "Địa chỉ chi tiết không được vượt quá 100 ký tự")
    @Pattern(regexp = "^([\\p{L}\\p{N}.\\- ])+$", message = "Địa chỉ chi tiết không hợp lệ")
    private String specificAddress;
}