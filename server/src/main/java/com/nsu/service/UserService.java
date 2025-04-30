package com.nsu.service;

import com.nsu.dto.UserLoginDTO;
import com.nsu.entity.User;

public interface UserService {
    // 用户登录
    User wxLogin(UserLoginDTO userLoginDTO);
}
