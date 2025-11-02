package com.swp.project.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swp.project.entity.user.Seller;

@Repository
public interface SellerRepository extends JpaRepository<Seller,Long> {
    Seller findByEmail(String email);

    Seller findByCid(String Cid);

    List<Seller> findByEmailContainsAndFullnameContainsAndCidContains(String queryEmail, String queryName, String queryCid);

}
