package com.nsu.controller.user;
/*
  Date:2025/3/18
  Time:21:32
  @author llh 
 */

import com.nsu.constant.JwtClaimsConstant;
import com.nsu.dto.UserLoginDTO;
import com.nsu.entity.User;
import com.nsu.properties.JwtProperties;
import com.nsu.result.Result;
import com.nsu.service.UserService;
import com.nsu.utils.JwtUtil;
import com.nsu.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public Result<UserLoginVO> wxLogin(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信用户登录:{}", userLoginDTO.getCode());
        // 由于在此处需要jwt令牌，所以在Controller中定义VO
        // 微信登陆
        User user = userService.wxLogin(userLoginDTO);
        // 为微信用户生成jwt令牌
        HashMap<String, Object> claims = new HashMap<>();
        // 令牌中放入用户id
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        // 令牌中放入用户openid
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        // 构建返回的VO对象
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();

        return Result.success(userLoginVO);

    }
}

