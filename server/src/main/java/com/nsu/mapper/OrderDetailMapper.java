package com.nsu.mapper;
/*
  Date:2025/3/21
  Time:10:10
  @author llh 
 */

import com.nsu.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单明细
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);

    @Select("select * from sky_take_out.order_detail where order_id = #{id}")
    List<OrderDetail> queryByOrderId(Long id);
}

