package com.swp.project.service.seller_request;

import com.swp.project.entity.seller_request.SellerRequest;
import com.swp.project.entity.seller_request.SellerRequestStatus;
import com.swp.project.repository.seller_request.SellerRequestStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SellerRequestStatusService {

    private final SellerRequestStatusRepository sellerRequestStatusRepository;

    public SellerRequestStatus getPendingStatus() {
        return sellerRequestStatusRepository.findByName("Đang Chờ Duyệt");
    }

    public SellerRequestStatus getApprovedStatus() {
        return sellerRequestStatusRepository.findByName("Đã Duyệt");
    }

    
    public boolean isPendingStatus(SellerRequest sellerRequest){
        return sellerRequest.getStatus().getName().equals("Đang Chờ Duyệt");
    }
}
