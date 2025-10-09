package com.xuwei.service;

import com.xuwei.model.User;

public interface UserService {
    User findUserByJwtToken(String jwt);
    User findUserByEmail(String email);
}
