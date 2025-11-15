package com.swp.project.service.product;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.swp.project.dto.CreateProductDto;
import com.swp.project.dto.UpdateProductDto;
import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.order.OrderItem;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.SubImage;
import com.swp.project.entity.seller_request.SellerRequest;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.listener.event.ProductRelatedUpdateEvent;
import com.swp.project.repository.order.OrderRepository;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.repository.shopping_cart.ShoppingCartItemRepository;
import com.swp.project.service.order.OrderStatusService;
import com.swp.project.service.seller_request.SellerRequestService;
import com.swp.project.service.seller_request.SellerRequestStatusService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderStatusService orderStatusService;
    private final OrderRepository orderRepository;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ImageService imageService;
    private final CategoryService categoryService;
    private final SellerRequestService sellerRequestService;
    private final SellerRequestStatusService sellerRequestStatusService;
    private static final Map<String, Sort> SORT_OPTIONS = Map.of(
            "price-asc", Sort.by("price").ascending(),
            "price-desc", Sort.by("price").descending(),
            "name-asc", Sort.by("name").ascending(),
            "name-desc", Sort.by("name").descending(),
            "newest", Sort.by("id").descending(),
            "oldest", Sort.by("id").ascending(),
            "best-seller", Sort.by("soldQuantity").descending(),
            "default", Sort.unsorted());

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getFirstEnabledProduct() {
        return productRepository.findFirstByEnabledOrderByIdAsc(true);
    }

    public Product add(Product product) {
        String mainImagePath = product.getMain_image_url();
        String firstSubImage = product.getSub_images().get(0).getSub_image_url();
        String secondSubImage = product.getSub_images().get(1).getSub_image_url();
        String thirdSubImage = product.getSub_images().get(2).getSub_image_url();
        product.setMain_image_url("");
        product.getSub_images().get(0).setSub_image_url("");
        product.getSub_images().get(1).setSub_image_url("");
        product.getSub_images().get(2).setSub_image_url("");
        Product savedProduct = productRepository.save(product);
        // Create directory: images/{product-id} at project root level
        String savedDir = ImageService.IMAGES_FINAL_PATH + savedProduct.getId();
        try {
            Files.createDirectories(Path.of(savedDir));
            imageService.base64ToFileNIO(mainImagePath, savedDir + "/1.jpg");
            imageService.base64ToFileNIO(firstSubImage, savedDir + "/2.jpg");
            imageService.base64ToFileNIO(secondSubImage, savedDir + "/3.jpg");
            imageService.base64ToFileNIO(thirdSubImage, savedDir + "/4.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        savedProduct.setMain_image_url(ImageService.DISPLAY_FINAL_PATH + savedProduct.getId() + "/" +
                "1.jpg");
        savedProduct.getSub_images().get(0).setSub_image_url(
                ImageService.DISPLAY_FINAL_PATH + savedProduct.getId() + "/" + "2.jpg");
        savedProduct.getSub_images().get(1).setSub_image_url(
                ImageService.DISPLAY_FINAL_PATH + savedProduct.getId() + "/" + "3.jpg");
        savedProduct.getSub_images().get(2).setSub_image_url(
                ImageService.DISPLAY_FINAL_PATH + savedProduct.getId() + "/" + "4.jpg");
        savedProduct = productRepository.save(savedProduct);
        eventPublisher.publishEvent(new ProductRelatedUpdateEvent(savedProduct));
        return savedProduct;
    }

    public void update(Product product) throws Exception {
        if (product.getMain_image_url() != null && product.getMain_image_url().contains("base64")) {
            // Save to images/{product-id}/1.jpg at project root
            imageService.base64ToFileNIO(product.getMain_image_url(), ImageService.IMAGES_FINAL_PATH + product.getId() + "/" + "1.jpg");
            product.setMain_image_url(ImageService.DISPLAY_FINAL_PATH + product.getId() + "/" + "1.jpg");
        }
        if (product.getSub_images() != null) {
            for (int i = 0; i < product.getSub_images().size(); i++) {
                SubImage subImage = product.getSub_images().get(i);
                
                if (subImage.getSub_image_url() != null && subImage.getSub_image_url().contains("base64")) {
                    // Save to images/{product-id}/{i+2}.jpg at project root
                    imageService.base64ToFileNIO(subImage.getSub_image_url(), ImageService.IMAGES_FINAL_PATH + product.getId() + "/" + (i + 2) + ".jpg");
                    subImage.setSub_image_url(ImageService.DISPLAY_FINAL_PATH + product.getId() + "/" + (i + 2) + ".jpg");
                }
            }
        }
        Product savedProduct = productRepository.save(product);
        eventPublisher.publishEvent(new ProductRelatedUpdateEvent(savedProduct));
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Transactional
    public void holdProductQuantity(Long productId, double quantity) {
        Product product = getProductById(productId);
        product.setQuantity(product.getQuantity() - quantity);
        product.setHeldQuantity(product.getHeldQuantity() + quantity);
        productRepository.save(product);
    }

    @Transactional
    public void releaseProductQuantity(Long productId, double quantity) {
        Product product = getProductById(productId);
        product.setQuantity(product.getQuantity() + quantity);
        product.setHeldQuantity(product.getHeldQuantity() - quantity);
        productRepository.save(product);
    }

    @Transactional
    public void reduceProductQuantity(Long productId, double quantity) {
        Product product = getProductById(productId);
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
    }

    public ShoppingCartItem getShoppingCartItemByCustomerEmailAndProductId(String email, Long productId) {
        return shoppingCartItemRepository.findByCustomer_EmailAndProduct_Id(email, productId);
    }

    /**
     * Lấy sản phẩm enable theo danh mục với phân trang và sắp xếp
     * 
     * @param categoryId ID danh mục
     * @param page       Số trang
     * @param size       Kích thước trang
     * @param sortBy     Loại sắp xếp
     * @return Trang sản phẩm
     */
    public Page<ViewProductDto> getViewProductsByCategoryWithPagingAndSorting(
            Long categoryId, int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(page, size, SORT_OPTIONS.getOrDefault(sortBy, Sort.unsorted()));
        if (categoryId == 0) {
            return productRepository.findAllViewProductDtoByEnabled(true, pageable);
        } else {
            return productRepository.findViewProductDtoByCategoryIdAndEnabled(categoryId, true, pageable);
        }
    }

    public List<Product> getRelatedProducts(Long id, int limit) {
        Product product = getProductById(id);
        if (product == null) {
            return List.of();
        }
        String productName = product.getName();
        List<Product> allProducts = productRepository.findAllByEnabled(true).stream()
                .filter(p -> !p.getId().equals(id))
                .toList();
        return allProducts.stream()
                .filter(p -> isProductNameRelated(p.getName(), productName))
                .limit(limit)
                .toList();
    }

    public double getSoldQuantity(Long id) {
        return orderRepository.findAll().stream()
                .filter(order -> (orderStatusService.isProcessingStatus(order) ||
                        orderStatusService.isShippingStatus(order) ||
                        orderStatusService.isDeliveredStatus(order)))
                .flatMap(order -> order.getOrderItem().stream())
                .filter(item -> item.getProduct().getId().equals(id))
                .mapToDouble(OrderItem::getQuantity)
                .sum();
    }

    private boolean isProductNameRelated(String productName, String anotherName) {
        if (productName.isEmpty() || anotherName.isEmpty())
            return false;
        String[] keywords = anotherName.split(" ");
        for (String keyword : keywords) {
            if (isKeywordInProductName(productName, keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKeywordInProductName(String productName, String keyword) {
        if (productName.isEmpty() || keyword.isEmpty())
            return false;
        String[] splitedWords = productName.split(" ");
        for (String word : splitedWords) {
            if (word.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    public Page<ViewProductDto> searchViewProductDto(String keyword, Long categoryId, int page, int size,
            String sortBy) {

        Pageable pageable = PageRequest.of(page, size, SORT_OPTIONS.get(sortBy));
        if (categoryId == 0) {
            return productRepository.findViewProductDtoByProductNameAndEnabled(keyword, true, pageable);
        }

        return productRepository.findViewProductDtoByProductNameAndCategoryIdAndEnabled(
                keyword, true, categoryId, pageable);
    }

    public Page<Product> GetAllProductList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    public Page<Product> searchProductForSeller(String name, Boolean enabled,
                                                Long minPrice, Long maxPrice, Pageable pageable) {
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasEnabled = enabled != null;
        boolean hasMinPrice = minPrice != null;
        boolean hasMaxPrice = maxPrice != null;

        if (hasName || hasEnabled || hasMinPrice || hasMaxPrice) {
            return productRepository.findByNameContainingIgnoreCaseAndEnabledAndPrice(name, enabled,minPrice,maxPrice, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    public Product getLastProduct() {
        return productRepository.findTopByOrderByIdDesc();
    }

    public boolean checkUniqueProductNameForUpdate(Long productId, String name) {
        return productRepository
                .findAll()
                .stream()
                .anyMatch(p -> p.getName().equals(name) && p.getId() != productId);
    }

    public boolean checkUniqueProductFromSellerRequestForUpdate(Long productId,String name) throws JsonProcessingException{
        boolean existed = false;
        for (SellerRequest sellerRequest : sellerRequestService.getSellerRequestByEntityName(Product.class)) {
            Product p = sellerRequestService.getEntityFromContent(sellerRequest.getContent(), Product.class);
            if(sellerRequestStatusService.isPendingStatus(sellerRequest) && p.getName().equals(name) && p.getId() != productId){
                existed = true;
                break;
            }
        }
        return existed;
    }

    public boolean checkUniqueProductNameForCreate(String name) {
        return productRepository
                .findAll()
                .stream()
                .anyMatch(p -> p.getName().equals(name));
    }

    public boolean checkUniqueProductFromSellerRequestForCreate(String name) throws JsonProcessingException{
        boolean existed = false;
        for (SellerRequest sellerRequest : sellerRequestService.getSellerRequestByEntityName(Product.class)) {
            Product p = sellerRequestService.getEntityFromContent(sellerRequest.getContent(), Product.class);
            if(sellerRequestStatusService.isPendingStatus(sellerRequest) && p.getName().equals(name)){
                existed = true;
                break;
            }
        }
        return existed;
    }

    public void validateCreateProductDto(CreateProductDto productDto, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldErrors().get(0);
            String message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            throw new RuntimeException(message);
        }
        if (checkUniqueProductNameForCreate(productDto.getName()) || checkUniqueProductFromSellerRequestForCreate(productDto.getName())) {
            throw new Exception("Tên sản phẩm đã tồn tại");
        }
        if (productDto.getImage() == null || productDto.getImage().isEmpty()) {
            throw new Exception("Vui lòng tải lên ảnh chính cho sản phẩm");
        }
        for (MultipartFile file : productDto.getSubImages()) {
            if (file == null || file.isEmpty()) {
                throw new Exception("Vui lòng tải lên đủ 3 ảnh phụ cho sản phẩm");
            }
        }
    }

    public Product createProductForAddRequest(CreateProductDto productDto)
            throws Exception { 
        productDto.setCategories(new ArrayList<>());
        for (Long catId : productDto.getCategoryIds()) {
            productDto.getCategories().add(categoryService.getCategoryById(catId));
        }
        Product product = Product.builder()
                .name(productDto.getName())
                .description(productDto.getDescription())
                .price(productDto.getPrice())
                .unit(productDto.getUnit())
                .enabled(productDto.isEnabled())
                .categories(productDto.getCategories())
                .sub_images(new ArrayList<>())
                .quantity(productDto.getQuantity())
                .main_image_url(ImageService.convertToBase64WithPrefix(productDto.getImage()))
                .heldQuantity(productDto.getHeldQuantity())
                .build();
        List<SubImage> pendingSubImages = new ArrayList<>();

        for (int i = 0; i < productDto.getSubImages().size(); i++) {
            SubImage subImage = SubImage.builder()
                    .product(product)
                    .sub_image_url(ImageService.convertToBase64WithPrefix(productDto.getSubImages().get(i)))
                    .build();
            pendingSubImages.add(subImage);
        }
        product.setSub_images(pendingSubImages);
        return product;
    }

    

    public Product createProductForUpdateRequest(UpdateProductDto updateProductDto, 
                                                  Product oldProduct, MultipartFile imageFile, 
                                                  MultipartFile[] subImageFiles) throws Exception {
        List<Category> categories = new ArrayList<>();
        List<SubImage> subImages = new ArrayList<>();
        for (Long catId : updateProductDto.getCategories()) {
            categories.add(categoryService.getCategoryById(catId));
        }
        updateProductDto.setFinalCategories(categories);
        if (checkUniqueProductNameForUpdate(updateProductDto.getId(), updateProductDto.getName()) || checkUniqueProductFromSellerRequestForUpdate(updateProductDto.getId(), updateProductDto.getName())) {
            throw new Exception("Tên sản phẩm đã tồn tại");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            updateProductDto.setMainImage(oldProduct.getMain_image_url());
        } else {
            updateProductDto.setMainImage(ImageService.convertToBase64WithPrefix(imageFile));
        }
        Product updateProduct = new Product(updateProductDto);
        for (int i = 0; i < subImageFiles.length; i++) {
            if (subImageFiles[i] == null || subImageFiles[i].isEmpty()) {
                subImages.add(oldProduct.getSub_images().get(i));
            } else {
                SubImage sub = new SubImage();
                sub.setProduct(updateProduct);
                sub.setSub_image_url(ImageService.convertToBase64WithPrefix(subImageFiles[i]));
                subImages.add(sub);
            }
        }
        updateProduct.setSub_images(subImages);
        return updateProduct;
    }
}
