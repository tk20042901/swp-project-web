package com.swp.project.repository.seller_request;

import com.swp.project.entity.seller_request.SellerRequest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRequestRepository extends JpaRepository<SellerRequest,Long> {

    List<SellerRequest> findByEntityName(String simpleName);

    List<SellerRequest> findBySellerEmail(String email);

    List<SellerRequest> findAllByOrderByCreatedAtDesc();
}
