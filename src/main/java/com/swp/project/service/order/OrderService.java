package com.swp.project.service.order;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.project.dto.DeliveryInfoDto;
import com.swp.project.dto.RevenueDto;
import com.swp.project.dto.SellerSearchOrderDto;
import com.swp.project.entity.order.Bill;
import com.swp.project.entity.order.Order;
import com.swp.project.entity.order.OrderItem;
import com.swp.project.entity.order.OrderStatus;
import com.swp.project.entity.order.shipping.Shipping;
import com.swp.project.entity.product.Product;
import com.swp.project.entity.shopping_cart.ShoppingCartItem;
import com.swp.project.repository.order.BillRepository;
import com.swp.project.repository.order.OrderRepository;
import com.swp.project.repository.product.ProductRepository;
import com.swp.project.repository.shopping_cart.ShoppingCartItemRepository;
import com.swp.project.repository.user.CustomerRepository;
import com.swp.project.service.AddressService;
import com.swp.project.service.SettingService;
import com.swp.project.service.order.shipping.ShippingStatusService;
import com.swp.project.service.product.ProductService;
import com.swp.project.service.user.ShipperService;

import lombok.RequiredArgsConstructor;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductService productService;
    private final OrderStatusService orderStatusService;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final AddressService addressService;
    private final PaymentMethodService paymentMethodService;
    private final ShippingStatusService shippingStatusService;
    private final SettingService settingService;
    private final BillRepository billRepository;
    private final ProductRepository productRepository;
    private final ShipperService shipperService;
    private final PayOS payOS;

    private List<Order> results = List.of();

    public Page<Order> getAllOrder() {
        Pageable pageable = PageRequest.of(0,5, Sort.by("orderAt").descending());
        return orderRepository.findAll(pageable);
    }

    public Page<Order> searchOrder(SellerSearchOrderDto sellerSearchOrderDto) {
        Sort sortBy = switch (sellerSearchOrderDto.getSortBy()) {
            case "orderDateNewest" -> Sort.by("orderAt").descending();
            case "orderDateOldest" -> Sort.by("orderAt").ascending();
            case "emailZA" -> Sort.by("customer.email").descending();
            case "emailAZ" -> Sort.by("customer.email").ascending();
            case "orderCodeAsc" -> Sort.by("id").ascending();
            default -> Sort.by("id").descending();
        };
        Pageable pageable = PageRequest.of(
                Integer.parseInt(sellerSearchOrderDto.getGoToPage()) - 1,
                5, sortBy);
        if (sellerSearchOrderDto.getStatusId() == null || sellerSearchOrderDto.getStatusId() == 0) {
            return orderRepository.searchByCustomer_EmailContainsAndOrderAtBetween(
                    sellerSearchOrderDto.getCustomerEmail() == null
                            ? ""
                            : sellerSearchOrderDto.getCustomerEmail(),
                    sellerSearchOrderDto.getFromDate() == null
                            ? LocalDate.parse("2005-06-03").atStartOfDay()
                            : sellerSearchOrderDto.getFromDate().atStartOfDay(),
                    sellerSearchOrderDto.getToDate() == null
                            ? LocalDateTime.now()
                            : sellerSearchOrderDto.getToDate().atTime(23,59),
                    pageable);
        }
        return orderRepository.searchByOrderStatus_IdAndCustomer_EmailContainsAndOrderAtBetween(
                sellerSearchOrderDto.getStatusId(),
                sellerSearchOrderDto.getCustomerEmail() == null
                        ? ""
                        : sellerSearchOrderDto.getCustomerEmail(),
                sellerSearchOrderDto.getFromDate() == null
                        ? LocalDate.parse("2005-06-03").atStartOfDay()
                        : sellerSearchOrderDto.getFromDate().atStartOfDay(),
                sellerSearchOrderDto.getToDate() == null
                        ? LocalDateTime.now()
                        : sellerSearchOrderDto.getToDate().atTime(23,59),
                pageable);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    @Transactional
    public void setOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = getOrderById(orderId);
        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void createCodOrder(String customerEmail,
                                List<ShoppingCartItem> shoppingCartItems,
                                DeliveryInfoDto deliveryInfoDto) {

        if(shoppingCartItems.stream().anyMatch(i ->
                i.getQuantity() > i.getProduct().getQuantity())) {
            throw new RuntimeException("Số lượng sản phẩm trong đơn hàng vượt quá số lượng khả dụng");
        }

        shoppingCartItems.forEach(i -> shoppingCartItemRepository
                .deleteByCustomerEmailAndProductId(customerEmail, i.getProduct().getId()));

        Order order = orderRepository.save(Order.builder()
                .paymentMethod(paymentMethodService.getCodMethod())
                .orderStatus(orderStatusService.getPendingConfirmationStatus())
                .fullName(deliveryInfoDto.getFullName())
                .phoneNumber(deliveryInfoDto.getPhone())
                .communeWard(addressService.getCommuneWardByCode(deliveryInfoDto.getCommuneWardCode()))
                .specificAddress(deliveryInfoDto.getSpecificAddress())
                .customer(customerRepository.getByEmail(customerEmail))
                .build());
        List<OrderItem> orderItems = shoppingCartItems.stream().map(cartItem ->
                        OrderItem.builder()
                                .order(order)
                                .product(cartItem.getProduct())
                                .quantity(cartItem.getQuantity())
                                .price(cartItem.getProduct().getPrice())
                                .build()).collect(Collectors.toList());
        order.setOrderItem(orderItems);
        orderRepository.save(order);
    }

    @Transactional
    public Order createQrOrder(String customerEmail,
                                List<ShoppingCartItem> shoppingCartItems,
                                DeliveryInfoDto deliveryInfoDto) {

        if(shoppingCartItems.stream().anyMatch(i ->
                i.getQuantity() > i.getProduct().getQuantity())) {
            throw new RuntimeException("Số lượng sản phẩm trong đơn hàng vượt quá số lượng khả dụng");
        }

        shoppingCartItems.forEach(i -> shoppingCartItemRepository
                .deleteByCustomerEmailAndProductId(customerEmail, i.getProduct().getId()));

        Order order = orderRepository.save(Order.builder()
                .paymentMethod(paymentMethodService.getQrMethod())
                .paymentExpiredAt(LocalDateTime.now().plusMinutes(2)) // QR expires in 2 minutes
                .orderStatus(orderStatusService.getPendingPaymentStatus())
                .fullName(deliveryInfoDto.getFullName())
                .phoneNumber(deliveryInfoDto.getPhone())
                .communeWard(addressService.getCommuneWardByCode(deliveryInfoDto.getCommuneWardCode()))
                .specificAddress(deliveryInfoDto.getSpecificAddress())
                .customer(customerRepository.getByEmail(customerEmail))
                .build());

        List<OrderItem> orderItems = new ArrayList<>();
        shoppingCartItems.forEach(item -> {
                    productService.holdProductQuantity(item.getProduct().getId(), item.getQuantity());
                    orderItems.add(OrderItem.builder()
                            .order(order)
                            .product(item.getProduct())
                            .quantity(item.getQuantity())
                            .price(item.getProduct().getPrice())
                            .build());
                });
        order.setOrderItem(orderItems);

        CreatePaymentLinkRequest paymentData =
                CreatePaymentLinkRequest.builder()
                        .orderCode(order.getId())
                        .amount(order.getTotalAmount())
                        .expiredAt(order.getPaymentExpiredAt().atZone(ZoneId.systemDefault()).toEpochSecond())
                        .description("FS" + order.getId())
                        .returnUrl("https://fruitshop.tech/customer/order-success")
                        .cancelUrl("https://fruitshop.tech/customer/order-cancel")
                        .build();
        order.setPaymentLink(payOS.paymentRequests().create(paymentData).getCheckoutUrl());

        return orderRepository.save(order);
    }

    @Scheduled(fixedRate = 60000) // cancel expired qr orders every 1 minutes
    @Transactional
    public void cancelExpiredQrOrders() {
        List<Order> expiredOrders = orderRepository.findByOrderStatusAndPaymentExpiredAtBefore(
                orderStatusService.getPendingPaymentStatus(), LocalDateTime.now());
        expiredOrders.forEach(order -> {
            order.getOrderItem().forEach(item ->
                    productService.releaseProductQuantity(item.getProduct().getId(), item.getQuantity()));
            order.setOrderStatus(orderStatusService.getCancelledStatus());
        });
        orderRepository.saveAll(expiredOrders);
    }

    public boolean isOrderItemQuantityMoreThanAvailable(Long orderId) {
        return getOrderById(orderId).getOrderItem().stream().anyMatch(i ->
                i.getQuantity() > i.getProduct().getQuantity());
    }

    @Transactional
    public void doWhenCodOrderConfirmed(Order order) {
        setOrderStatus(order.getId(), orderStatusService.getProcessingStatus());

        order.getOrderItem().forEach(item ->
                productService.reduceProductQuantity(item.getProduct().getId(), item.getQuantity()));
    }

    @Transactional
    public void doWhenQrOrderConfirmed(Order order) {
        setOrderStatus(order.getId(), orderStatusService.getProcessingStatus());

        order.getOrderItem().forEach(item -> {
            productService.releaseProductQuantity(item.getProduct().getId(), item.getQuantity());
            productService.reduceProductQuantity(item.getProduct().getId(), item.getQuantity());
        });
    }

    public void createBillForOrder(Order order) {
        Bill bill = Bill.builder()
                .paymentTime(LocalDateTime.now())
                .shopName(settingService.getShopName())
                .shopAddress(settingService.getShopAddress())
                .shopPhone(settingService.getShopPhone())
                .shopEmail(settingService.getShopEmail())
                .order(order)
                .build();
                billRepository.save(bill);
    }

    public List<Order> getSuccessOrder() {
        return orderRepository.findAll().stream()
                .filter(orderStatusService::isDeliveredStatus)
                .collect(Collectors.toList());
    }

    public List<Order> getOrderByProductId(List<Order> orders, Long productId) {
        return orders.stream()
                .filter(order -> order.getOrderItem().stream()
                        .anyMatch(item -> item.getProduct().getId().equals(productId)))
                .collect(Collectors.toList());
    }
        public double getSoldQuantity(Long productId) {
                List<Order> orders = getOrderByProductId(getSuccessOrder(), productId);
                return orders.stream()
                        .mapToDouble(order -> order.getOrderItem().stream()
                                .filter(item -> item.getProduct().getId().equals(productId))
                                .mapToDouble(OrderItem::getQuantity)
                                .sum())
                        .sum();
        }

    public Order getOrderByOrderId(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() ->
                new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
    }

    public long getTotalOrders(){
        return orderRepository.count();
    }
    public long getTotalDeliveredOrders(){
        return orderRepository.findAll().stream()
                .filter(orderStatusService::isDeliveredStatus)
                .count();
    }

    public long getTotalProcessingOrders(){
        return orderRepository.findAll().stream()
                .filter(orderStatusService::isProcessingStatus)
                .count();
    }

    public long getTotalPendingOrders(){
        return orderRepository.findAll().stream()
                .filter(order -> orderStatusService.isPendingConfirmationStatus(order)
                        || orderStatusService.isPendingPaymentStatus(order))
                .count();
    }

    public long getTotalCancelledOrders(){
        return orderRepository.findAll().stream()
                .filter(orderStatusService::isCancelledStatus)
                .count();
    }
    public long getTotalShippingOrders(){
        return orderRepository.findAll().stream()
                .filter(orderStatusService::isShippingStatus)
                .count();
    }

    public long getUnitSold(){
        long total =0;
        for(Product p : productRepository.findAll()){
            if(p.getSoldQuantity() != null)
                total += (long) getSoldQuantity(p.getId());
        }
        return total;
    }

    public LocalDateTime startOfDay(LocalDate date){
        return date.atStartOfDay();
    }
    public LocalDateTime nextDay(LocalDate date){
        return date.plusDays(1).atStartOfDay();
    }

    public long getRevenueToday() {
        LocalDate today = LocalDate.now();
        return orderRepository.getRevenueBetween(startOfDay(today),nextDay(today));
    }
    public long getRevenueThisMonth(){
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);
        return orderRepository.getRevenueBetween(startOfDay(start),nextDay(end));

    }
    public long getRevenueThisWeek() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.with(DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(7);
        return orderRepository.getRevenueBetween(startOfDay(start),nextDay(end));
    }

    public List<Product> getNearlySoldOutProduct(){
        int unitsoldOut = 20;
        return orderRepository.findingNearlySoldOutProduct(unitsoldOut);
    }

    public long getRevenueYesterday(){
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return orderRepository.getRevenueBetween(startOfDay(yesterday),nextDay(yesterday));
    }

    public long getRevenueLastWeek(){
        LocalDate now = LocalDate.now();
        LocalDate startThisWeek = now.with(DayOfWeek.MONDAY);
        LocalDate startLastWeek = startThisWeek.minusWeeks(1);
        return orderRepository.getRevenueBetween(startOfDay(startLastWeek),nextDay(startThisWeek));
    }

    public long getRevenueLastMonth(){
        LocalDate starThisMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate startLastMonth = starThisMonth.minusMonths(1);
        return orderRepository.getRevenueBetween(startOfDay(startLastMonth),nextDay(starThisMonth));
    }
    public double getDailyPercentageChange(){
        long today = getRevenueToday();
        long yesterday = getRevenueYesterday();
        if(yesterday == 0){
            return 100.0;
        }
        if(today == 0){
            return 0;
        }
        double percentageChange = ((double)(today - yesterday) / yesterday) * 100;
        return Math.round(percentageChange * 100.0)/100.0;
    }

    public double getWeeklyPercentageChange(){
        long thisWeek = getRevenueThisWeek();
        long lastWeek = getRevenueLastWeek();
        if(lastWeek == 0) {
            return 100.0;
        }
        if(thisWeek == 0) {
            return 0;
        }
        double percentageChange = ((double)(thisWeek - lastWeek) / lastWeek) * 100;
        return Math.round(percentageChange * 100.0)/100.0;
    }

    public double getMonthlyPercentageChange(){
        long thisMonth = getRevenueThisMonth();
        long lastMonth = getRevenueLastMonth();
        if(lastMonth == 0) {
            return 100.0;
        }
        if(thisMonth == 0) {
            return 0;
        }
        double percentageChange = ((double)(thisMonth - lastMonth) / lastMonth) * 100;
        return Math.round(percentageChange * 100.0)/100.0;
    }

    public void markOrderStatusAsShipping(Order order) {
        order.setOrderStatus(orderStatusService.getShippingStatus());
        order.addShippingStatus(Shipping.builder()
                .shippingStatus(shippingStatusService.getAwaitingPickupStatus())
                .build());
        shipperService.autoAssignShipperToOrder(order);
        orderRepository.save(order);
    }

    public void markOrderShippingStatusAsAwaitingPickup(Long orderId, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        try {
            // Update shipping status to awaiting pickup
            order.addShippingStatus(Shipping.builder()
                    .shippingStatus(shippingStatusService.getAwaitingPickupStatus())
                    .build());
            orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển trạng thái: " + e.getMessage());
        }
    }

    public void markOrderShippingStatusAsPickedUp(Long orderId, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        try {
            // Update shipping status to picked up
            order.addShippingStatus(Shipping.builder()
                    .shippingStatus(shippingStatusService.getPickedUpStatus())
                    .build());
            orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển trạng thái: " + e.getMessage());
        }
    }

    public void markOrderShippingStatusAsShipping(Long orderId, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        try {
            // Update shipping status to shipping
            order.addShippingStatus(Shipping.builder()
                    .shippingStatus(shippingStatusService.getShippingStatus())
                    .build());
            orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển trạng thái: " + e.getMessage());
        }
    }

    public void markOrderStatusAsDelivered(Long orderId, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        try {
            // Update order status to deliver directly instead of calling OrderService
            order.setOrderStatus(orderStatusService.getDeliveredStatus());
            order.addShippingStatus(Shipping.builder()
                    .shippingStatus(shippingStatusService.getDeliveredStatus())
                    .build());
            orderRepository.save(order);
    
            // If COD, create bill after order is delivered
            if(paymentMethodService.isCodMethod(order.getPaymentMethod())) {
                createBillForOrder(order);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi chuyển trạng thái hoặc tạo hóa đơn: " + e.getMessage());
        }
    }

    public int countDoneOrdersXMonthsAgo(Principal principal, int monthsAgo) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        return (int) results
        .stream()
        // .filter(orderStatusService::isDeliveredStatus)
        .filter(order -> order.getCurrentShipping().getOccurredAt().getMonth() == LocalDate.now().minusMonths(monthsAgo).getMonth()
                && order.getCurrentShipping().getOccurredAt().getYear() == LocalDate.now().minusMonths(monthsAgo).getYear())
        .count();
    }

    public int countPercentageComparedToThePreviousMonth(Principal principal, int monthsAgo) {
        int currentMonthCount = countDoneOrdersXMonthsAgo(principal, monthsAgo);
        int previousMonthCount = countDoneOrdersXMonthsAgo(principal, monthsAgo + 1);

        if (previousMonthCount == 0) {
            return currentMonthCount == 0 ? 0 : 100; // If both are 0, return 0%, else return 100%
        }

        return (int) (((double) (currentMonthCount - previousMonthCount) / previousMonthCount) * 100);
    }

    public int countDoneOrdersXDaysAgo(Principal principal, int daysAgo) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        return (int) results
        .stream()
        // .filter(orderStatusService::isDeliveredStatus)
        .filter(order -> order.getCurrentShipping().getOccurredAt().getDayOfYear() == LocalDate.now().minusDays(daysAgo).getDayOfYear()
                && order.getCurrentShipping().getOccurredAt().getYear() == LocalDate.now().minusDays(daysAgo).getYear())
        .count();
    }

    public int countPercentageComparedToThePreviousDay(Principal principal, int daysAgo) {
        int currentDayCount = countDoneOrdersXDaysAgo(principal, daysAgo);
        int previousDayCount = countDoneOrdersXDaysAgo(principal, daysAgo + 1);

        if (previousDayCount == 0) {
            return currentDayCount == 0 ? 0 : 100; // If both are 0, return 0%, else return 100%
        }

        return (int) (((double) (currentDayCount - previousDayCount) / previousDayCount) * 100);
    }

    public Page<Order> getDeliveringOrders(Principal principal, int page, int size, String searchQuery, String sortCriteria, int k, String sortCriteriaInPage) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Pageable pageable = PageRequest.of(page - 1, size);

        // Nếu repository chưa có query riêng thì vẫn phải filter trong memory
        List<Order> allOrders = orderRepository.findByShipper_Email(principal.getName())
            .stream()
            .filter(orderStatusService::isShippingStatus)
            .sorted((o1, o2) -> {
                if (sortCriteria == null) return 0;
                return switch (sortCriteria) {
                    case "id" -> o1.getId().compareTo(o2.getId());
                    case "email" -> o1.getCustomer().getEmail().compareTo(o2.getCustomer().getEmail());
                    case "status" ->
                            o1.getCurrentShippingStatus().getId().compareTo(o2.getCurrentShippingStatus().getId());
                    default -> 0;
                };
            })
            .toList();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            allOrders = allOrders.stream()
                .filter(order -> order.getCustomer().getEmail().toLowerCase().contains(searchQuery.toLowerCase()))
                .toList();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allOrders.size());
        List<Order> pagedOrders = allOrders.subList(start, end);

        pagedOrders = pagedOrders.stream()
            .sorted((o1, o2) -> {
                if (sortCriteriaInPage == null) return 0;
                return switch (sortCriteriaInPage) {
                    case "id" -> k * o1.getId().compareTo(o2.getId());
                    case "email" -> k * o1.getCustomer().getEmail().compareTo(o2.getCustomer().getEmail());
                    case "status" ->
                            k * o1.getCurrentShippingStatus().getId().compareTo(o2.getCurrentShippingStatus().getId());
                    default -> 0;
                };
            })
            .toList();

        return new PageImpl<>(pagedOrders, pageable, allOrders.size());
    }

    public Page<Order> getDoneOrders(Principal principal, int page, int size, String searchQuery, Date completionDateFromQuery, Date completionDateToQuery, String sortCriteria, int k, String sortCriteriaInPage) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        Pageable pageable = PageRequest.of(page - 1, size);

        List<Order> allOrders = orderRepository.findByShipper_Email(principal.getName())
        .stream()
        .filter(orderStatusService::isDeliveredStatus)
        .sorted((o1, o2) -> {
            if (sortCriteria == null) return 0;
            return switch (sortCriteria) {
                case "id" -> o1.getId().compareTo(o2.getId());
                case "email" -> o1.getCustomer().getEmail().compareTo(o2.getCustomer().getEmail());
                case "status" -> o1.getCurrentShippingStatus().getId().compareTo(o2.getCurrentShippingStatus().getId());
                case "deliveredAt" -> o1.getCurrentShipping().getOccurredAt().compareTo(o2.getCurrentShipping().getOccurredAt());
                default -> 0;
            };
        })
        .toList();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            allOrders = allOrders.stream()
                .filter(order -> order.getCustomer().getEmail().toLowerCase().contains(searchQuery.toLowerCase()))
                .toList();
        }

        if (completionDateFromQuery != null) {
            LocalDateTime fromDateTime = LocalDateTime.ofInstant(completionDateFromQuery.toInstant(), java.time.ZoneId.systemDefault());
            allOrders = allOrders.stream()
                .filter(order -> order.getCurrentShipping().getOccurredAt().isAfter(fromDateTime) ||
                                 order.getCurrentShipping().getOccurredAt().isEqual(fromDateTime))
                .toList();
        }

        if (completionDateToQuery != null) {
            LocalDateTime toDateTime = LocalDateTime.ofInstant(completionDateToQuery.toInstant(), java.time.ZoneId.systemDefault());
            allOrders = allOrders.stream()
                .filter(order -> order.getCurrentShipping().getOccurredAt().isBefore(toDateTime) ||
                                 order.getCurrentShipping().getOccurredAt().isEqual(toDateTime))
                .toList();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allOrders.size());
        List<Order> pagedOrders = allOrders.subList(start, end);

        pagedOrders = pagedOrders.stream()
        .sorted((o1, o2) -> {
            if (sortCriteriaInPage == null) return 0;
            return switch (sortCriteriaInPage) {
                case "id" -> k * o1.getId().compareTo(o2.getId());
                case "email" -> k * o1.getCustomer().getEmail().compareTo(o2.getCustomer().getEmail());
                case "status" -> k * o1.getCurrentShippingStatus().getId().compareTo(o2.getCurrentShippingStatus().getId());
                case "deliveredAt" -> k * o1.getCurrentShipping().getOccurredAt().compareTo(o2.getCurrentShipping().getOccurredAt());
                default -> 0;
            };
        })
        .toList();

        return new PageImpl<>(pagedOrders, pageable, allOrders.size());
    }

    public void loadDoneOrders(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        results = orderRepository.findByShipper_Email(principal.getName())
            .stream()
            .filter(orderStatusService::isDeliveredStatus)
            .toList();

    }

    public String getShippedAt(Order order){
        if (order.getShipping() != null && !order.getShipping().isEmpty()) {
            if (shippingStatusService.isDeliveredStatus(order.getCurrentShippingStatus())) {
                LocalDateTime occurredAt = order.getCurrentShipping().getOccurredAt();
                return "Ngày " + occurredAt.getDayOfMonth() + " tháng " + occurredAt.getMonthValue() + " năm " + occurredAt.getYear() + 
                        " lúc " + String.format("%02d", occurredAt.getHour()) + ":" + String.format("%02d", occurredAt.getMinute());
            }
        }
        return "Chưa giao";
    }

    public List<RevenueDto> getDaysRevenue(){
        List<Object[]> raw = orderRepository.getRevenueLast7Days();
        List<RevenueDto> result = new ArrayList<>();

        for (Object[] row : raw) {
            String date = (String) row[0];
            Long revenue = ((Number) row[1]).longValue();
            result.add(new RevenueDto(date, revenue,null));
        }

        for (int i = 0; i < result.size(); i++) {
            if (i == result.size() - 1) {
                result.get(i).setGrowthPercent(null);
            } else {
                long today = result.get(i).getRevenue();
                long yesterday = result.get(i + 1).getRevenue();
                if (yesterday == 0 && today ==0) {
                    result.get(i).setGrowthPercent(0.0);
                }
                else if(yesterday == 0){
                    result.get(i).setGrowthPercent(100.0);
                }
                else
                    result.get(i).setGrowthPercent(((today - yesterday) / (double) yesterday) * 100) ;
            }

        }


        return result.subList(0, 7);

    }
    public List<RevenueDto> getMonthsRevenue(){
        List<Object[]> raw = orderRepository.getRevenueLast12Months();
        List<RevenueDto> result = new ArrayList<>();

        for (Object[] row : raw) {
            String date = (String) row[0];
            Long revenue = ((Number) row[1]).longValue();
            result.add(new RevenueDto(date, revenue,null));
        }
        for (int i = 0; i < result.size(); i++) {
            if (i == result.size() - 1) {
                result.get(i).setGrowthPercent(null);
            } else {
                long thisMonth = result.get(i).getRevenue();
                long lastMonth = result.get(i + 1).getRevenue();
                if (lastMonth == 0 && thisMonth == 0) {
                    result.get(i).setGrowthPercent(0.0);
                }
                else if(lastMonth == 0){
                    result.get(i).setGrowthPercent(100.0);
                }
                else
                    result.get(i).setGrowthPercent(((thisMonth - lastMonth) / (double) lastMonth) * 100) ;
            }

        }


        return result.subList(0, 12);
    }

    public ByteArrayInputStream exportDaysRevenueToExcel() throws IOException {
        List<Object[]> raw = orderRepository.getRevenueLast7Days();

        // Chuyển sang List<RevenueDto>
        List<RevenueDto> revenues = new ArrayList<>();
        for (Object[] row : raw) {
            String date = (String) row[0];
            Long revenue = (Long) row[1];
            revenues.add(new RevenueDto(date, revenue,null));
        }
        for (int i = 0; i < revenues.size() - 1; i++) {
                long today = revenues.get(i).getRevenue();
                long yesterday = revenues.get(i + 1).getRevenue();

                double growth;
                if (yesterday == 0 && today == 0) {
                    growth = 0.0;
                } else if (yesterday == 0) {
                    growth = 100.0;
                } else {
                    growth = ((today - yesterday) / (double) yesterday) * 100;
                }

                revenues.get(i).setGrowthPercent(growth);

        }

            revenues = revenues.subList(0, 7);

        // Tạo workbook Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revenue 7 Days");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Ngày");
            header.createCell(1).setCellValue("Doanh thu");
            header.createCell(2).setCellValue("(%) Tăng trưởng");

            int rowIdx = 1;
            for (RevenueDto dto : revenues) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dto.getDate());
                row.createCell(1).setCellValue(dto.getRevenue()+" VND");
                row.createCell(2).setCellValue(dto.getGrowthPercent()+" %");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }

    }
    public ByteArrayInputStream exportMonthsRevenueToExcel() throws IOException {
        List<Object[]> raw = orderRepository.getRevenueLast12Months();

        // Chuyển sang List<RevenueDto>
        List<RevenueDto> revenues = new ArrayList<>();
        for (Object[] row : raw) {
            String date = (String) row[0];
            Long revenue = (Long) row[1];
            revenues.add(new RevenueDto(date, revenue,null));
        }

        for (int i = 0; i < revenues.size() - 1; i++) {

                long thisMonth = revenues.get(i).getRevenue();
                long lastMonth = revenues.get(i + 1).getRevenue();

                double growth;
                if (lastMonth == 0 && thisMonth == 0) {
                    growth = 0.0;
                } else if (lastMonth == 0) {
                    growth = 100.0;
                } else {
                    growth = ((thisMonth - lastMonth) / (double) lastMonth) * 100;
                }

                revenues.get(i).setGrowthPercent(growth);

        }

            revenues = revenues.subList(0, 12);


        // Tạo workbook Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Revenue 12 Months");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Tháng");
            header.createCell(1).setCellValue("Doanh thu");
            header.createCell(2).setCellValue("(%) Tăng trưởng");

            int rowIdx = 1;
            for (RevenueDto dto : revenues) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(dto.getDate());
                row.createCell(1).setCellValue(dto.getRevenue()+" VND");
                row.createCell(2).setCellValue(dto.getGrowthPercent()+" %");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }

    }

    public long countDeliveringOrders(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        return orderRepository.findByShipper_Email(principal.getName())
            .stream()
            .filter(orderStatusService::isShippingStatus)
            .count();
    }

    public long countDoneOrders(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Người giao hàng không xác định");
        }
        return orderRepository.findByShipper_Email(principal.getName())
            .stream()
            .filter(orderStatusService::isDeliveredStatus)
            .count();
    }

}
