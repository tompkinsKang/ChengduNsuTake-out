package com.nsu.service;

import com.nsu.vo.BusinessDataVO;
import com.nsu.vo.DishOverViewVO;
import com.nsu.vo.OrderOverViewVO;
import com.nsu.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkSpaceService {
    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 管理端工作台订单概览
     * @return
     */
    OrderOverViewVO overviewOrders();

    /**
     * 管理端工作台菜品概览
     * @return
     */
    DishOverViewVO overviewDishes();

    /**
     * 查询套餐总览
     * @return
     */
    SetmealOverViewVO overviewSetmeal();
}
