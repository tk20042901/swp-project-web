package com.swp.project.service.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.swp.project.entity.order.Bill;
import com.swp.project.repository.order.BillRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BillService {
    private final BillRepository billRepository;
    private final OrderService orderService;

    public Page<Bill> getBills(int page, int size, String queryName, String queryShopName, String queryAddress, String fromDate, String toDate, String minAmount, String maxAmount, String sortCriteria, int k, String sortCriteriaInPage) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Bill> bills = billRepository.findAll()
        .stream()
        .sorted((o1, o2) -> {
            if (sortCriteria == null) return 0;
            switch (sortCriteria) {
                case "id":
                    return o1.getId().compareTo(o2.getId());
                case "shopName":
                    return o1.getShopName().compareTo(o2.getShopName());
                case "customer":
                    return o1.getOrder().getCustomer().getFullName().compareTo(o2.getOrder().getCustomer().getFullName());
                case "paymentTime":
                    return o1.getPaymentTime().compareTo(o2.getPaymentTime());
                case "totalAmount":
                    return o1.getOrder().getTotalAmount().compareTo(o2.getOrder().getTotalAmount());
                case "address":
                    return o1.getOrder().getAddressString().compareTo(o2.getOrder().getAddressString());
                default:
                    return 0;
            }
        })
        .toList();

        if (queryName != null && !queryName.isEmpty()) {
            String lowerCaseQuery = queryName.toLowerCase();
            bills = bills.stream()
                    .filter(bill -> bill.getOrder().getCustomer().getFullName().toLowerCase().contains(lowerCaseQuery))
                    .toList();
        }
        if (queryShopName != null && !queryShopName.isEmpty()) {
            String lowerCaseShop = queryShopName.toLowerCase();
            bills = bills.stream()
                    .filter(bill -> bill.getShopName() != null && bill.getShopName().toLowerCase().contains(lowerCaseShop))
                    .toList();
        }
        if (queryAddress != null && !queryAddress.isEmpty()) {
            String lowerCaseAddr = queryAddress.toLowerCase();
            bills = bills.stream()
                    .filter(bill -> bill.getOrder().getAddressString() != null && bill.getOrder().getAddressString().toLowerCase().contains(lowerCaseAddr))
                    .toList();
        }
        if (fromDate != null && !fromDate.isEmpty()) {
            bills = bills.stream()
                    .filter(bill -> bill.getPaymentTime() != null && !bill.getPaymentTime().toLocalDate().isBefore(java.time.LocalDate.parse(fromDate)))
                    .toList();
        }
        if (toDate != null && !toDate.isEmpty()) {
            bills = bills.stream()
                    .filter(bill -> bill.getPaymentTime() != null && !bill.getPaymentTime().toLocalDate().isAfter(java.time.LocalDate.parse(toDate)))
                    .toList();
        }
        if (minAmount != null && !minAmount.isEmpty()) {
            try {
                long min = Long.parseLong(minAmount);
                bills = bills.stream()
                        .filter(bill -> bill.getOrder().getTotalAmount() >= min)
                        .toList();
            } catch (NumberFormatException ignored) {}
        }
        if (maxAmount != null && !maxAmount.isEmpty()) {
            try {
                long max = Long.parseLong(maxAmount);
                bills = bills.stream()
                        .filter(bill -> bill.getOrder().getTotalAmount() <= max)
                        .toList();
            } catch (NumberFormatException ignored) {}
        }

        int start = Math.min((page - 1) * size, bills.size());
        int end = Math.min(start + size, bills.size());
        List<Bill> pagedBills = bills.subList(start, end);

        pagedBills = pagedBills.stream()
        .sorted((o1, o2) -> {
            if (sortCriteriaInPage == null) return 0;
            switch (sortCriteriaInPage) {
                case "id":
                    return k * o1.getId().compareTo(o2.getId());
                case "shopName":
                    return k * o1.getShopName().compareTo(o2.getShopName());
                case "customer":
                    return k * o1.getOrder().getCustomer().getFullName().compareTo(o2.getOrder().getCustomer().getFullName());
                case "paymentTime":
                    return k * o1.getPaymentTime().compareTo(o2.getPaymentTime());
                case "totalAmount":
                    return k * o1.getOrder().getTotalAmount().compareTo(o2.getOrder().getTotalAmount());
                case "address":
                    return k * o1.getOrder().getAddressString().compareTo(o2.getOrder().getAddressString());
                default:
                    return 0;
            }
        })
        .toList();

        return new PageImpl<>(pagedBills, pageable, bills.size());
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id).orElse(null);
    }

    public String getPaidAt(Bill bill) {
        LocalDateTime paymentTime = bill.getPaymentTime();
        if (paymentTime == null) {
            return "N/A";
        }
        return "Ngày " + paymentTime.getDayOfMonth() + " tháng " + paymentTime.getMonthValue() + " năm " + paymentTime.getYear() +
                " lúc " + String.format("%02d", paymentTime.getHour()) + ":" + String.format("%02d", paymentTime.getMinute());
    }
}
