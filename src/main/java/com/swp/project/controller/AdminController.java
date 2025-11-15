package com.swp.project.controller;

import com.swp.project.dto.*;
import com.swp.project.entity.user.Manager;
import com.swp.project.service.AddressService;
import com.swp.project.service.AiService;
import com.swp.project.service.SettingService;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.user.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ManagerService managerService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final SettingService settingService;
    private final AiService aiService;

    @GetMapping("")
    public String showAdminMainPage(@RequestParam(defaultValue = "day") String timeSelected,
                                    Model model) {
        Long totalUnitSold = orderService.getUnitSold();
        Long revenueToday = orderService.getRevenueToday();
        Long revenueThisWeek = orderService.getRevenueThisWeek();
        Long revenueThisMonth = orderService.getRevenueThisMonth();
        double dailyPercentageChange = orderService.getDailyPercentageChange();
        double weeklyPercentageChange = orderService.getWeeklyPercentageChange();
        double monthlyPercentageChange = orderService.getMonthlyPercentageChange();
        List<RevenueDto> daysReport = orderService.getDaysRevenue();
        List<RevenueDto> monthsReport = orderService.getMonthsRevenue();
        String label  = "Doanh thu hôm nay";
        long amount   = revenueToday == null ? 0 : revenueToday;
        double change = dailyPercentageChange;
        switch(timeSelected) {
            case "week":
                label = "Doanh thu tuần này";
                amount = revenueThisWeek == null ? 0 : revenueThisWeek;
                change = weeklyPercentageChange;
                break;
            case "month":
                label = "Doanh thu tháng này";
                amount = revenueThisMonth == null ? 0 : revenueThisMonth;
                change = monthlyPercentageChange;
                break;
            case "day":
            default:

                break;
        }
        model.addAttribute("timeSelected", timeSelected);
        model.addAttribute("label", label);
        model.addAttribute("amount", amount);
        model.addAttribute("change", change);
        model.addAttribute("daysReport", daysReport);
        model.addAttribute("monthsReport", monthsReport);
        model.addAttribute("totalOrder",orderService.getTotalOrders());
        model.addAttribute("deliverOrder",orderService.getTotalDeliveredOrders());
        model.addAttribute("processingOrder",orderService.getTotalProcessingOrders());
        model.addAttribute("pendingOrder",orderService.getTotalPendingOrders());
        model.addAttribute("shippingOrder",orderService.getTotalShippingOrders());
        model.addAttribute("totalCanceledOrder", orderService.getTotalCancelledOrders());
        return "pages/admin/index";
    }

    @GetMapping("/create-manager")
    public String getCreateManagerPage(Model model) {
        model.addAttribute("managerRegisterDto", new CreateManagerDto());
        return "pages/admin/create-manager";
    }

    @GetMapping("/edit-manager/{id}")
    public String getEditManagerPage(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        Manager manager = managerService.getManagerById(id);
        if (manager == null) {
            redirectAttributes.addFlashAttribute("failed", "Không tìm thấy quản lý.");
            return "redirect:/admin/manage-manager";
        }
        EditManagerDto editManagerDto = new EditManagerDto(manager);
        model.addAttribute("editManagerDto", editManagerDto);
        return "pages/admin/edit-manager";
    }

    @GetMapping("/manage-manager")
    public String showManageManagersPage(
            Model model) {
        List<ViewManagerDto> managers = managerService.getAllViewManager();
        model.addAttribute("managers", managers);
        return "pages/admin/manage-manager";
    }

    @PostMapping("/create-manager")
    public String createManager(
            @Valid @ModelAttribute("managerRegisterDto") CreateManagerDto managerRegisterDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "pages/admin/create-manager";
        }
        try {
            managerService.createManager(managerRegisterDto);
            redirectAttributes.addFlashAttribute("success", "Tạo quản lý thành công.");
        } catch (RuntimeException e) {
            model.addAttribute("failed", e.getMessage());
            return "pages/admin/create-manager";
        }
        return "redirect:/admin/manage-manager";
    }

    @PostMapping("/edit-manager/{id}")
    public String editManager(@PathVariable Long id,
                              @Valid @ModelAttribute EditManagerDto editManagerDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "pages/admin/edit-manager";
        }

        try {
            managerService.updateManager(id, editManagerDto);
        } catch (RuntimeException e) {
            // nếu muốn vẫn có lỗi chung
            model.addAttribute("failed", e.getMessage());
            return "pages/admin/edit-manager";
        }

        redirectAttributes.addFlashAttribute("success", "Cập nhật quản lý thành công.");
        return "redirect:/admin/manage-manager";
    }





    @GetMapping("/provinces")
    @ResponseBody
    public List<ProvinceDto> getAllProvinceCities() {
        return addressService.getAllProvinceCity().stream()
                .map(pc -> {
                    ProvinceDto dto = new ProvinceDto();
                    dto.setCode(pc.getCode());
                    dto.setName(pc.getName());
                    return dto;
                })
                .toList();
    }

    @GetMapping("/wards{provinceId}")
    @ResponseBody
    public List<WardDto> getAllSelectedWard(@RequestParam String provinceId) {
        return addressService.getAllCommuneWardByProvinceCityCode(provinceId).stream()
                .map(ward -> {
                    WardDto dto = new WardDto();
                    dto.setCode(ward.getCode());
                    dto.setName(ward.getName());
                    return dto;
                })
                .toList();
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

    @GetMapping("/setting")
    public String getAdminSettingPage(Model model) {
        model.addAttribute("shopName", settingService.getShopName());
        model.addAttribute("shopAddress", settingService.getShopAddress());
        model.addAttribute("shopPhone", settingService.getShopPhone());
        model.addAttribute("shopEmail", settingService.getShopEmail());
        model.addAttribute("shopSlogan", settingService.getShopSlogan());
        model.addAttribute("shopFacebook", settingService.getShopFacebook());
        model.addAttribute("shopInstagram", settingService.getShopInstagram());
        return "pages/admin/setting";
    }

    @PostMapping("/updateSetting")
    public String updateAdminSetting(@Valid @ModelAttribute AdminSettingDto adminSettingDto,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("failed", "Cập nhật cài đặt thất bại. Vui lòng kiểm tra lại dữ liệu.");
            return "redirect:/admin/setting";
        }
        settingService.updateSetting(adminSettingDto);
        redirectAttributes.addFlashAttribute("success", "Cập nhật cài đặt thành công.");
        return "redirect:/admin/setting";
    }


    @PostMapping("/synchronize-vector-database")
    public String synchronizeVectorDatabase(RedirectAttributes redirectAttributes) {
        CompletableFuture.runAsync(aiService::synchronizeVectorStoreWithAllProductInDatabase);
        redirectAttributes.addFlashAttribute("msg", "Yêu cầu đồng bộ cơ sở dữ liệu AI với thông tin sản phẩm trong hệ thống đang được thực thi, có thể mất vài phút để hoàn thành");
        return "redirect:/admin";
    }

}
