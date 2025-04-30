package com.nsu.controller.admin;
/*
  Date:2025/3/22
  Time:13:58
  @author llh 
 */


import com.nsu.dto.OrdersPageQueryDTO;
import com.nsu.dto.OrdersRejectionDTO;
import com.nsu.result.PageResult;
import com.nsu.result.Result;

import com.nsu.service.OrderService;
import com.nsu.vo.OrderStatisticsVO;
import com.nsu.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "管理端订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    OrderService orderService;

    /**
     * 订单查询
     * @param ordersPageQueryDTO
     * @return
     */
    @ApiOperation("订单查询")
    @GetMapping("/conditionSearch")
    public Result<PageResult> orderSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("管理端订单查询:{}",ordersPageQueryDTO);
        PageResult pageResult = orderService.orderSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单统计
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单统计")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 订单详情查询
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("订单详情查询")
    public Result<OrderVO> details(@PathVariable Long id){
        log.info("订单详情查询:{}",id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     * @param id
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirmOrder(@RequestBody Long id){
        log.info("接单:{}",id);
        orderService.confirmOrder(id);
        return Result.success();
    }

    /**
     * 拒绝接单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒绝接单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        orderService.rejectionOrder(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody Long id,String cancelReason){
        log.info("取消订单:{},{}",id,cancelReason);
        orderService.cancel(id,cancelReason);
        return Result.success();
    }

    /**
     * 派送订单
     *
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id) {
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     *
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable("id") Long id) {
        orderService.complete(id);
        return Result.success();
    }
}

