package com.nsu.service;
/*
  Date:2025/3/15
  Time:21:46
  @author llh 
 */

import com.nsu.dto.SetmealDTO;
import com.nsu.dto.SetmealPageQueryDTO;
import com.nsu.entity.Setmeal;
import com.nsu.result.PageResult;
import com.nsu.vo.DishItemVO;
import com.nsu.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    void addSetmeal(SetmealDTO setmealDTO);

    PageResult pageQuerySetmeal(SetmealPageQueryDTO setmealPageQueryDTO);

    void deleteSetmeal(List<Long> ids);

    SetmealVO getById(Long id);

    void updateSetmeal(SetmealDTO setmealDTO);

    void openOrCloseSetmeal(Long id, Integer status);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}

