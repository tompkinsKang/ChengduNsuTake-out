package com.nsu.service;
/*
  Date:2025/3/20
  Time:10:21
  @author llh 
 */

import com.nsu.dto.ShoppingCartDTO;
import com.nsu.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加进购物车
     * @param shoppingCartDTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCarts();

    void clean();

    void sub(ShoppingCartDTO shoppingCartDTO);
}

