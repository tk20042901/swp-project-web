package com.swp.project.service.product;

import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductUnit;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.product.ProductUnitRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductUnitService {

    private final ProductUnitRepository productUnitRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductUnit getProductUnitById(Long id){
        return productUnitRepository.findById(id).orElse(null);
    }
    public List<ProductUnit> getAllUnits(){
        return productUnitRepository.findAll();
    }

    public void add(ProductUnit productUnit) {
        productUnitRepository.save(productUnit);
    }

    public void update(ProductUnit productUnit) {
        ProductUnit savedProductUnit = productUnitRepository.save(productUnit);
        for(Product product : savedProductUnit.getProducts()) {
             eventPublisher.publishEvent(new ProductRelatedUpdateEvent(product));
        }
    }

    public List<ProductUnit> getAllProductUnit(){
        return productUnitRepository.findAll();
    }

    public List<ProductUnit> getUnitsByAllowDecimal(boolean allowDecimal){
        return productUnitRepository.findByIsAllowDecimal(allowDecimal);
    }

    public void delete(ProductUnit productUnit) {
        productUnitRepository.delete(productUnit);
    }

    public Page<ProductUnit> getAllProductUnit(Pageable pageable){
        return productUnitRepository.findAll(pageable);
    }
}
