package com.xuwei.service.Impl;

import com.xuwei.domain.AccountStatus;
import com.xuwei.model.Seller;

import java.util.List;

public interface SellerRepository {

    Seller findByEmail(String email);
    List<Seller> findByAccountStatus(AccountStatus status);
}
