package com.nsu.service.impl;
/*
  Date:2025/3/18
  Time:21:40
  @author llh 
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nsu.constant.MessageConstant;
import com.nsu.dto.UserLoginDTO;
import com.nsu.entity.User;
import com.nsu.exception.LoginFailedException;
import com.nsu.mapper.UserMapper;
import com.nsu.properties.WeChatProperties;
import com.nsu.service.UserService;
import com.nsu.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    // 微信登录接口
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    public String getOpenId(String code) {
        // 查看微信用户接口文档，获取openid
        Map<String, String> map = new HashMap<>();
        map.put("appId", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        // 返回一个json字符串，包含openid，session_key等信息
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, map);

        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openId = getOpenId(userLoginDTO.getCode());
        // 未正常获取openId，抛出登录异常
        if (openId == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        // 判断是否为新用户
        User user = userMapper.getByOpenId(openId);
        if (user == null) {
            // 新用户，插入数据库
            user = User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }
}

