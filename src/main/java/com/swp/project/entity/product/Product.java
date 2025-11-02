package com.swp.project.entity.product;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.swp.project.dto.UpdateProductDto;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.Formula;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = {"soldQuantity", "quantity"})
@Entity
public class Product implements Serializable{

    public Product(UpdateProductDto dto){
        this.id = dto.getId();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.price = dto.getPrice();
        this.unit = dto.getUnit();
        this.main_image_url = dto.getMainImage();
        this.enabled = dto.getEnabled();
        this.quantity = dto.getQuantity();
        this.categories = dto.getFinalCategories();
        
    }    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id")
    private ProductUnit unit;

    @Column(nullable = false)
    private String main_image_url;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<SubImage> sub_images;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Category> categories;

    @Column(nullable = false)
    private double quantity;

    // Formula tính sold quantity trong DB - đang giao hàng,đã giao hàng và đang chuẩn bị hàng
    @Formula("(SELECT COALESCE(SUM(oi.quantity), 0) " +
            "FROM order_item oi " +
            "INNER JOIN orders o ON o.id = oi.order_id " +
            "INNER JOIN order_status os ON o.order_status_id = os.id " +
            "WHERE oi.product_id = id AND os.name IN ('Đã Giao Hàng','Đang Giao Hàng','Đang Chuẩn Bị Hàng'))")
    private Integer soldQuantity;

    // Formula tính available quantity trong DB - số lượng còn lại sau khi trừ đi số lượng đang chờ thanh toán
    @Formula("(quantity - COALESCE((SELECT SUM(oi.quantity) " +
            "FROM order_item oi " +
            "INNER JOIN orders o ON o.id = oi.order_id " +
            "INNER JOIN order_status os ON o.order_status_id = os.id " +
            "WHERE oi.product_id = id AND os.name = 'Chờ Thanh Toán'), 0))")
    private Double availableQuantity;

}
