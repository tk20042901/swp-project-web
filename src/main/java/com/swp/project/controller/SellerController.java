package com.swp.project.controller;

import java.security.Principal;
import java.util.List;
import com.swp.project.dto.*;
import com.swp.project.entity.product.ProductUnit;
import com.swp.project.entity.seller_request.SellerRequest;
import com.swp.project.service.user.SellerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.product.ProductUnitService;
import com.swp.project.service.seller_request.SellerRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/seller")
public class SellerController {

    private final OrderStatusService orderStatusService;
    private final OrderService orderService;
    private final ProductService productService;
    private final ProductUnitService unitService;
    private final CategoryService categoryService;
    private final SellerRequestService sellerRequestService;
    private final ProductUnitService productUnitService;
    private final SellerService sellerService;
    private final SellerRequestService sellerRequesService;

    @GetMapping("")
    public String index(Model model) {
        List<Order> orders = sellerService.get5NearOrder();
        model.addAttribute("totalOrder",orderService.getTotalOrders());
        model.addAttribute("deliverOrder",orderService.getTotalDeliveredOrders());
        model.addAttribute("processingOrder",orderService.getTotalProcessingOrders());
        model.addAttribute("pendingOrder",orderService.getTotalPendingOrders());
        model.addAttribute("shippingOrder",orderService.getTotalShippingOrders());
        model.addAttribute("totalCanceledOrder", orderService.getTotalCancelledOrders());
        model.addAttribute("nearlySoldOutProducts", orderService.getNearlySoldOutProduct());
        model.addAttribute("top5ProductRevenue",sellerService.getTop5ProductRevenue());
        model.addAttribute("recentOrders",orders);
        return "pages/seller/index";
    }

    @GetMapping("/all-orders")
    public String allOrdersList(@Valid @ModelAttribute SellerSearchOrderDto sellerSearchOrderDto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("orders", orderService.getAllOrder());
            model.addAttribute("orderStatusService", orderStatusService);
            model.addAttribute("sellerSearchOrderDto", sellerSearchOrderDto);
            return "pages/seller/order/all-orders";
        }

        if (sellerSearchOrderDto.isEmpty()) {
            model.addAttribute("orders", orderService.getAllOrder());
        } else {
            Page<Order> orders = orderService.searchOrder(sellerSearchOrderDto);
            int totalPages = orders.getTotalPages();
            if (totalPages > 0 && Integer.parseInt(sellerSearchOrderDto.getGoToPage()) > totalPages) {
                bindingResult.rejectValue("goToPage", "invalid.range",
                        "Trang phải trong khoảng 1 đến " + totalPages);
                sellerSearchOrderDto.setGoToPage("1");
                orders = orderService.searchOrder(sellerSearchOrderDto);
            }
            model.addAttribute("orders", orders);
        }
        model.addAttribute("orderStatusService", orderStatusService);
        model.addAttribute("sellerSearchOrderDto", sellerSearchOrderDto);
        return "pages/seller/order/all-orders";
    }

    @GetMapping("/order-detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model) {
        if (orderService.isOrderItemQuantityMoreThanAvailable(orderId) &&
                orderStatusService.isPendingConfirmationStatus(orderService.getOrderById(orderId))) {
            model.addAttribute("warning",
                    "Cảnh báo: Một số sản phẩm trong đơn hàng này có số lượng lớn hơn số lượng hiện có trong kho.");
        }
        model.addAttribute("orderStatusService", orderStatusService);
        model.addAttribute("order", orderService.getOrderById(orderId));
        return "pages/seller/order/order-detail";
    }

    @PostMapping("/update-pending-order-status")
    public String updatePendingOrderStatus(@RequestParam Long orderId,
            @RequestParam String action,
            RedirectAttributes redirectAttributes) {
        if (action.equals("accept")) {
            if (orderService.isOrderItemQuantityMoreThanAvailable(orderId)) {
                redirectAttributes.addFlashAttribute("error",
                        "Lỗi: Một số sản phẩm trong đơn hàng vừa chấp nhận có số lượng lớn hơn số lượng hiện có trong kho. Tác vụ bị hủy.");
            } else {
                orderService.doWhenCodOrderConfirmed(orderService.getOrderById(orderId));
                redirectAttributes.addFlashAttribute("msg", "Chấp nhận đơn hàng thành công");
            }
        } else if (action.equals("reject")) {
            orderService.setOrderStatus(orderId, orderStatusService.getCancelledStatus());
            redirectAttributes.addFlashAttribute("msg",
                    "Từ chối đơn hàng thành công");
        }
        return "redirect:/seller/order-detail/"+orderId;
    }

    @PostMapping("/update-processing-order-status")
    public String updateProcessingOrderStatus(@RequestParam Long orderId,
            RedirectAttributes redirectAttributes) {
        orderService.markOrderStatusAsShipping(orderService.getOrderById(orderId));
        redirectAttributes.addFlashAttribute("msg",
                "Cập nhật trạng thái đơn hàng thành Đang giao hàng thành công.\n" +
                        "Hệ thống đã tự động phân công Shipper cho đơn hàng.");
        return "redirect:/seller/order-detail/"+orderId;
    }

    @GetMapping("/all-products")
    public String getAllProductList(@RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        Sort sort;
        if ("priceAsc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("price").ascending();
        } else if ("priceDesc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("price").descending();
        }else {
            sort = Sort.by("id").ascending(); // mặc định
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productService.searchProductForSeller(name, enabled,minPrice,maxPrice, pageable);
        model.addAttribute("products", products);
        model.addAttribute("name", name);
        model.addAttribute("enabled", enabled);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        return "pages/seller/product/all-products";
    }

    @GetMapping("/product/product-detail/{productId}")
    public String getProductDetail(@PathVariable("productId") Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "pages/seller/product/product-detail";
    }

//    @GetMapping("/statistic-report")
//    public String getSellerReport(Model model) {
//        List<Order> orders = sellerService.get5NearOrder();
//        model.addAttribute("totalOrder",orderService.getTotalOrders());
//        model.addAttribute("deliverOrder",orderService.getTotalDeliveredOrders());
//        model.addAttribute("processingOrder",orderService.getTotalProcessingOrders());
//        model.addAttribute("pendingOrder",orderService.getTotalPendingOrders());
//        model.addAttribute("shippingOrder",orderService.getTotalShippingOrders());
//        model.addAttribute("totalCanceledOrder", orderService.getTotalCancelledOrders());
//        model.addAttribute("nearlySoldOutProducts", orderService.getNearlySoldOutProduct());
//        model.addAttribute("top5ProductRevenue",sellerService.getTop5ProductRevenue());
//        model.addAttribute("recentOrders",orders);
//        return "pages/seller/index";
//    }


    @GetMapping("/product-report")
    public String getProductRevenueReport(
            @RequestParam(required = false) String searchName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        Page<ProductRevenueDto> productRevenues = sellerService.getProductRevenue(searchName,page,size);
        model.addAttribute("products", productRevenues);
        model.addAttribute("searchName", searchName);
        return "pages/seller/statistic-report/product-report";
    }

    @GetMapping("/seller-update-product/{id}")
    public String showUpdateProductForm(
            @PathVariable Long id,
            Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("units", unitService.getAllActiveProductUnits());
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("updateProductDto", new UpdateProductDto(product));
        return "pages/seller/product/update-product";
    }

    @PostMapping("/seller-update-product")
    public String handleUpdateProduct(
            @Valid @ModelAttribute UpdateProductDto updateProductDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @RequestParam MultipartFile imageFile,
            @RequestParam MultipartFile[] subImageFiles,
            Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/seller/seller-update-product/" + updateProductDto.getId();
        }
        try {
            Product oldProduct = productService.getProductById(updateProductDto.getId());
            Product updateProduct = productService.createProductForUpdateRequest(updateProductDto,oldProduct, imageFile, subImageFiles);
            sellerRequestService.saveUpdateRequest(oldProduct, updateProduct, principal.getName());
            redirectAttributes.addFlashAttribute("msg", "Yêu cầu cập nhật sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/seller-update-product/" + updateProductDto.getId();
        }
        return "redirect:/seller";
    }

    @GetMapping("/seller-create-product")
    public String showCreateProductForm(Model model) {
        CreateProductDto newProduct = new CreateProductDto();
        newProduct.setCategories(categoryService.getAllActiveCategories());
        model.addAttribute("productDto", newProduct);
        model.addAttribute("units", unitService.getAllActiveProductUnits());
        return "pages/seller/product/create-product";
    }

    @PostMapping("/seller-create-product")
    public String handleCreateProduct(
            @Valid @ModelAttribute CreateProductDto productDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        try {
            productService.validateCreateProductDto(productDto, bindingResult);
            Product product = productService.createProductForAddRequest(productDto);
            sellerRequestService.saveAddRequest(product, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Yêu cầu tạo sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/seller-create-product";
    }

    @GetMapping("/product-unit")
    public String getProductUnitList(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductUnit> productUnits = productUnitService.getAllProductUnit(pageable);
        model.addAttribute("productUnits", productUnits);
        return "pages/seller/product/product-unit";
    }

    @GetMapping("/product-category")
    public String getAllProductUnit(Model model,
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Page<Category> categories = categoryService.searchByCategoryName(categoryName, size, page);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryName", categoryName);
        return "pages/seller/product/product-category";
    }

    @GetMapping("/create-product-unit")
    public String showCreateProductUnitForm(Model model) {
        CreateProductUnitDto createProductUnitDto = new CreateProductUnitDto();
        model.addAttribute("productUnitDto", createProductUnitDto);
        return "pages/seller/product/create-product-unit";
    }

    @PostMapping("/create-product-unit")
    public String handleCreateProductUnit(
            @Valid @ModelAttribute CreateProductUnitDto productUnitDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ thông tin");
            return "redirect:/seller/create-product-unit";
        }
        try {
            productUnitService.createNewProductUnit(productUnitDto,principal);
            redirectAttributes.addFlashAttribute("success", "Yêu cầu tạo đơn vị sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/create-product-unit";
    }

    @GetMapping("/edit-product-unit")
    public String showEditProductUnitForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductUnit productUnit = productUnitService.getProductUnitById(id);
            if (productUnit == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn vị sản phẩm");
                return "redirect:/seller/product-unit";
            }
            UpdateProductUnitDto updateProductUnitDto = new UpdateProductUnitDto(productUnit);
            model.addAttribute("updateProductUnitDto", updateProductUnitDto);
            return "pages/seller/product/edit-product-unit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/seller/product-unit";
        }
    }

    @PostMapping("/edit-product-unit")
    public String handleEditProductUnit(
            @Valid @ModelAttribute UpdateProductUnitDto updateProductUnitDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ thông tin");
            return "redirect:/seller/edit-product-unit?id=" + updateProductUnitDto.getId();
        }
        try {
            productUnitService.updateProductUnit(updateProductUnitDto,principal);
            redirectAttributes.addFlashAttribute("success", "Yêu cầu cập nhật đơn vị sản phẩm đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/edit-product-unit?id=" + updateProductUnitDto.getId();
        }
        return "redirect:/seller/product-unit";
    }
    @GetMapping("/create-product-category")
    public String showCreateCategoryForm(Model model) {
        CreateCategoryDto createCategoryDto = new CreateCategoryDto();
        model.addAttribute("categoryDto", createCategoryDto);
        return "pages/seller/product/create-product-category";
    }

    @PostMapping("/create-product-category")
    public String handleCreateCategory(
            @Valid @ModelAttribute CreateCategoryDto categoryDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ thông tin");
            return "redirect:/seller/create-product-category";
        }
        try {
            Category category = new Category(categoryDto);
            if(categoryService.nameAlreadyExistsForCreate(category.getName())){
                throw new Exception("Danh mục đã tồn tại");
            }
            if(categoryService.nameAlreadyExistsInSellerRequest(category.getName())){
                throw new Exception("Danh mục đã tồn tại trong yêu cầu của người bán");
            }
            sellerRequestService.saveAddRequest(category, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Yêu cầu tạo danh mục đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/create-product-category";
    }

    @GetMapping("/edit-product-category")
    public String showEditCategoryForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes) {
        try {

            
            Category category = categoryService.getCategoryById(id);
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục");
                return "redirect:/seller/product-category";
            }

            
            UpdateCategoryDto updateCategoryDto = new UpdateCategoryDto(category);
                    
            model.addAttribute("updateCategoryDto", updateCategoryDto);
            return "pages/seller/product/edit-product-category";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/seller/product-category";
        }
    }

    @PostMapping("/edit-product-category")
    public String handleEditCategory(
            @Valid @ModelAttribute UpdateCategoryDto updateCategoryDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng điền đầy đủ thông tin");
            return "redirect:/seller/edit-product-category?id=" + updateCategoryDto.getId();
        }
        try {
            categoryService.updateCategory(updateCategoryDto,principal);
            redirectAttributes.addFlashAttribute("success", "Yêu cầu cập nhật danh mục đã được gửi đến quản lý");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/seller/edit-product-category?id=" + updateCategoryDto.getId();
        }
        return "redirect:/seller/product-category";
    }


    @GetMapping("/my-requests")
    public String getMyRequests(
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long typeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model) {
        
        List<SellerRequest> allRequests = sellerRequestService.getSellerRequestBySellerEmail(principal.getName());
        
        // Filter requests
        if (entityName != null && !entityName.isEmpty()) {
            allRequests = allRequests.stream()
                    .filter(req -> req.getEntityName().equals(entityName))
                    .toList();
        }
        
        if (statusId != null) {
            allRequests = allRequests.stream()
                    .filter(req -> req.getStatus().getId().equals(statusId))
                    .toList();
        }
        
        if (typeId != null) {
            allRequests = allRequests.stream()
                    .filter(req -> req.getRequestType().getId().equals(typeId))
                    .toList();
        }
        
        // Sort by createdAt descending (newest first)
        allRequests = allRequests.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .toList();
        
        // Pagination
        int start = page * size;
        int end = Math.min(start + size, allRequests.size());
        List<SellerRequest> paginatedRequests = allRequests.subList(start, end);
        
        model.addAttribute("requests", paginatedRequests);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) allRequests.size() / size));
        model.addAttribute("totalElements", allRequests.size());
        model.addAttribute("size", size);
        model.addAttribute("entityName", entityName);
        model.addAttribute("statusId", statusId);
        model.addAttribute("typeId", typeId);
        model.addAttribute("hasNext", end < allRequests.size());
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("isFirst", page == 0);
        model.addAttribute("isLast", end >= allRequests.size());
        return "pages/seller/seller-requests";
    }
}
