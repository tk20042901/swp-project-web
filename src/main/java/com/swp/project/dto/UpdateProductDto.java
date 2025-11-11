package com.swp.project.dto;

import java.util.List;

import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProductDto{
    
    public UpdateProductDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.unit = product.getUnit();  
        this.enabled = product.isEnabled();
        this.categories = product.getCategories().stream().map(Category::getId).toList();
        this.mainImage = product.getMain_image_url();
        this.subDisplay1 = product.getSub_images().get(0).getSub_image_url();
        this.subDisplay2 = product.getSub_images().get(1).getSub_image_url();
        this.subDisplay3 = product.getSub_images().get(2).getSub_image_url();
        this.quantity = product.getQuantity();
        this.heldQuantity = product.getHeldQuantity();
    }

    private Long id;
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 50, message = "Tên sản phẩm không được vượt quá 50 ký tự")
    private String name;
    @NotBlank(message = "Mô tả sản phẩm không được để trống")
    @Size(max = 255, message = "Mô tả sản phẩm không được vượt quá 255 ký tự")
    private String description;

    @Min(value = 0, message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    private long price;

    @NotNull(message = "Đơn vị sản phẩm không được để trống")
    private ProductUnit unit;

    private boolean enabled;

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private List<Long> categories;
    private double quantity;
    private String subDisplay1;
    private String subDisplay2;
    private String subDisplay3;
    private String mainImage; 
    private List<Category> finalCategories;
    private double heldQuantity;
}
