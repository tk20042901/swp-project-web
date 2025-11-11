package com.swp.project.dto;

import java.util.List;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.ProductUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductDto {

    private Long id;
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Length(max = 50, message = "Tên sản phẩm không được vượt quá 50 ký tự")
    private String name;

    @NotBlank(message = "Tên chú thích không được để trống")
    @Length(max = 250, message = "Không được vượt quá 250 ký tự")
    private String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @PositiveOrZero(message = "Giá sản phẩm phải là số nguyên dương hoặc bằng 0")
    private Long price;

    @NotNull(message = "Đơn vị sản phẩm không được để trống")
    private ProductUnit unit;

    private boolean enabled;

    @PositiveOrZero(message = "Số lượng sản phẩm phải là số nguyên dương hoặc bằng 0")
    private double quantity;

    @NotNull(message = "Hình ảnh chính sản phẩm không được để trống")
    private MultipartFile image;
    private List<MultipartFile> subImages;
    private List<Category> categories;
    private List<Long> categoryIds;
    private double heldQuantity;

}
