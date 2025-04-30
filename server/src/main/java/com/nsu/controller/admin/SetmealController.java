package com.nsu.controller.admin;
/*
  Date:2025/3/15
  Time:21:42
  @author llh 
 */

import com.nsu.dto.SetmealDTO;
import com.nsu.dto.SetmealPageQueryDTO;
import com.nsu.result.PageResult;
import com.nsu.result.Result;
import com.nsu.service.SetmealService;
import com.nsu.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {

    @Autowired
    SetmealService setmealService;

    @PostMapping
    @ApiOperation("添加套餐")
    // 添加了新套餐后，将缓存清空。
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐:{}",setmealDTO);
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> pageQuerySetmeal(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询:{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuerySetmeal(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("删除套餐")
    // 删除其中的套餐后，保证数据一致性，将缓存清空
    @CacheEvict(value = "setmealCache",allEntries = true)
    public Result deleteSetmeal(@RequestParam List<Long> ids){
        log.info("批量或者单个删除套餐,ids:{}",ids);
        setmealService.deleteSetmeal(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查找套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查找套餐,id:{}",id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @ApiOperation("更新套餐")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("更新套餐，setmealDTO:{}",setmealDTO);
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache", allEntries = true)
    public Result openOrCloseSetmeal(@PathVariable Integer status,Long id){
        log.info("开启或者关闭套餐,id:{},status:{}",id,status);
        setmealService.openOrCloseSetmeal(id,status);
        return Result.success();
    }
}

