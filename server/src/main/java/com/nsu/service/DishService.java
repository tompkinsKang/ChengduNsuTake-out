package com.nsu.service;
/*
  Date:2025/3/13
  Time:13:48
  @author llh 
 */

import com.nsu.dto.DishDTO;
import com.nsu.dto.DishPageQueryDTO;
import com.nsu.entity.Dish;
import com.nsu.result.PageResult;
import com.nsu.vo.DishVO;

import java.util.List;

public interface DishService {

    void addDish(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    void delete(List<Long> ids);

    DishVO getById(Long id);

    void update(DishDTO dishDTO);

    List<Dish> getByCategoryId(Long categoryId);

    List<DishVO> listWithFlavor(Dish dish);

    void startOrStop(Long id, Integer status);
}

