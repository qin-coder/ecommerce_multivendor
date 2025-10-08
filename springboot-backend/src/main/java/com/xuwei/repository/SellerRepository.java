package com.xuwei.repository;


import com.xuwei.domain.AccountStatus;
import com.xuwei.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerRepository extends JpaRepository<Seller,Long> {

    Seller findByEmail(String email);
    List<Seller> findByAccountStatus(AccountStatus status);
}
