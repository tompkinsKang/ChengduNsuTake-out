package com.nsu.mapper;

import com.nsu.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
  Date:2025/3/13
  Time:16:38
  @author llh 
 */
@Mapper
public interface DishFlavorMapper {
    void insertBatch(List<DishFlavor> flavors);

    @Delete("delete from sky_take_out.dish_flavor where dish_id = #{id}")
    void deleteByDishId(Long id);

    @Select("select * from sky_take_out.dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);

    void deleteByDishIds(List<Long> ids);
}

