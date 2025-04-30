package com.nsu.mapper;

import com.nsu.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    void addSetmealDishBatch(List<SetmealDish> setmealDishes);

    void deleteSetmealDishByIds(List<Long> ids);

    @Select("SELECT * from sky_take_out.setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealId(Long id);

    @Delete("delete from sky_take_out.setmeal_dish where setmeal_id = #{id}")
    void deleteSetmealDishBySetmealId(Long id);
}
