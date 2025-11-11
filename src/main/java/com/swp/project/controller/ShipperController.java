package com.swp.project.controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.swp.project.entity.order.Order;
import com.swp.project.service.order.OrderService;
import com.swp.project.service.order.OrderStatusService;
import com.swp.project.service.order.PaymentMethodService;
import com.swp.project.service.order.shipping.ShippingStatusService;
import com.swp.project.service.user.ShipperService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
@Controller
@RequestMapping("/shipper")
public class ShipperController {

    private final ShipperService shipperService;
    private final OrderService orderService;
    private final ShippingStatusService shippingStatusService;
    private final OrderStatusService orderStatusService;
    private final PaymentMethodService paymentMethodService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Allow empty date values ("") to be converted to null instead of causing a bind error
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    private String formatDateForInput(Date date) {
        if (date == null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }

    @GetMapping("")
    public String shipperMain(Model model, Principal principal) {
        try {
            model.addAttribute("orderService", orderService);
            model.addAttribute("principal", principal);
            orderService.loadDoneOrders(principal);
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "pages/shipper/index";
    }

    @GetMapping("/delivering-orders")
    public String shipperDeliveringOrders(Model model,
                                Principal principal,
                                @RequestParam(defaultValue = "1") int pageDelivering,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String sortCriteria,
                                @RequestParam(required = false) String searchQuery,
                                @RequestParam(required = false) String sortCriteriaInPage,
                                HttpSession session) {
        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (sortCriteria != null) {
            session.setAttribute("sortCriteria", sortCriteria);
        }
        if (session.getAttribute("sortCriteriaInPage") == null) {
            session.setAttribute("sortCriteriaInPage", "id");
        }
        if (sortCriteriaInPage != null) {
            session.setAttribute("sortCriteriaInPage", sortCriteriaInPage);
        }
        if (sortCriteriaInPage != null) {
            int k = (int) session.getAttribute("k");
            k = k * -1; // Đảo chiều sắp xếp
            session.setAttribute("k", k);
        }
        if (searchQuery != null) {
            pageDelivering = 1; // Reset về trang đầu tiên khi có tìm kiếm mới
            session.setAttribute("searchQuery", searchQuery);
        }
        if (pageDelivering < 1) {
            pageDelivering = 1;
        }
        try {
            // Lấy Page<Order> thay vì List<Order>
            Page<Order> deliveringOrders = orderService.getDeliveringOrders(principal, 
                                                                            pageDelivering, 
                                                                            size, 
                                                                            (String) session.getAttribute("searchQuery"), 
                                                                            (String) session.getAttribute("sortCriteria"), 
                                                                            (int) session.getAttribute("k"),
                                                                            (String) session.getAttribute("sortCriteriaInPage"));
            model.addAttribute("deliveringOrders", deliveringOrders.getContent());
            model.addAttribute("shippingStatusService", shippingStatusService);
            model.addAttribute("currentPageDelivering", pageDelivering);
            model.addAttribute("totalPagesDelivering", deliveringOrders.getTotalPages());
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("shippingStatusService", shippingStatusService);
        model.addAttribute("searchQuery", session.getAttribute("searchQuery"));
        model.addAttribute("sortCriteria", session.getAttribute("sortCriteria"));
        model.addAttribute("k", (int) session.getAttribute("k"));
        model.addAttribute("sortCriteriaInPage", session.getAttribute("sortCriteriaInPage"));
        model.addAttribute("orderNumber", orderService.countDeliveringOrders(principal));
        return "pages/shipper/delivering-orders";
    }

    @GetMapping("/done-orders")
    public String shipperDoneOrders(Model model,
                                Principal principal,
                                @RequestParam(defaultValue = "1") int pageDone,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String sortCriteria,
                                @RequestParam(required = false) String searchQuery,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date completionDateFromQuery,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date completionDateToQuery,
                                @RequestParam(required = false) String sortCriteriaInPage,
                                HttpSession session) {

        if (session.getAttribute("k") == null) {
            session.setAttribute("k", 1);
        }
        if (session.getAttribute("sortCriteria") == null) {
            session.setAttribute("sortCriteria", "id");
        }
        if (sortCriteria != null) {
            session.setAttribute("sortCriteria", sortCriteria);
        }
        if (session.getAttribute("sortCriteriaInPage") == null) {
            session.setAttribute("sortCriteriaInPage", "id");
        }
        if (sortCriteriaInPage != null) {
            session.setAttribute("sortCriteriaInPage", sortCriteriaInPage);
            int k = (int) session.getAttribute("k");
            k = k * -1;
            session.setAttribute("k", k);
        }
        if (searchQuery != null) {
            pageDone = 1;
            session.setAttribute("searchQuery", searchQuery);
        }
        if (completionDateFromQuery != null) {
            session.setAttribute("completionDateFromQuery", completionDateFromQuery);
        }
        if (completionDateToQuery != null) {
            session.setAttribute("completionDateToQuery", completionDateToQuery);
        }
        if (pageDone < 1) {
            pageDone = 1;
        }
        try {
            Page<Order> doneOrders = orderService.getDoneOrders(principal,
                                                            pageDone,
                                                            size,
                                                            (String) session.getAttribute("searchQuery"),
                                                            (Date) session.getAttribute("completionDateFromQuery"),
                                                            (Date) session.getAttribute("completionDateToQuery"),
                                                            (String) session.getAttribute("sortCriteria"),
                                                            (int) session.getAttribute("k"),
                                                            (String) session.getAttribute("sortCriteriaInPage"));
            model.addAttribute("doneOrders", doneOrders.getContent());
            model.addAttribute("currentPageDone", pageDone);
            model.addAttribute("totalPagesDone", doneOrders.getTotalPages());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("searchQuery", session.getAttribute("searchQuery"));
        model.addAttribute("completionDateFromQuery", formatDateForInput((Date) session.getAttribute("completionDateFromQuery")));
        model.addAttribute("completionDateToQuery", formatDateForInput((Date) session.getAttribute("completionDateToQuery")));
        model.addAttribute("sortCriteria", session.getAttribute("sortCriteria"));
        model.addAttribute("k", (int) session.getAttribute("k"));
        model.addAttribute("sortCriteriaInPage", session.getAttribute("sortCriteriaInPage"));
        model.addAttribute("orderNumber", orderService.countDoneOrders(principal));
        model.addAttribute("orderService", orderService);
        return "pages/shipper/done-orders";
    }

    @PostMapping("/mark/{orderId}")
    public String markOrder(@PathVariable Long orderId,
                            @RequestParam(required = false) String nextStatus,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        try {
            // ShippingStatus shippingStatus = orderService.getOrderByOrderId(orderId).getCurrentShippingStatus();
            switch (nextStatus) {
                case "awaitingPickup":
                    orderService.markOrderShippingStatusAsAwaitingPickup(orderId, principal);
                    redirectAttributes.addFlashAttribute("msg", "Đơn hàng " + orderId + " đã được đánh dấu là đang chờ lấy hàng.");
                    break;
                case "pickedUp":
                    orderService.markOrderShippingStatusAsPickedUp(orderId, principal);
                    redirectAttributes.addFlashAttribute("msg", "Đơn hàng " + orderId + " đã được đánh dấu là đã lấy hàng.");
                    break;
                case "shipping":
                    orderService.markOrderShippingStatusAsShipping(orderId, principal);
                    redirectAttributes.addFlashAttribute("msg", "Đơn hàng " + orderId + " đã được đánh dấu là đang giao hàng.");
                    break;
                case "delivered":
                    orderService.markOrderStatusAsDelivered(orderId, principal);
                    redirectAttributes.addFlashAttribute("msg", "Đơn hàng " + orderId + " đã được đánh dấu là hoàn thành.");
                    break;
                default:
                    throw new Exception("Trạng thái đơn hàng không hợp lệ để thực hiện hành động này.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shipper/delivering-orders";
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderByOrderId(orderId);

            if (order == null) {
                throw new Exception("Đơn hàng không tồn tại.");
            }

            Long totalAmount = 0L;
            if (paymentMethodService.isCodMethod(order.getPaymentMethod())) {
                totalAmount = order.getTotalAmount(); // Chỉ tính tổng nếu phương thức thanh toán là COD
            }
            model.addAttribute("orderStatusService", shipperService.getOrderStatusService());
            model.addAttribute("shippingStatusService", shippingStatusService);
            model.addAttribute("order", order);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("shippedAt", orderService.getShippedAt(order));
            model.addAttribute("returnUrl", orderStatusService.isDeliveredStatus(order) ? "/shipper/done-orders" : "/shipper/delivering-orders");
            return "pages/shipper/order-details";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/shipper";
        }
    }
    
}
