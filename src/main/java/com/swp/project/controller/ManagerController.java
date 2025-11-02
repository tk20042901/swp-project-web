package com.swp.project.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swp.project.dto.RevenueDto;
import com.swp.project.dto.StaffDto;
import com.swp.project.entity.address.CommuneWard;
import com.swp.project.entity.address.ProvinceCity;
import com.swp.project.entity.order.Bill;
import com.swp.project.entity.product.Category;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.product.ProductUnit;
import com.swp.project.entity.seller_request.SellerRequest;
import com.swp.project.entity.user.Seller;
import com.swp.project.entity.user.Shipper;
import com.swp.project.service.AddressService;
import com.swp.project.service.order.BillService;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.product.CategoryService;
import com.swp.project.service.product.ImageService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.product.ProductUnitService;
import com.swp.project.service.seller_request.SellerRequestService;
import com.swp.project.service.seller_request.SellerRequestStatusService;
import com.swp.project.service.seller_request.SellerRequestTypeService;
import com.swp.project.service.user.SellerService;
import com.swp.project.service.user.ShipperService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final SellerRequestTypeService sellerRequestTypeService;
    private final SellerService sellerService;
    private final ShipperService shipperService;
    private final AddressService addressService;
    private final SellerRequestService sellerRequestService;
    private final ProductService productService;
    private final ProductUnitService productUnitService;
    private final CategoryService categoryService;
    private final BillService billService;
    private final int numEachPage = 10;
    private final OrderService orderService;
    private final SellerRequestStatusService sellerRequestStatusService;
    private final ImageService imageService;

    @GetMapping("")
    public String index() {
        return "forward:/manager/report";
    }

    @GetMapping("/manage-seller")
    public String manageSeller(
            @RequestParam(value = "sortCriteria", required = false) String sortCriteria,
            @RequestParam(value = "subpageIndex", required = false, defaultValue = "1") Integer subpageIndex,
            @RequestParam(value = "queryEmail", required = false) String queryEmail,
            @RequestParam(value = "queryName", required = false) String queryName,
            @RequestParam(value = "queryAddress", required = false) String queryAddress,
            @RequestParam(value = "queryCid", required = false) String queryCid,
            @RequestParam(value = "sortCriteriaInPage", required = false) String sortCriteriaInPage,
            HttpSession session,
            Model model) {
        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (session.getAttribute("sortCriteriaInPage") == null) {
            session.setAttribute("sortCriteriaInPage", "id");
        }
        if (session.getAttribute("subpageIndex") == null) {
            session.setAttribute("subpageIndex", 1);
        }
        if (session.getAttribute("numEachPage") == null) {
            session.setAttribute("numEachPage", numEachPage);
        }
        if (session.getAttribute("queryEmail") == null) {
            session.setAttribute("queryEmail", "");
        }
        if (session.getAttribute("queryName") == null) {
            session.setAttribute("queryName", "");
        }
        if (session.getAttribute("queryAddress") == null) {
            session.setAttribute("queryAddress", "");
        }
        if (session.getAttribute("queryCid") == null) {
            session.setAttribute("queryCid", "");
        }

        if (queryEmail != null) {
            session.setAttribute("queryEmail", queryEmail);
            session.setAttribute("subpageIndex", 1);
        }
        if (queryName != null) {
            session.setAttribute("queryName", queryName);
            session.setAttribute("subpageIndex", 1);
        }
        if (queryAddress != null) {
            session.setAttribute("queryAddress", queryAddress);
            session.setAttribute("subpageIndex", 1);
        }
        if (queryCid != null) {
            session.setAttribute("queryCid", queryCid);
            session.setAttribute("subpageIndex", 1);
        }
        if (sortCriteria != null && !sortCriteria.isEmpty()) {
            session.setAttribute("sortCriteria", sortCriteria);
        }
        if (sortCriteriaInPage != null && !sortCriteriaInPage.isEmpty()) {
            session.setAttribute("sortCriteriaInPage", sortCriteriaInPage);
            int k = (int) session.getAttribute("k");
            k = -k;
            session.setAttribute("k", k);
        }

        if (subpageIndex != null) {
            session.setAttribute("subpageIndex", subpageIndex);
        }

        Page<Seller> sellers = 
                sellerService.getSellers(
                (Integer) session.getAttribute("subpageIndex"),
                numEachPage,
                session.getAttribute("queryEmail").toString(),
                session.getAttribute("queryName").toString(),
                session.getAttribute("queryAddress").toString(),
                session.getAttribute("queryCid").toString(),
                (String) session.getAttribute("sortCriteria"),
                (Integer) session.getAttribute("k"),
                (String) session.getAttribute("sortCriteriaInPage"));

        model.addAttribute("list", sellers.getContent());

        model.addAttribute("totalPages", sellers.getTotalPages());
        return "pages/manager/manage-seller";
    }

    @GetMapping("/manage-shipper")
    public String manageShipper(
            @RequestParam(value = "sortCriteria", required = false) String sortCriteria,
            @RequestParam(value = "subpageIndex", required = false, defaultValue = "1") Integer subpageIndex,
            @RequestParam(value = "queryEmail", required = false) String queryEmail,
            @RequestParam(value = "queryName", required = false) String queryName,
            @RequestParam(value = "queryAddress", required = false) String queryAddress,
            @RequestParam(value = "queryCid", required = false) String queryCid,
            @RequestParam(value = "sortCriteriaInPage", required = false) String sortCriteriaInPage,
            HttpSession session,
            Model model) {
        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (session.getAttribute("sortCriteriaInPage") == null) {
            session.setAttribute("sortCriteriaInPage", "id");
        }
        if (session.getAttribute("subpageIndex") == null) {
            session.setAttribute("subpageIndex", 1);
        }
        if (session.getAttribute("numEachPage") == null) {
            session.setAttribute("numEachPage", numEachPage);
        }
        if (session.getAttribute("queryEmail") == null) {
            session.setAttribute("queryEmail", "");
        }
        if (session.getAttribute("queryName") == null) {
            session.setAttribute("queryName", "");
        }
        if (session.getAttribute("queryAddress") == null) {
            session.setAttribute("queryAddress", "");
        }
        if (session.getAttribute("queryCid") == null) {
            session.setAttribute("queryCid", "");
        }

        if (queryEmail != null) {
            session.setAttribute("queryEmail", queryEmail);
            session.setAttribute("subpageIndex", 1);
        }
        if (queryName != null) {
            session.setAttribute("queryName", queryName);
            session.setAttribute("subpageIndex", 1);
        }
        if (queryAddress != null) {
            session.setAttribute("queryAddress", queryAddress);
            session.setAttribute("subpageIndex", 1);
        }
        if (queryCid != null) {
            session.setAttribute("queryCid", queryCid);
            session.setAttribute("subpageIndex", 1);
        }
        if (sortCriteria != null && !sortCriteria.isEmpty()) {
            session.setAttribute("sortCriteria", sortCriteria);
        }
        if (sortCriteriaInPage != null && !sortCriteriaInPage.isEmpty()) {
            session.setAttribute("sortCriteriaInPage", sortCriteriaInPage);
            int k = (int) session.getAttribute("k");
            k = -k;
            session.setAttribute("k", k);
        }

        if (subpageIndex != null) {
            session.setAttribute("subpageIndex", subpageIndex);
        }

        Page<Shipper> shippers = shipperService.getShippers(
                (Integer) session.getAttribute("subpageIndex"),
                numEachPage,
                session.getAttribute("queryEmail").toString(),
                session.getAttribute("queryName").toString(),
                session.getAttribute("queryAddress").toString(),
                session.getAttribute("queryCid").toString(),
                (String) session.getAttribute("sortCriteria"),
                (Integer) session.getAttribute("k"),
                (String) session.getAttribute("sortCriteriaInPage")
        );

        model.addAttribute("list", shippers.getContent());

        model.addAttribute("totalPages", shippers.getTotalPages());
        return "pages/manager/manage-shipper";
    }

    @GetMapping("/edit-staff")
    public String editStaff(
            @RequestParam(value = "className", required = false) String className,
            @RequestParam(value = "email", required = false) String email,
            Model model,
            HttpSession session) {

        List<ProvinceCity> provinces = addressService.getAllProvinceCity();
        List<CommuneWard> wards = new ArrayList<>();
        StaffDto staffDto = (StaffDto) session.getAttribute("staffDto");

        if (className != null && !className.isEmpty()) {
            switch (className) {
                case "Seller":
                    session.setAttribute("newClassName", className);
                    if (email != null && !email.isEmpty()) {
                        Seller seller = sellerService.getByEmail(email);

                        if (seller == null) {
                            staffDto = new StaffDto();
                            break;
                        }

                        staffDto = new StaffDto().parse(seller);
                    } else {
                        staffDto = new StaffDto();
                    }
                    break;
                case "Shipper":
                    session.setAttribute("newClassName", className);
                    if (email != null && !email.isEmpty()) {
                        Shipper shipper = shipperService.getByEmail(email);

                        if (shipper == null) {
                            staffDto = new StaffDto();
                            break;
                        }

                        staffDto = new StaffDto().parse(shipper);
                    } else {
                        staffDto = new StaffDto();
                    }
                    break;
            }
        }
        if (staffDto.getProvinceCity() != null) {
            wards = addressService.getAllCommuneWardByProvinceCityCode(staffDto.getProvinceCity());
        }

        session.setAttribute("provinces", provinces);
        model.addAttribute("wards", wards);
        model.addAttribute("staffDto", staffDto);
        session.setAttribute("staffDto", staffDto);

        return "pages/manager/edit-staff";
    }

    @PostMapping("/edit-staff")
    public String editStaff(
            @Valid @ModelAttribute("staffDto") StaffDto staffDto,
            BindingResult bindingResult,
            @RequestParam("newClassName") String newClassName,
            @RequestParam(value = "submitButton", required = false) String submitButton,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpSession session) {

        session.setAttribute("staffDto", staffDto);

        model.addAttribute("wards", addressService.getAllCommuneWardByProvinceCityCode(staffDto.getProvinceCity()));

        String managerRedirectUrl = "";
        String editRedirectUrl = "redirect:/manager/edit-staff";
        String editForwardUrl = "pages/manager/edit-staff";

        switch (newClassName) {
            case "Seller":
                managerRedirectUrl = "redirect:/manager/manage-seller";
                break;
            case "Shipper":
                managerRedirectUrl = "redirect:/manager/manage-shipper";
                break;
        }

        if (submitButton == null) {
            staffDto.setCommuneWard("");
            return editRedirectUrl;

        } else if (submitButton.equals("save")) {
            if (bindingResult.hasErrors()) {
                return editForwardUrl;
            }
            try {
                if (!newClassName.isEmpty()) {

                    switch (newClassName) {
                        case "Seller":
                            try {
                                sellerService.add(staffDto);
                                if (staffDto.getId() != 0) {
                                    sellerService.setSellerStatus(staffDto.getId(), staffDto.isEnabled());
                                }
                                // sellerService.findByNameAndCid(
                                //         session.getAttribute("queryName").toString(),
                                //         session.getAttribute("queryCid").toString());
                                // sellerService.sortBy(session.getAttribute("sortCriteria").toString(),
                                //         (int) session.getAttribute("k"));

                                // session.setAttribute("list", sellerService.getResults());
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                                return editRedirectUrl;
                            }
                            break;

                        case "Shipper":
                            try {
                                shipperService.add(staffDto);
                                if (staffDto.getId() != 0) {
                                    shipperService.setShipperStatus(staffDto.getId(), staffDto.isEnabled());
                                }
                                shipperService.findByNameAndCid(
                                        session.getAttribute("queryName").toString(),
                                        session.getAttribute("queryCid").toString());
                                shipperService.sortBy(session.getAttribute("sortCriteria").toString(),
                                        (int) session.getAttribute("k"));

                                session.setAttribute("list", shipperService.getResults());
                            } catch (Exception e) {
                                redirectAttributes.addFlashAttribute("error", e.getMessage());
                                return editRedirectUrl;
                            }
                            break;
                    }
                    if (staffDto.getId() == 0) {
                        redirectAttributes.addFlashAttribute("msg",
                                "Thêm tài khoản " + staffDto.getEmail() + " thành công");
                    } else {
                        redirectAttributes.addFlashAttribute("msg",
                                "Sửa tài khoản " + staffDto.getEmail() + " thành công");
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        switch (newClassName) {
            case "Seller":
            case "Shipper":
                return managerRedirectUrl;
            default:
                return "redirect:/manager/manage-seller";
        }
    }

    @GetMapping("/statistic-report")
    public String getManagerStastisticReport(Model model) {
        Long totalUnitSold = orderService.getUnitSold();
        Long revenueToday = orderService.getRevenueToday();
        Long revenueThisWeek = orderService.getRevenueThisWeek();
        Long revenueThisMonth = orderService.getRevenueThisMonth();
        double dailyPercentageChange = orderService.getDailyPercentageChange();
        double weeklyPercentageChange = orderService.getWeeklyPercentageChange();
        double monthlyPercentageChange = orderService.getMonthlyPercentageChange();
        model.addAttribute("totalUnitSold", totalUnitSold == null ? 0 : totalUnitSold);
        model.addAttribute("revenueToday", revenueToday == null ? 0 : revenueToday);
        model.addAttribute("revenueThisWeek", revenueThisWeek == null ? 0 : revenueThisWeek);
        model.addAttribute("revenueThisMonth", revenueThisMonth == null ? 0 : revenueThisMonth);
        model.addAttribute("dailyPercentageChange", dailyPercentageChange);
        model.addAttribute("weeklyPercentageChange", weeklyPercentageChange);
        model.addAttribute("monthlyPercentageChange", monthlyPercentageChange);
        return "pages/manager/statistic-report";
    }

    @GetMapping("/report")
    public String getManagerReport(Model model) {
        Long totalUnitSold = orderService.getUnitSold();
        Long revenueToday = orderService.getRevenueToday();
        Long revenueThisWeek = orderService.getRevenueThisWeek();
        Long revenueThisMonth = orderService.getRevenueThisMonth();
        double dailyPercentageChange = orderService.getDailyPercentageChange();
        double weeklyPercentageChange = orderService.getWeeklyPercentageChange();
        double monthlyPercentageChange = orderService.getMonthlyPercentageChange();
        model.addAttribute("totalUnitSold", totalUnitSold == null ? 0 : totalUnitSold);
        model.addAttribute("revenueToday", revenueToday == null ? 0 : revenueToday);
        model.addAttribute("revenueThisWeek", revenueThisWeek == null ? 0 : revenueThisWeek);
        model.addAttribute("revenueThisMonth", revenueThisMonth == null ? 0 : revenueThisMonth);
        model.addAttribute("dailyPercentageChange", dailyPercentageChange);
        model.addAttribute("weeklyPercentageChange", weeklyPercentageChange);
        model.addAttribute("monthlyPercentageChange", monthlyPercentageChange);
        return "pages/manager/index";
    }

    @GetMapping("/detail-report")
    public String getDetailReport(Model model) {
        List<RevenueDto> daysReport = orderService.getDaysRevenue();
        List<RevenueDto> monthsReport = orderService.getMonthsRevenue();
        model.addAttribute("daysReport", daysReport);
        model.addAttribute("monthsReport", monthsReport);
        return "pages/manager/detail-report";

    }

    @GetMapping("/all-products-request")
    public String getAllProductsRequest(
            Model model) {
        model.addAttribute("sellerRequests", sellerRequestService.getAllSellerRequest());
        return "pages/manager/all-products-request";
    }

    @GetMapping("/product-request-details/{requestId}")
    public String viewRequestChanges(
            @PathVariable Long requestId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            SellerRequest sellerRequest = sellerRequestService.getSellerRequestById(requestId);
            if (sellerRequest == null) {
                redirectAttributes.addFlashAttribute("error", "Yêu cầu không tồn tại");
                return "redirect:/manager/all-products-request";
            }

            String entityName = sellerRequest.getEntityName();
            String returnPage;

            if (ProductUnit.class.getSimpleName().equals(entityName)) {
                returnPage = handleProductUnitRequest(sellerRequest, model);
            } else if (Category.class.getSimpleName().equals(entityName)) {
                returnPage = handleCategoryRequest(sellerRequest, model);
            } else if (Product.class.getSimpleName().equals(entityName)) {
                returnPage = handleProductRequest(sellerRequest, model);
            } else {
                redirectAttributes.addFlashAttribute("error", "Loại yêu cầu không hợp lệ");
                return "redirect:/manager/all-products-request";
            }

            model.addAttribute("sellerRequest", sellerRequest);
            model.addAttribute("isPending", sellerRequestStatusService.isPendingStatus(sellerRequest));
            return returnPage;
        } catch (NullPointerException e) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu yêu cầu không đầy đủ");
            return "redirect:/manager/all-products-request";
        } catch (IndexOutOfBoundsException e) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu hình ảnh không đầy đủ");
            return "redirect:/manager/all-products-request";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xử lý yêu cầu: " + e.getMessage());
            return "redirect:/manager/all-products-request";
        }
    }

    private String handleProductUnitRequest(SellerRequest sellerRequest, Model model) {
        try {
            ProductUnit newProductUnit = sellerRequestService.getEntityFromContent(
                    sellerRequest.getContent(), ProductUnit.class);
            
            if (newProductUnit == null) {
                throw new IllegalArgumentException("Không thể đọc dữ liệu đơn vị sản phẩm");
            }
            
            if (sellerRequestTypeService.isUpdateType(sellerRequest)) {
                ProductUnit oldProductUnit = sellerRequestService.getEntityFromContent(
                        sellerRequest.getOldContent(), ProductUnit.class);
                model.addAttribute("oldProductUnit", oldProductUnit);
            }
            
            model.addAttribute("newProductUnit", newProductUnit);
            return "pages/manager/product-unit-request-details";
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý yêu cầu đơn vị sản phẩm: " + e.getMessage(), e);
        }
    }

    private String handleCategoryRequest(SellerRequest sellerRequest, Model model) {
        try {
            Category newCategory = sellerRequestService.getEntityFromContent(
                    sellerRequest.getContent(), Category.class);
            
            if (newCategory == null) {
                throw new IllegalArgumentException("Không thể đọc dữ liệu danh mục");
            }
            
            if (sellerRequestTypeService.isUpdateType(sellerRequest)) {
                Category oldCategory = sellerRequestService.getEntityFromContent(
                        sellerRequest.getOldContent(), Category.class);
                model.addAttribute("oldCategory", oldCategory);
            }
            
            model.addAttribute("newCategory", newCategory);
            return "pages/manager/category-request-details";
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý yêu cầu danh mục: " + e.getMessage(), e);
        }
    }

    private String handleProductRequest(SellerRequest sellerRequest, Model model) {
        try {
            Product newProduct = sellerRequestService.getEntityFromContent(
                    sellerRequest.getContent(), Product.class);
            
            if (newProduct == null) {
                throw new IllegalArgumentException("Không thể đọc dữ liệu sản phẩm");
            }
            
            if (sellerRequestTypeService.isUpdateType(sellerRequest)) {
                if (sellerRequestStatusService.isPendingStatus(sellerRequest)) {
                    Product oldProductFromContent = sellerRequestService.getEntityFromContent(
                            sellerRequest.getOldContent(), Product.class);
                    if (oldProductFromContent != null) {
                        Product oldProduct = productService.getProductById(oldProductFromContent.getId());
                        model.addAttribute("oldProduct", oldProduct);
                    }
                } else {
                    Product oldProduct = sellerRequestService.getEntityFromContent(
                            sellerRequest.getOldContent(), Product.class);
                    model.addAttribute("oldProduct", oldProduct);
                }
            }
            
            model.addAttribute("newProduct", newProduct);
            
            // Add sub-images with null safety and size validation
            if (newProduct.getSub_images() != null && newProduct.getSub_images().size() >= 3) {
                model.addAttribute("firstNewImage", newProduct.getSub_images().get(0).getSub_image_url());
                model.addAttribute("secondNewImage", newProduct.getSub_images().get(1).getSub_image_url());
                model.addAttribute("thirdNewImage", newProduct.getSub_images().get(2).getSub_image_url());
            } else {
                throw new IllegalStateException("Sản phẩm phải có ít nhất 3 hình ảnh phụ");
            }
            
            return "pages/manager/product-request-details";
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý yêu cầu sản phẩm: " + e.getMessage(), e);
        }
    }

    @PostMapping("/approve-product-request")
    public String approveProductRequest(
            @RequestParam Long requestId,
            RedirectAttributes redirectAttributes) {
        try {
            SellerRequest sellerRequest = sellerRequestService.getSellerRequestById(requestId);
            if (sellerRequest == null) {
                throw new Exception("Yêu cầu không tồn tại");
            }

            Product newProduct = sellerRequestService.getEntityFromContent(sellerRequest.getContent(), Product.class);
            if (newProduct == null) {
                throw new Exception("Dữ liệu sản phẩm không hợp lệ");
            }
            if (sellerRequestTypeService.isUpdateType(sellerRequest)) {
                Product oldProduct = sellerRequestService.getEntityFromContent(sellerRequest.getOldContent(),
                        Product.class);
                CompletableFuture<String> mainImageFuture = imageService.convertFromDisplayPathToBase64(oldProduct.getMain_image_url());
                CompletableFuture<String> firstSubImageFuture = imageService
                        .convertFromDisplayPathToBase64(oldProduct.getSub_images().get(0).getSub_image_url());
                CompletableFuture<String> secondSubImageFuture = imageService
                        .convertFromDisplayPathToBase64(oldProduct.getSub_images().get(1).getSub_image_url());
                CompletableFuture<String> thirdSubImageFuture = imageService
                        .convertFromDisplayPathToBase64(oldProduct.getSub_images().get(2).getSub_image_url());

                CompletableFuture.allOf(mainImageFuture, firstSubImageFuture, secondSubImageFuture, thirdSubImageFuture).join();

                String mainImageUrl = mainImageFuture.get();
                String firstSubImage = firstSubImageFuture.get();
                String secondSubImage = secondSubImageFuture.get();
                String thirdSubImage = thirdSubImageFuture.get();

                oldProduct.setMain_image_url(mainImageUrl);
                oldProduct.getSub_images().get(0).setSub_image_url(firstSubImage);
                oldProduct.getSub_images().get(1).setSub_image_url(secondSubImage);
                oldProduct.getSub_images().get(2).setSub_image_url(thirdSubImage);
                sellerRequestService.updateOldContent(oldProduct, sellerRequest);
            }

            sellerRequestService.approveRequest(requestId, Product.class,
                    productService::add,
                    t -> {
                        try {
                            productService.update(t);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            redirectAttributes.addFlashAttribute("msg", "Đã duyệt yêu cầu thành công");
        } catch (Exception e) {
            System.out.println("Exception" + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/all-products-request";
    }

    @PostMapping("/reject-product-request")
    public String rejectProductRequest(
            @RequestParam Long requestId,
            RedirectAttributes redirectAttributes) {
        try {
            SellerRequest sellerRequest = sellerRequestService.getSellerRequestById(requestId);
            if (sellerRequest == null) {
                throw new Exception("Yêu cầu không tồn tại");
            }

            // Process the rejection logic here
            sellerRequestService.rejectRequest(requestId);

            redirectAttributes.addFlashAttribute("msg", "Đã từ chối yêu cầu");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/all-products-request";
    }

    @PostMapping("/approve-product-unit-request")
    public String approveProductUnitRequest(
            @RequestParam Long requestId,
            RedirectAttributes redirectAttributes) {
        try {
            SellerRequest sellerRequest = sellerRequestService.getSellerRequestById(requestId);
            if (sellerRequest == null) {
                throw new Exception("Yêu cầu không tồn tại");
            }

            ProductUnit newProductUnit = sellerRequestService.getEntityFromContent(sellerRequest.getContent(), ProductUnit.class);
            if (newProductUnit == null) {
                throw new Exception("Dữ liệu đơn vị sản phẩm không hợp lệ");
            }
            if(sellerRequestTypeService.isDeleteType(sellerRequest)) {
                sellerRequestService.approveDeleteRequest(requestId, ProductUnit.class, productUnitService::delete);
            }else{
                sellerRequestService.approveRequest(requestId, ProductUnit.class,
                        productUnitService::add,
                        t -> {
                            try {
                                productUnitService.update(t);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
            redirectAttributes.addFlashAttribute("msg", "Đã duyệt yêu cầu đơn vị sản phẩm thành công");
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/all-products-request";
    }

    @PostMapping("/reject-product-unit-request")
    public String rejectProductUnitRequest(
            @RequestParam Long requestId,
            RedirectAttributes redirectAttributes) {
        try {
            SellerRequest sellerRequest = sellerRequestService.getSellerRequestById(requestId);
            if (sellerRequest == null) {
                throw new Exception("Yêu cầu không tồn tại");
            }

            sellerRequestService.rejectRequest(requestId);

            redirectAttributes.addFlashAttribute("msg", "Đã từ chối yêu cầu đơn vị sản phẩm");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/all-products-request";
    }

    @PostMapping("/approve-category-request")
    public String approveCategoryRequest(
            @RequestParam Long requestId,
            RedirectAttributes redirectAttributes) {
        try {
            SellerRequest sellerRequest = sellerRequestService.getSellerRequestById(requestId);
            if (sellerRequest == null) {
                throw new Exception("Yêu cầu không tồn tại");
            }

            Category newCategory = sellerRequestService.getEntityFromContent(sellerRequest.getContent(), Category.class);
            if (newCategory == null) {
                throw new Exception("Dữ liệu danh mục không hợp lệ");
            }
            if(sellerRequestTypeService.isDeleteType(sellerRequest)) {
                sellerRequestService.approveDeleteRequest(requestId, Category.class, categoryService::delete);
            }else{
                sellerRequestService.approveRequest(requestId, Category.class,
                        categoryService::add,
                        t -> {
                            try {
                                categoryService.update(t);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
            redirectAttributes.addFlashAttribute("msg", "Đã duyệt yêu cầu danh mục thành công");
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/all-products-request";
    }

    @PostMapping("/reject-category-request")
    public String rejectCategoryRequest(
            @RequestParam Long requestId,
            RedirectAttributes redirectAttributes) {
        try {
            SellerRequest sellerRequest = sellerRequestService.getSellerRequestById(requestId);
            if (sellerRequest == null) {
                throw new Exception("Yêu cầu không tồn tại");
            }

            sellerRequestService.rejectRequest(requestId);

            redirectAttributes.addFlashAttribute("msg", "Đã từ chối yêu cầu danh mục");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/all-products-request";
    }

    @GetMapping("/bill-list")
    public String getBills( @RequestParam(value = "sortCriteria", required = false) String sortCriteria,
                            @RequestParam(value = "subpageIndex", required = false, defaultValue= "1") Integer subpageIndex,
                            @RequestParam(value = "queryName", required = false) String queryName,
                            @RequestParam(value = "queryShopName", required = false) String queryShopName,
                            @RequestParam(value = "queryAddress", required = false) String queryAddress,
                            @RequestParam(value = "fromDate", required = false) String fromDate,
                            @RequestParam(value = "toDate", required = false) String toDate,
                            @RequestParam(value = "minAmount", required = false) String minAmount,
                            @RequestParam(value = "maxAmount", required = false) String maxAmount,
                            @RequestParam(value = "sortCriteriaInPage", required = false) String sortCriteriaInPage,
                            HttpSession session,
                            Model model) {
        final int numEachPage = 10;
        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (session.getAttribute("sortCriteriaInPage") == null) {
            session.setAttribute("sortCriteriaInPage", "id");
        }
        if (session.getAttribute("subpageIndex") == null) {
            session.setAttribute("subpageIndex", 1);
        }
        if (session.getAttribute("numEachPage") == null) {
            session.setAttribute("numEachPage", numEachPage);
        }
        if (session.getAttribute("queryName") == null) session.setAttribute("queryName", "");
        if (session.getAttribute("queryShopName") == null) session.setAttribute("queryShopName", "");
        if (session.getAttribute("queryAddress") == null) session.setAttribute("queryAddress", "");
        if (session.getAttribute("fromDate") == null) session.setAttribute("fromDate", "");
        if (session.getAttribute("toDate") == null) session.setAttribute("toDate", "");
        if (session.getAttribute("minAmount") == null) session.setAttribute("minAmount", "");
        if (session.getAttribute("maxAmount") == null) session.setAttribute("maxAmount", "");
        if (session.getAttribute("queryCid") == null) session.setAttribute("queryCid", "");

        if (queryName != null) { session.setAttribute("queryName", queryName); session.setAttribute("subpageIndex", 1); }
        if (queryShopName != null) { session.setAttribute("queryShopName", queryShopName); session.setAttribute("subpageIndex", 1); }
        if (queryAddress != null) { session.setAttribute("queryAddress", queryAddress); session.setAttribute("subpageIndex", 1); }
        if (fromDate != null) { session.setAttribute("fromDate", fromDate); session.setAttribute("subpageIndex", 1); }
        if (toDate != null) { session.setAttribute("toDate", toDate); session.setAttribute("subpageIndex", 1); }
        if (minAmount != null) { session.setAttribute("minAmount", minAmount); session.setAttribute("subpageIndex", 1); }
        if (maxAmount != null) { session.setAttribute("maxAmount", maxAmount); session.setAttribute("subpageIndex", 1); }
        if (sortCriteria != null && !sortCriteria.isEmpty()) {
            session.setAttribute("sortCriteria", sortCriteria);
        }
        if (sortCriteriaInPage != null && !sortCriteriaInPage.isEmpty()) {
            session.setAttribute("sortCriteriaInPage", sortCriteriaInPage);
            int k = (int) session.getAttribute("k");
            k = -k;
            session.setAttribute("k", k);
        }
        if (subpageIndex != null) {
            session.setAttribute("subpageIndex", subpageIndex);
        }


    Page<Bill> bills = billService.getBills(
        (Integer) session.getAttribute("subpageIndex"),
        numEachPage,
        session.getAttribute("queryName").toString(),
        session.getAttribute("queryShopName").toString(),
        session.getAttribute("queryAddress").toString(),
        session.getAttribute("fromDate").toString(),
        session.getAttribute("toDate").toString(),
        session.getAttribute("minAmount").toString(),
        session.getAttribute("maxAmount").toString(),
        (String) session.getAttribute("sortCriteria"),
        (Integer) session.getAttribute("k"),
        (String) session.getAttribute("sortCriteriaInPage"));

        model.addAttribute("k", session.getAttribute("k"));
        model.addAttribute("bills", bills.getContent());
        model.addAttribute("subpageIndex", session.getAttribute("subpageIndex"));
        model.addAttribute("numEachPage", numEachPage);
        model.addAttribute("sortCriteria", session.getAttribute("sortCriteria"));
        model.addAttribute("sortCriteriaInPage", session.getAttribute("sortCriteriaInPage"));
        model.addAttribute("queryName", session.getAttribute("queryName"));
    model.addAttribute("totalPages", bills.getTotalPages());
    model.addAttribute("billService", billService);
    // Add new search fields to model for form repopulation
    model.addAttribute("queryShopName", session.getAttribute("queryShopName"));
    model.addAttribute("queryAddress", session.getAttribute("queryAddress"));
    model.addAttribute("fromDate", session.getAttribute("fromDate"));
    model.addAttribute("toDate", session.getAttribute("toDate"));
    model.addAttribute("minAmount", session.getAttribute("minAmount"));
    model.addAttribute("maxAmount", session.getAttribute("maxAmount"));
    return "pages/manager/bill-list";
    }

    @GetMapping("/orders/{billId}")
    public String getOrdersByBillId(@PathVariable Long billId, Model model) {
        Bill bill = billService.getBillById(billId);
        if (bill == null) {
            model.addAttribute("error", "Hóa đơn không tồn tại");
            return "pages/manager/bill-list";
        }
        // Order order = bill.getOrder();
        Long totalAmount = orderService.calculateTotalAmount(bill.getOrder());
        model.addAttribute("bill", bill);
        model.addAttribute("shippedAt", orderService.getShippedAt(bill.getOrder()));
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("orderService", orderService);
        return "pages/manager/order-details";
    }

    @GetMapping("/days/export-excel")
    public ResponseEntity<InputStreamResource> exportDaysRevenueToExcel() throws IOException {
        ByteArrayInputStream in = orderService.exportDaysRevenueToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=revenue-7-days.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/months/export-excel")
    public ResponseEntity<InputStreamResource> exportMonthsRevenueToExcel() throws IOException {
        ByteArrayInputStream in = orderService.exportMonthsRevenueToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=revenue-12-months.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

}