package com.swp.project.dto;

import org.hibernate.validator.constraints.Length;
import com.swp.project.entity.product.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCategoryDto {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Length(max = 50, message = "Tên danh mục không được vượt quá 50 ký tự")
    private String name;

    private boolean active;

    public CreateCategoryDto(Category category) {
        this.name = category.getName();
        this.active = category.isActive();
    }
}
