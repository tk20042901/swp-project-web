package com.swp.project.dto;

import java.io.Serializable;
import java.util.Date;

import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id = 0L;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    // @NotBlank(message = "Mật khẩu không được để trống")
    // @Size(min = 6, max = 50, message = "Mật khẩu phải có độ dài từ 6 đến 50 ký tự")
    private String password;

    @NotBlank(message = "Tên không được để trống")
    private String fullname;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Ngày tháng năm sinh không được để trống")
    @Past(message = "Ngày tháng năm sinh là quá khứ")
    private Date birthDate;

    @NotBlank(message = "Mã căn cước công dân không được để trống")
    @Pattern(regexp = "\\d{9}|\\d{12}", message = "Mã căn cước công dân phải gồm 9 hoặc 12 chữ số")
    private String cid;

    private String provinceCity;

    @NotBlank(message = "Phường / xã không để trống")
    private String communeWard;

    @NotBlank(message = "Địa chỉ cụ thể không để trống")
    private String specificAddress;

    private boolean enabled = true;

    public StaffDto parse(Seller seller) {
        return StaffDto.builder()
                .id(seller.getId())
                .email(seller.getEmail())
                .password("")
                .fullname(seller.getFullname())
                .birthDate(seller.getBirthDate())
                .cid(seller.getCid())
                .provinceCity(seller.getCommuneWard().getProvinceCity().getCode())
                .communeWard(seller.getCommuneWard().getCode())
                .specificAddress(seller.getSpecificAddress())
                .enabled(seller.isEnabled())
                .build();
    }

    public StaffDto parse(Shipper shipper) {
        return StaffDto.builder()
                .id(shipper.getId())
                .email(shipper.getEmail())
                .password("")
                .fullname(shipper.getFullname())
                .birthDate(shipper.getBirthDate())
                .cid(shipper.getCid())
                .provinceCity(shipper.getCommuneWard().getProvinceCity().getCode())
                .communeWard(shipper.getCommuneWard().getCode())
                .specificAddress(shipper.getSpecificAddress())
                .enabled(shipper.isEnabled())
                .build();
    }
}
