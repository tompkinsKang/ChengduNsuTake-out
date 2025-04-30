package com.nsu.service.impl;
/*
  Date:2025/3/15
  Time:21:46
  @author llh 
 */

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nsu.constant.MessageConstant;
import com.nsu.constant.StatusConstant;
import com.nsu.dto.SetmealDTO;
import com.nsu.dto.SetmealPageQueryDTO;
import com.nsu.entity.Dish;
import com.nsu.entity.Setmeal;
import com.nsu.entity.SetmealDish;
import com.nsu.exception.DeletionNotAllowedException;
import com.nsu.exception.SetmealEnableFailedException;
import com.nsu.mapper.DishMapper;
import com.nsu.mapper.SetmealDishMapper;
import com.nsu.mapper.SetmealMapper;
import com.nsu.result.PageResult;
import com.nsu.service.SetmealService;
import com.nsu.vo.DishItemVO;
import com.nsu.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    SetmealMapper setmealMapper;

    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Autowired
    DishMapper dishMapper;
    @Autowired
    private SetmealService setmealService;

    @Override
    @Transactional // 涉及多张表的操作，保证事务的完整性
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 如果选择了套餐菜品，则遍历套餐菜品数组，并设置套餐菜品所属为当前套餐的id
        if (setmealDishes != null && !setmealDishes.isEmpty()){
            // 遍历套餐菜品数组，并设置套餐菜品所属为当前套餐的id
            for (SetmealDish dish : setmealDishes) {
                dish.setSetmealId(setmeal.getId());
            }
        }
        // 批量插入套餐菜品
        setmealDishMapper.addSetmealDishBatch(setmealDishes);
    }

    @Override
    public PageResult pageQuerySetmeal(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        // 查询到的总记录数
        long total = page.getTotal();
        // 查询到的结果集
        List<SetmealVO> result = page.getResult();
        return new PageResult(total,result);
    }

    @Override
    public void deleteSetmeal(List<Long> ids) {
        // 判断套餐是否在售
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == 1 ){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        // 批量删除套餐
        setmealMapper.deleteByIds(ids);
        // 批量删除套餐菜品
        setmealDishMapper.deleteSetmealDishByIds(ids);
    }

    @Override
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        // 查询套餐
        Setmeal setmeal = setmealMapper.getById(id);
        // 复制属性
        BeanUtils.copyProperties(setmeal,setmealVO);
        // 查询套餐菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        // 复制属性
        BeanUtils.copyProperties(setmealDTO,setmeal);
        // 修改套餐
        setmealMapper.update(setmeal);
        // 根据套餐的id，删除原有的套餐菜品
        Long id = setmeal.getId();
        setmealDishMapper.deleteSetmealDishBySetmealId(id);
        // 设置套餐菜品所属套餐的id
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
        });
        // 批量插入套餐菜品
        setmealDishMapper.addSetmealDishBatch(setmealDishes);
    }

    @Override
    public void openOrCloseSetmeal(Long id, Integer status) {
       // 起售时的判断
        // 若起售时，套餐内存在停售的菜品，则不允许起售
        if (status == StatusConstant.ENABLE){
            List<Dish> Dishes = dishMapper.getBySetmealId(id);
          Dishes.forEach(dish -> {
              if (dish.getStatus() == StatusConstant.DISABLE) {
                  throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
              }
          });
        }
        setmealMapper.openOrCloseSetmeal(id,status);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}

