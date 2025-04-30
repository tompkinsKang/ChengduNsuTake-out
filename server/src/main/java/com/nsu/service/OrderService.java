package com.nsu.service;
/*
  Date:2025/3/20
  Time:19:34
  @author llh 
 */

import com.nsu.dto.OrdersPageQueryDTO;
import com.nsu.dto.OrdersPaymentDTO;
import com.nsu.dto.OrdersRejectionDTO;
import com.nsu.dto.OrdersSubmitDTO;
import com.nsu.result.PageResult;
import com.nsu.vo.OrderPaymentVO;
import com.nsu.vo.OrderStatisticsVO;
import com.nsu.vo.OrderSubmitVO;
import com.nsu.vo.OrderVO;

public interface OrderService {

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 查询历史订单
     * @param  page, pageSize, status
     * @return
     */
    PageResult historyOrders(Integer page, Integer pageSize,Integer status);

    OrderVO getOrderById(Long id);

    void cancelOrder(Long id);

    void repetitionOrder(Long id);

    PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO);

    OrderStatisticsVO statistics();

    OrderVO details(Long id);

    void confirmOrder(Long id);

    void cancel(Long id, String cancelReason);

    void delivery(Long id);

    void complete(Long id);

    void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO);

    void reminder(Long id);
}

