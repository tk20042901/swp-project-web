package com.swp.project.entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swp.project.dto.CreateProductUnitDto;
import com.swp.project.dto.UpdateProductUnitDto;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"products"})
@Entity
public class ProductUnit implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    @Column(nullable = false)
    private boolean isAllowDecimal;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @JsonIgnore
    @OneToMany(mappedBy = "unit", fetch = FetchType.EAGER)
    private List<Product> products;


    public ProductUnit(CreateProductUnitDto dto) {
        this.name = dto.getName();
        this.isAllowDecimal = dto.isAllowDecimal();
        this.isActive = dto.isActive();
    }

    public ProductUnit(UpdateProductUnitDto dto) {
        this.id = dto.getId();  
        this.name = dto.getName();
        this.isAllowDecimal = dto.isAllowDecimal();
        this.isActive = dto.isActive();
    }
}
