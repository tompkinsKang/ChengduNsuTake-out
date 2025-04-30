package com.nsu.task;
/*
  Date:2025/3/25
  Time:20:01
  @author llh 
 */

import com.nsu.entity.Orders;
import com.nsu.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 处理支付超时订单
     * 0 * * * * ? 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
//    @Scheduled(cron = "1/5 * * * * ?")
    public void processTimeoutOrders(){
        log.info("处理支付超时订单：{}", new Date());
        // 查询未支付状态的订单，以及下单时间小于当前时间-15分钟，代表下单后15分钟内未支付的订单。
        // select * from order where status = 1 and order_time < (当前时间-15min)
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> orders = ordersMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if (orders != null && !orders.isEmpty()){
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());

                ordersMapper.update(order);
            }
        }
    }

    /**
     * 处理“派送中”状态的订单
     * 0 0 1 * * ? 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0/5 * * * * ?")
    public void processDeliveryOrder(){
        log.info("处理派送中订单：{}", new Date());
        // 凌晨一点闭店后查询未完成处于派送中的订单，自动完成订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orders = ordersMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if (orders != null && !orders.isEmpty()){
            for (Orders order : orders) {
                order.setStatus(Orders.COMPLETED);

                ordersMapper.update(order);
            }
        }
    }

}

