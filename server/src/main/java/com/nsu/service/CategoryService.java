package com.nsu.service;

import com.nsu.dto.CategoryDTO;
import com.nsu.dto.CategoryPageQueryDTO;
import com.nsu.entity.Category;
import com.nsu.result.PageResult;
import java.util.List;

/*
  Date:2025/3/11
  Time:16:42
  @author llh 
 */

public interface CategoryService {

    void add(CategoryDTO categoryDTO);

    PageResult pageQuery(CategoryPageQueryDTO pageQueryDTO);

    void delete(Long id);

    void update(CategoryDTO categoryDTO);

    void startOrStop(Integer status, Long id);

    List<Category> list(Integer type);
}

