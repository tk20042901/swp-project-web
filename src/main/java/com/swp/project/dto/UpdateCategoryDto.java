package com.swp.project.dto;

import org.hibernate.validator.constraints.Length;

import com.swp.project.entity.product.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryDto {

    @NotNull(message = "ID danh mục không được để trống")
    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Length(max = 50, message = "Tên danh mục không được vượt quá 50 ký tự")
    private String name;

    private boolean isActive;

    public UpdateCategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.isActive = category.isActive();
    }
}
