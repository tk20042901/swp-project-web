package com.swp.project.repository.product;

import com.swp.project.entity.product.ProductUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit,Long> {

    List<ProductUnit> findByIsAllowDecimal(boolean allowDecimal);
    Page<ProductUnit> findAll(Pageable pageable);
    List<ProductUnit> findByIsActiveTrue();
}
