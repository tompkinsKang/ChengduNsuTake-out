package com.nsu.mapper;
/*
  Date:2025/3/20
  Time:12:22
  @author llh 
 */

import com.nsu.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 查询购物车中是否存在当前想要添加进购物车的商品，返回list但仅有一条数据，为了之后复用。
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id修改商品数量
     * @param newCart
     */
    @Update("update sky_take_out.shopping_cart set number = #{number} where id = #{id}")
    void updateNumById(ShoppingCart newCart);

    /**
     * 将商品插入购物车
     * @param cart
     */
    @Insert("insert into sky_take_out.shopping_cart(name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "VALUES (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void insert(ShoppingCart cart);

    @Delete("DELETE FROM sky_take_out.shopping_cart where user_id = #{id}")
    void deleteByUserId(Long Id);

    @Delete("delete from sky_take_out.shopping_cart where id = #{id}")
    void deleteByCartId(Long id);

    void insertBatch(List<ShoppingCart> shopList);
}

