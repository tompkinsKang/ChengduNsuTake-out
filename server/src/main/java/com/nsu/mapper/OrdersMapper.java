package com.nsu.mapper;
/*
  Date:2025/3/21
  Time:9:49
  @author llh 
 */

import com.github.pagehelper.Page;
import com.nsu.dto.GoodsSalesDTO;
import com.nsu.dto.OrdersPageQueryDTO;
import com.nsu.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Mapper
public interface OrdersMapper {

    /**
     * 插入订单
     * @param order
     */
    void insert(Orders order);

    /**
     * 分页查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    @Select("select * from sky_take_out.orders where id = #{id}")
    Orders getById(Long id);

    @Update("update sky_take_out.orders set status = #{orderStatus},orders.pay_status=#{orderPaidStatus}," +
            "checkout_time=#{check_out_time} where number = #{orderNumber}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, String orderNumber);

    void update(Orders orders);

    @Select("select count(*) from sky_take_out.orders where status = #{status}")
    // 订单状态数量
    Integer countStatus(Integer status);

    /**
     * 根据状态和下单时间查询订单，可能会有多个未支付订单
     * @param status
     * @param orderTime
     */
    @Select("select * from sky_take_out.orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 根据时间查询订单数量
     * @return
     */
    Integer getOrderByMap(HashMap<String,Object> map);

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}

