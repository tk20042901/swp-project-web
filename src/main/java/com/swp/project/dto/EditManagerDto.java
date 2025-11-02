package com.swp.project.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.swp.project.entity.user.Manager;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EditManagerDto {
    public EditManagerDto(Manager manager){
        this.id = manager.getId();
        this.email = manager.getEmail();
        this.fullname = manager.getFullname();
        this.birthDate = manager.getBirthDate();
        this.cId = manager.getCid();
        this.provinceCityCode = manager.getCommuneWard().getProvinceCity().getCode();
        this.communeWardCode = manager.getCommuneWard().getCode();
        this.specificAddress = manager.getSpecificAddress();
        this.status = manager.isEnabled();
    }  
    
    private Long id;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

   @Pattern(
    regexp = "^$|^.{6,50}$",
    message = "Mật khẩu phải có độ dài từ 6 đến 50 ký tự")   
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    @Pattern(regexp = "^([\\p{L}\\p{N}.\\- ])+$", message = "Tên không hợp lệ") 
    private String fullname;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotBlank(message = "CMND/CCCD không được để trống")
    @Size(max = 50, message = "CMND/CCCD không được vượt quá 50 ký tự")
    private String cId;

    //Contain only code, name will be fetched in controller
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String provinceCityCode;

    //Contain only code, name will be fetched in controller
    @NotBlank(message = "Quận/Huyện không được để trống")
    private String communeWardCode;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    @Size(max = 100, message = "Địa chỉ chi tiết không được vượt quá 100 ký tự")
    @Pattern(regexp = "^([\\p{L}\\p{N}.\\- ])+$", message = "Địa chỉ chi tiết không hợp lệ")
    private String specificAddress;

    private Boolean status;
}