package com.nsu.service.impl;
/*
  Date:2025/3/11
  Time:16:42
  @author llh 
 */

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nsu.constant.MessageConstant;
import com.nsu.context.BaseContext;
import com.nsu.dto.CategoryDTO;
import com.nsu.dto.CategoryPageQueryDTO;
import com.nsu.entity.Category;
import com.nsu.exception.DeletionNotAllowedException;
import com.nsu.mapper.CategoryMapper;
import com.nsu.mapper.DishMapper;
import com.nsu.mapper.SetmealMapper;
import com.nsu.result.PageResult;
import com.nsu.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(CategoryDTO categoryDTO) {
        Category category = new Category();
        // 将categoryDTO的属性值拷贝到category中
        BeanUtils.copyProperties(categoryDTO,category);
        // 状态默认为0，禁用状态
        category.setStatus(0);
//        category.setCreateUser(BaseContext.getCurrentId());
//        category.setUpdateUser(BaseContext.getCurrentId());
//        category.setCreateTime(LocalDateTime.now());
//        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.add(category);
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        //下一条sql进行分页，自动加入limit关键字分页
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void delete(Long id) {
        Integer count = dishMapper.countByCategoryId(id);
        if (count > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }
        count = setmealMapper.countByCategoryId(id);
        if (count > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }
        categoryMapper.delete(id);
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        // 设置修改时间和修改人
//        category.setUpdateTime(LocalDateTime.now());
//        category.setCreateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);

    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.startOrStop(category);

    }

    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}

