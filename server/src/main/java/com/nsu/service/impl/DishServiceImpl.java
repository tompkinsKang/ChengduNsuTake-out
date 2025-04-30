package com.nsu.service.impl;
/*
  Date:2025/3/13
  Time:13:49
  @author llh 
 */

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nsu.constant.MessageConstant;
import com.nsu.dto.DishDTO;
import com.nsu.dto.DishPageQueryDTO;
import com.nsu.entity.Dish;
import com.nsu.entity.DishFlavor;
import com.nsu.exception.DeletionNotAllowedException;
import com.nsu.mapper.DishFlavorMapper;
import com.nsu.mapper.DishMapper;
import com.nsu.mapper.SetmealDishMapper;
import com.nsu.result.PageResult;
import com.nsu.service.DishService;
import com.nsu.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Override

    @Transactional // 对多张表进行操作时使用，保证事务的完整性。
    public void addDish(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        // 新增菜品
        dishMapper.insert(dish);
        // 取到菜品的id
        Long dishId = dish.getId();
        // 对应口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 如果选择了口味，则遍历口味数组，并设置口味所属为当前菜品的id
        if (flavors != null && !flavors.isEmpty()){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
        }
        dishFlavorMapper.insertBatch(flavors);
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        // 设置分页,第一个参数是当前页码，第二个参数是每页显示的条数
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        // 查询数据并返回一个Page对象，里面包含了分页的所有信息
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        // 查询到的总条数
        long total = page.getTotal();
        // 查询到的结果
        List<DishVO> result = page.getResult();
        // 返回一个自定义的分页结果对象,里面包含了总条数和查询结果
        return new PageResult(total,result);
    }

    @Override
    @Transactional // 对多张表进行操作时使用，同时提交或回滚事务
    public void delete(List<Long> ids){
        // 1判断当前菜品是否在起售状态
        for (Long id : ids){
            Dish dish = dishMapper.getById(id);
            // 如果菜品状态为1，表示在售，不允许删除
            if (dish.getStatus() == 1){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 2判断当前菜品是否在套餐中，如果在套餐中，不允许删除
        // 根据菜品id查询套餐id
        for (Long id : ids) {
           List<Long> setmaelIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
           if (setmaelIds != null && !setmaelIds.isEmpty()){
               throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
           }
        }
//        for (Long id : ids) {
//            // 删除菜品
//            dishMapper.delete(id);
//            // 删除菜品对应的口味
//            dishFlavorMapper.deleteByDishId(id);
//        }
        // 批量删除菜品
        dishMapper.deleteByIds(ids);
        // 批量删除菜品对应的口味
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public DishVO getById(Long id) {
        // 根据id查询菜品
        Dish dish = dishMapper.getById(id);
        // 查询菜品对应的口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        // 将查询到的数据封装到VO对象中
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.update(dish);
        Long dishId = dish.getId();
        // 删除原有的口味
        dishFlavorMapper.deleteByDishId(dishId);
        // 设置新的口味所属菜品id
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
        }
        // 新增新的口味
        dishFlavorMapper.insertBatch(dishDTO.getFlavors());
    }

    // 根据分类id查询菜品
    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.getByCategoryId(dish.getCategoryId());

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    public void startOrStop(Long id, Integer status) {
        Dish dish = dishMapper.getById(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }
}

