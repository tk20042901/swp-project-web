package com.swp.project.service.product;

import com.swp.project.dto.UpdateCategoryDto;
import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.seller_request.SellerRequest;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.product.CategoryRepository;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.service.seller_request.SellerRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SellerRequestService sellerRequestService;
    private final ProductRepository productRepository;
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue(Sort.by("name"));
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category getReferenceById(Long id) {
        return categoryRepository.getReferenceById(id);
    }

    public void add(Category category) {
        categoryRepository.save(category);
    }

    public void update(Category category) {
        Category savedCategory = categoryRepository.save(category);
        for (Product product : savedCategory.getProducts()) {
            eventPublisher.publishEvent(new ProductRelatedUpdateEvent(product));
        }
    }

    public void delete(Category category) {
        categoryRepository.delete(category);
    }

    public List<Category> getUniqueCategoriesBaseOnPageOfProduct(List<ViewProductDto> content) {
        List<Long> ids = content.stream().map(ViewProductDto::getId).toList();
        return categoryRepository.findDistinctCategoriesByProductIds(ids);
    }

    public Page<Category> searchByCategoryName(String categoryName, int size, int page) {
        Pageable pageable = PageRequest.of(page, size);
        boolean hasCategoryName = categoryName != null && !categoryName.trim().isEmpty();
        if (hasCategoryName) {
            return categoryRepository.findByCategoryName(categoryName, pageable);
        } else {
            return categoryRepository.findAll(pageable);
        }
    }

    public void updateCategory(UpdateCategoryDto updateCategoryDto, Principal principal) throws Exception {
        Category oldCategory = getCategoryById(updateCategoryDto.getId());
        if (oldCategory == null) {
            throw new Exception("Không tìm thấy danh mục");
        } 
        Product p = productRepository.findAll().stream().filter(x -> x.getCategories().stream().anyMatch(c -> c.getName().equals(updateCategoryDto.getName()))).findFirst().orElse(null);
        if(!updateCategoryDto.isActive() && p != null){
            throw new Exception("Danh mục đã được sử dụng bởi sản phẩm " + p.getName());
        }
        Category newCategory = new Category(updateCategoryDto);
        sellerRequestService.saveUpdateRequest(oldCategory, newCategory, principal.getName());
    }
}
