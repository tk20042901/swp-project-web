package com.swp.project.dto;

import java.util.List;

import com.swp.project.entity.product.Product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ViewProductRequestDetailDto {

    public ViewProductRequestDetailDto(Product oldProduct, Product newProduct) {
        newProduct.setCategories(
                newProduct.getCategories().stream().sorted((c1, c2) -> c1.getId().compareTo(c2.getId())).toList());
        this.newName = newProduct.getName();
        this.newDescription = newProduct.getDescription();
        this.newPrice = String.valueOf(newProduct.getPrice());
        this.newProductUnit = newProduct.getUnit().getName();
        this.newMainImageUrl = newProduct.getMain_image_url();
        this.newEnabled = String.valueOf(newProduct.isEnabled());
        this.newFirstSubImage = newProduct.getSub_images().get(0).getSub_image_url();
        this.newSecondSubImage = newProduct.getSub_images().get(1).getSub_image_url();
        this.newThirdSubImage = newProduct.getSub_images().get(2).getSub_image_url();
        this.newCategories = newProduct.getCategories().stream().map(c -> c.getName()).toList();
        this.newQuantity = String.valueOf(newProduct.getQuantity());
        if (oldProduct == null) {
            return;
        }
        oldProduct.setCategories(
                oldProduct.getCategories().stream().sorted((c1, c2) -> c1.getId().compareTo(c2.getId())).toList());
        if (!oldProduct.getName().equals(newProduct.getName())) {
            oldName = oldProduct.getName();
        }
        if (!oldProduct.getDescription().equals(newProduct.getDescription())) {
            oldDescription = oldProduct.getDescription();
        }
        if (oldProduct.getPrice().equals(newProduct.getPrice())) {
            oldPrice = String.valueOf(oldProduct.getPrice());
        }
        if (!oldProduct.getUnit().getName().equals(newProduct.getUnit().getName())) {
            oldProductUnit = oldProduct.getUnit().getName();
        }
        if (!oldProduct.getMain_image_url().equals(newProduct.getMain_image_url())) {
            oldMainImageUrl = oldProduct.getMain_image_url();
        }
        if (oldProduct.isEnabled() != newProduct.isEnabled()) {
            oldEnabled = String.valueOf(oldProduct.isEnabled());
        }
        if (!oldProduct.getSub_images().get(0).getSub_image_url()
                .equals(newProduct.getSub_images().get(0).getSub_image_url())) {
            oldFirstSubImage = oldProduct.getSub_images().get(0).getSub_image_url();
        }
        if (!oldProduct.getSub_images().get(0).getSub_image_url()
                .equals(newProduct.getSub_images().get(0).getSub_image_url())) {
            oldFirstSubImage = oldProduct.getSub_images().get(0).getSub_image_url();
        }
        if (!oldProduct.getSub_images().get(1).getSub_image_url()
                .equals(newProduct.getSub_images().get(1).getSub_image_url())) {
            oldSecondSubImage = oldProduct.getSub_images().get(1).getSub_image_url();
        }
        if (!oldProduct.getSub_images().get(2).getSub_image_url()
                .equals(newProduct.getSub_images().get(2).getSub_image_url())) {
            oldThirdSubImage = oldProduct.getSub_images().get(2).getSub_image_url();
        }
        if (newProduct.getCategories().size() != oldProduct.getCategories().size() ||
                !newProduct.getCategories().stream().map(c -> c.getName()).sorted().toList()
                        .equals(oldProduct.getCategories().stream().map(c -> c.getName()).sorted().toList())) {
            oldCategories = oldProduct.getCategories().stream().map(c -> c.getName()).toList();
        }
        if (oldProduct.getQuantity() != newProduct.getQuantity()) {
            oldQuantity = String.valueOf(oldProduct.getQuantity());
        }
    }

    private String oldName;
    private String oldDescription;
    private String oldPrice;
    private String oldProductUnit;
    private String oldMainImageUrl;
    private String oldEnabled;
    private String oldFirstSubImage;
    private String oldSecondSubImage;
    private String oldThirdSubImage;
    private List<String> oldCategories;
    private String oldQuantity;

    private String newName;
    private String newDescription;
    private String newPrice;
    private String newProductUnit;
    private String newMainImageUrl;
    private String newEnabled;
    private String newFirstSubImage;
    private String newSecondSubImage;
    private String newThirdSubImage;
    private List<String> newCategories;
    private String newQuantity;
}
