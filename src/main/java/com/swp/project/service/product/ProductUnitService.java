package com.swp.project.service.product;

import com.swp.project.dto.CreateProductUnitDto;
import com.swp.project.dto.UpdateProductUnitDto;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductUnit;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.product.ProductUnitRepository;
import com.swp.project.repository.seller_request.SellerRequestRepository;
import com.swp.project.repository.seller_request.SellerRequestTypeRepository;
import com.swp.project.service.seller_request.SellerRequestService;
import com.swp.project.service.seller_request.SellerRequestStatusService;
import com.swp.project.service.seller_request.SellerRequestTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
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
    private final SellerRequestService sellerRequestService;
    private final ProductService productService;
    private final SellerRequestStatusService sellerRequestStatusService;

    public ProductUnit getProductUnitById(Long id) {
        return productUnitRepository.findById(id).orElse(null);
    }

    public List<ProductUnit> getAllUnits() {
        return productUnitRepository.findAll();
    }

    public List<ProductUnit> getAllActiveProductUnits() {
        return productUnitRepository.findByIsActiveTrue();
    }

    public void add(ProductUnit productUnit) {
        productUnitRepository.save(productUnit);
    }

    public void update(ProductUnit productUnit) {
        ProductUnit savedProductUnit = productUnitRepository.save(productUnit);
        for (Product product : savedProductUnit.getProducts()) {
            eventPublisher.publishEvent(new ProductRelatedUpdateEvent(product));
        }
    }

    public List<ProductUnit> getAllProductUnit() {
        return productUnitRepository.findAll();
    }

    public List<ProductUnit> getUnitsByAllowDecimal(boolean allowDecimal) {
        return productUnitRepository.findByIsAllowDecimal(allowDecimal);
    }

    public void delete(ProductUnit productUnit) {
        productUnitRepository.delete(productUnit);
    }

    public Page<ProductUnit> getAllProductUnit(Pageable pageable) {
        return productUnitRepository.findAll(pageable);
    }

    public boolean nameAlreadyExistsForCreate(String name) {
        return productUnitRepository.existsByName(name);
    }
    public boolean nameAlreadyExistsInSellerRequestForCreate(String name) throws Exception{
        boolean existed = false;
        for (var sellerRequest : sellerRequestService.getSellerRequestByEntityName(ProductUnit.class)) {
            ProductUnit pu = sellerRequestService.getEntityFromContent(sellerRequest.getContent(), ProductUnit.class);
            if (sellerRequestStatusService.isPendingStatus(sellerRequest) && pu.getName().equals(name)) {
                existed = true;
                break;
            }
        }
        return existed;
    }

    public boolean nameAlreadyExistsForUpdate(Long id,String name) {
        return productUnitRepository.findAll().stream()
                .anyMatch(p -> p.getName().equals(name) && p.getId() != id);
    }

    public void createNewProductUnit(CreateProductUnitDto productUnitDto, Principal principal) throws Exception {
        ProductUnit productUnit = new ProductUnit(productUnitDto);
        if(nameAlreadyExistsForCreate(productUnit.getName())){
            throw new Exception("Đơn vị sản phẩm đã tồn tại");
        }
        if(nameAlreadyExistsInSellerRequestForCreate(productUnit.getName())){
            throw new Exception("Đơn vị sản phẩm đã tồn tại trong yêu cầu của người bán");
        }
        sellerRequestService.saveAddRequest(productUnit, principal.getName());
    }

    public void updateProductUnit(UpdateProductUnitDto updateProductUnitDto, Principal principal) throws Exception {
        ProductUnit oldProductUnit = getProductUnitById(updateProductUnitDto.getId());
        if (oldProductUnit == null) {
            throw new Exception("Không tìm thấy đơn vị sản phẩm");
        }
        Product p = productService.getAllProducts().stream()
                .filter(x -> x.getUnit().getName().equals(updateProductUnitDto.getName())).findFirst().orElse(null);
        if (!updateProductUnitDto.isActive() && p != null) {
            throw new Exception("Đơn vị đã được dùng cho sản phẩm " + p.getName());
        }
        if(nameAlreadyExistsForUpdate(updateProductUnitDto.getId(),updateProductUnitDto.getName())){
            throw new Exception("Đơn vị sản phẩm đã tồn tại");
        }
        if(nameAlreadyExistsInSellerRequestForCreate(updateProductUnitDto.getName())){
            throw new Exception("Đơn vị sản phẩm đã tồn tại trong yêu cầu của người bán");
        }
        ProductUnit newProductUnit = new ProductUnit(updateProductUnitDto);
        sellerRequestService.saveUpdateRequest(oldProductUnit, newProductUnit, principal.getName());
    }
}
