package com.nsu.service.impl;
/*
  Date:2025/3/20
  Time:10:21
  @author llh 
 */

import com.nsu.context.BaseContext;
import com.nsu.dto.ShoppingCartDTO;
import com.nsu.entity.Dish;
import com.nsu.entity.Setmeal;
import com.nsu.entity.ShoppingCart;
import com.nsu.mapper.DishMapper;
import com.nsu.mapper.SetmealMapper;
import com.nsu.mapper.ShoppingCartMapper;
import com.nsu.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加进购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 1、考虑购物车中是否存在该商品
        // 购物车对象
        ShoppingCart cart = new ShoppingCart();
        // 拷贝数据，包含菜品id，套餐id二者之一，如果是菜品的话还有flavor
        BeanUtils.copyProperties(shoppingCartDTO, cart);
        // 设置当前用户Id，只能自己看自己的购物车
        cart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(cart);

        // 2、如果已经存在了，就将该商品的数量+1，然后更新数据
        if (list != null && list.size() > 0) {
            ShoppingCart newCart = list.get(0);
            newCart.setNumber(list.get(0).getNumber() + 1);
            shoppingCartMapper.updateNumById(newCart);
        }
        else {
            // 3、如果购物车中不存在当前添加的商品，先判断是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                //如果是菜品，就为当前购物车商品，添加该菜品的一些信息。
                Dish dish = dishMapper.getById(dishId);
                cart.setName(dish.getName());
                cart.setImage(dish.getImage());
                cart.setAmount(dish.getPrice());
            }else {
                // 如果是套餐，添加信息。
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                cart.setName(setmeal.getName());
                cart.setImage(setmeal.getImage());
                cart.setAmount(setmeal.getPrice());
            }
            // 重复字段整理
            cart.setCreateTime(LocalDateTime.now());
            cart.setNumber(1);
            // 统一插入
            shoppingCartMapper.insert(cart);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCarts() {
        return shoppingCartMapper.list(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());
    }


    @Override
    public void clean() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    /**
     *
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        // 查看当前用户购物车中的商品
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 查出购物车中的商品
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list == null || list.isEmpty()){
            return;
        }

        shoppingCart = list.get(0);
        // 根据商品份数减少商品数量，如果商品数量为1，就删除该商品，否则-1
        if(shoppingCart.getNumber() == 1){
            shoppingCartMapper.deleteByCartId(shoppingCart.getId());
        }else{
            shoppingCart.setNumber(shoppingCart.getNumber() - 1);
            shoppingCartMapper.updateNumById(shoppingCart);
        }

    }
}

