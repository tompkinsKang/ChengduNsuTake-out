package com.nsu.service.impl;
/*
  Date:2025/3/28
  Time:11:05
  @author llh 
 */

import com.nsu.constant.StatusConstant;
import com.nsu.entity.Orders;
import com.nsu.mapper.*;
import com.nsu.service.WorkSpaceService;
import com.nsu.vo.BusinessDataVO;
import com.nsu.vo.DishOverViewVO;
import com.nsu.vo.OrderOverViewVO;
import com.nsu.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    ReportMapper reportMapper;

    @Autowired
    OrdersMapper ordersMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    DishMapper dishMapper;

    @Autowired
    SetmealMapper setmealMapper;

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        // 营业额
        HashMap<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        Double turnover = reportMapper.sumByMap(map);
        turnover = turnover == null ? 0 : turnover;
        // 新增用户数
        Integer userCount = userMapper.getUserByMap(map);
        // 总订单数
        Integer totalOrderCount = ordersMapper.getOrderByMap(map);
        // 有效订单数
        map.put("status",5);
        Integer validOrderCount = ordersMapper.getOrderByMap(map);

        Double averageOrderPrice = 0.0;
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0) {
            // 订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            // 平均客单价，营业额 / 有效订单数
            averageOrderPrice = turnover / validOrderCount;
        }

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(averageOrderPrice)
                .newUsers(userCount)
                .build();
    }

    /**
     * 订单总览
     * @return
     */
    @Override
    public OrderOverViewVO overviewOrders() {
        //获得当天的开始时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        //获得当天的结束时间
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        HashMap<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);
        // 全部订单
        Integer allOrders = ordersMapper.getOrderByMap(map);
        // 待接单数量
        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = ordersMapper.getOrderByMap(map);
        // 待派送数量
        map.put("status", Orders.CONFIRMED);
        Integer deliverOrders = ordersMapper.getOrderByMap(map);
        // 已完成数量
        map.put("status",Orders.COMPLETED);
        Integer completedOrders = ordersMapper.getOrderByMap(map);
        // 已取消数量
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = ordersMapper.getOrderByMap(map);

        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliverOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO overviewDishes() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO overviewSetmeal() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}

