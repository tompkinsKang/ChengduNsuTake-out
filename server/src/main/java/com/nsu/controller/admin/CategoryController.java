package com.nsu.controller.admin;
/*
  Date:2025/3/11
  Time:16:39
  @author llh 
 */

import com.nsu.dto.CategoryDTO;
import com.nsu.dto.CategoryPageQueryDTO;
import com.nsu.entity.Category;
import com.nsu.result.PageResult;
import com.nsu.result.Result;
import com.nsu.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("admin/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @ApiOperation("添加分类")
    public Result addCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("添加分类：{}",categoryDTO);
        categoryService.add(categoryDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询分类")
    public Result<PageResult> page(CategoryPageQueryDTO pageQueryDTO){
        log.info("分页查询分类：{}",pageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result deleteCategory(Long id){
        log.info("删除分类：{}",id);
        categoryService.delete(id);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改分类")
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类：{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("停用或启用分类")
    public Result startOrStop(@PathVariable("status")Integer status,Long id){
        log.info("停用或启用分类 status={},id={}",status,id);
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type){
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }

}

