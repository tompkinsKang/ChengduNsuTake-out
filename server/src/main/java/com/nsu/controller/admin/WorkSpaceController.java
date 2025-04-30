package com.nsu.controller.admin;
/*
  Date:2025/3/28
  Time:10:23
  @author llh 
 */

import com.nsu.result.Result;
import com.nsu.service.WorkSpaceService;
import com.nsu.vo.BusinessDataVO;
import com.nsu.vo.DishOverViewVO;
import com.nsu.vo.OrderOverViewVO;
import com.nsu.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController("adminWorkSpaceController")
@RequestMapping("/admin/workspace")
@Api(tags = "管理端工作台相关接口")
@Slf4j
public class WorkSpaceController {

    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * 管理端工作台数今日据概览
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("管理端工作台数今日据概览")
    public Result<BusinessDataVO> businessData(){
        //获得当天的开始时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        //获得当天的结束时间
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        log.info("管理端工作台数今日据概览");
        BusinessDataVO businessDataVO = workSpaceService.getBusinessData(begin,end);
        return Result.success(businessDataVO);
    }

    /**
     * 管理端工作台订单概览
     * @return
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("管理端工作台订单概览")
    public Result<OrderOverViewVO> overviewOrders(){
        log.info("管理端工作台订单概览");
        OrderOverViewVO orderOverViewVO = workSpaceService.overviewOrders();
        return Result.success(orderOverViewVO);

    }

    /**
     * 管理端工作台菜品概览
     * @return
     */
    @GetMapping("/overviewDishes")
    @ApiOperation("管理端工作台菜品概览")
    public Result<DishOverViewVO> overviewDishes(){
        log.info("管理端工作台菜品概览");
        DishOverViewVO dishOverViewVO = workSpaceService.overviewDishes();
        return Result.success(dishOverViewVO);
    }

    /**
     * 查询套餐总览
     * @return
     */
    @GetMapping("/overviewSetmeals")
    @ApiOperation("查询套餐总览")
    public Result<SetmealOverViewVO> setmealOverView(){
        return Result.success(workSpaceService.overviewSetmeal());
    }
}


