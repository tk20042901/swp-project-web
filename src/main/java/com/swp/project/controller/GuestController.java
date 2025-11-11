package com.swp.project.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.swp.project.dto.AiMessageDto;
import com.swp.project.dto.CategoryDto;
import com.swp.project.dto.RegisterDto;
import com.swp.project.dto.ViewProductDto;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.SubImage;
import com.swp.project.service.AiService;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.user.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class GuestController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AiService aiService;
    private final CustomerService customerService;
    private final static int PAGE_SIZE = 9;

    @Value("${recaptcha.site-key}")
    private String recaptchaSite;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
<<<<<<< HEAD
        model.addAttribute("siteKey", recaptchaSite);
        return "pages/guest/login";
=======
        return "/pages/guest/login";
>>>>>>> 8e8602e705488d0b1a3f824be7613312050886b0
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
<<<<<<< HEAD
        model.addAttribute("siteKey", recaptchaSite);
        return "pages/guest/register";
=======
        return "/pages/guest/register";
>>>>>>> 8e8602e705488d0b1a3f824be7613312050886b0
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute RegisterDto registerDto, BindingResult bindingResult,
            RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            return "/pages/guest/register";
        }
        try {
            customerService.register(registerDto);
            redirectAttributes.addFlashAttribute("email", registerDto.getEmail());
            return "redirect:/verify-otp";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@ModelAttribute("email") String email,
            Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/register";
        }
        model.addAttribute("email", email);
        return "pages/guest/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
            @RequestParam String otp,
            Model model) {
        try {
            customerService.verifyOtp(email, otp);
            return "redirect:/login?register_success";
        } catch (RuntimeException e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "pages/guest/verify-otp";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("siteKey", recaptchaSite);
        return "pages/guest/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            customerService.forgotPassword(email);
            redirectAttributes.addFlashAttribute("success",
                    "Mật khẩu mới vừa được gửi tới " + email);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/forgot-password";
    }

    @GetMapping({ "/" })
    public String getHomepage(
            Model model) {
        try {
            model.addAttribute("url", "/");
            model.addAttribute("Title", "Trang danh sách sản phẩm");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "pages/guest/homepage";
    }

    @GetMapping("/search-product")
    public String searchProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "default") String sortBy,
            Model model) {

        // Handle empty or null keywords
        if (keyword == null || keyword.trim().isEmpty()) {
            return "redirect:/product-category-sorting?categoryId=" + categoryId + "&sortBy=" + sortBy;
        }

        Page<ViewProductDto> products = productService.searchViewProductDto(keyword, categoryId, page, size, sortBy);
        List<Category> categories = categoryService.getUniqueCategoriesBaseOnPageOfProduct(
                productService.searchViewProductDto(keyword, 0L, page, size, "default").getContent());

        model.addAttribute("viewProductDto", products);
        model.addAttribute("totalElement", products.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("categories", categories);
        model.addAttribute("Title", "Kết quả tìm kiếm cho \"" + keyword + "\"");
        model.addAttribute("url", "/search-product");
        model.addAttribute("keyword", keyword);
        model.addAttribute("hadKeyword", true);
        model.addAttribute("showSearchBar", true);
        return "pages/guest/product-category-sorting";
    }

    @GetMapping("/product-category-sorting")
    public String getAllProductsWithSorting(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(required = false) String sortBy,
            Model model) {

        // Add categories for the dropdown filter
        List<Category> categories = categoryService.getAllCategories();
        Page<ViewProductDto> productsPage = productService.getViewProductsByCategoryWithPagingAndSorting(categoryId,
                page, size, sortBy);

        model.addAttribute("viewProductDto", productsPage);
        model.addAttribute("totalElement", productsPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("categories", categories);
        model.addAttribute("Title", "Danh sách sản phẩm");
        model.addAttribute("url", "/product-category-sorting");
        model.addAttribute("showSearchBar", true);
        return "pages/guest/product-category-sorting";
    }

    @GetMapping("/api/products")
    @ResponseBody
    public List<ViewProductDto> getViewProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + PAGE_SIZE) int size,
            @RequestParam(defaultValue = "0") Long categoryId,
            @RequestParam(required = false) String sortBy) {
        Page<ViewProductDto> productsPage = productService.getViewProductsByCategoryWithPagingAndSorting(categoryId,
                page, size, sortBy);
        return productsPage.getContent();
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public List<CategoryDto> getCategory() {
        return categoryService.getAllActiveCategories().stream().map(category -> {
            CategoryDto dto = new CategoryDto();
            dto.setId(category.getId());
            dto.setName(category.getName());
            return dto;
        }).toList();
    }

    @GetMapping("/product/{id}")
    public String getProductDetails(
            @PathVariable(name = "id") Long id,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        Product product = productService.getProductById(id);

        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm đã bị xóa hoặc không còn tồn tại");
            return "redirect:/";
        }

        List<SubImage> subImages = product.getSub_images();
        boolean isAllowDecimal = product.getUnit().isAllowDecimal();
        model.addAttribute("product", product);
        model.addAttribute("subImages", subImages);

        double quantityInCart = customerService.getProductQuantityInCart(principal, id);

        if (isAllowDecimal) {
            model.addAttribute("maxQuantity", product.getQuantity() - quantityInCart);
        } else {
            model.addAttribute("maxQuantity", (int) Math.floor(product.getQuantity() - quantityInCart));
        }

        List<Product> relatedProducts = productService.getRelatedProducts(id, 6);
        model.addAttribute("relatedProducts", relatedProducts);

        double soldQuantity = productService.getSoldQuantity(id);

        if (isAllowDecimal) {
            model.addAttribute("soldQuantity", soldQuantity);
        } else {
            model.addAttribute("soldQuantity", (int) Math.floor(soldQuantity));
        }

        List<Category> categories = product.getCategories();
        model.addAttribute("categories", categories);

        model.addAttribute("quantityInCart", quantityInCart);
        model.addAttribute("showSearchBar", true);
        return "pages/guest/product";
    }

    @GetMapping("/ai")
    public String ask(Model model, HttpSession session) {
        String conversationId = UUID.randomUUID().toString();
        model.addAttribute("conversationId", conversationId);
        session.removeAttribute("conversation");
        List<AiMessageDto> conversation = new ArrayList<>();
        session.setAttribute("conversation", conversation);
        aiService.initChat(conversationId,conversation);
        model.addAttribute("conversation", conversation);
        return "pages/guest/ai";
    }

    @PostMapping("/ai")
    public String ask(@RequestParam String conversationId,
            @RequestParam String q,
            @RequestParam MultipartFile image,
            HttpSession session,
            Model model) {
        List<AiMessageDto> conversation = (List<AiMessageDto>) session.getAttribute("conversation");
        try {
            aiService.ask(conversationId, q, image, conversation);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("conversationId", conversationId);
        session.setAttribute("conversation", conversation);
        model.addAttribute("conversation", conversation);
        return "pages/guest/ai";
    }

    @PostMapping("/product/add")
    public String addToCart(
            @RequestParam(value = "productId") String productIdString,
            @RequestParam(value = "quantity") String quantityString,
            RedirectAttributes redirectAttributes,
            Principal principal) {
        try {
            long productId;
            double quantity;
            try {
                productId = Long.parseLong(productIdString);
                quantity = Double.parseDouble(quantityString);
            } catch (NumberFormatException e) {
                throw new Exception("Dữ liệu không hợp lệ");
            }
            customerService.addShoppingCartItem(principal, productId, quantity);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/product/" + productIdString;
        }
        redirectAttributes.addFlashAttribute("msg", "Thêm sản phẩm vào giỏ hàng thành công");
        return "redirect:/product/" + productIdString;
    }
    
}
