package com.nsu.controller.user;


import com.nsu.dto.OrdersPaymentDTO;
import com.nsu.dto.OrdersSubmitDTO;
import com.nsu.result.PageResult;
import com.nsu.result.Result;
import com.nsu.service.OrderService;
import com.nsu.vo.OrderPaymentVO;
import com.nsu.vo.OrderSubmitVO;
import com.nsu.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*
  Date:2025/3/20
  Time:19:27
  @author llh 
 */
@Slf4j
@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户端订单相关接口")
public class OrderController {

    @Autowired
    OrderService orderService;

    /**
     * 提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("提交订单")
    public Result<OrderSubmitVO> submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交订单:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 历史订单
     * @param page, pageSize, status
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单")
    public Result<PageResult> historyOrders(Integer page, Integer pageSize,Integer status) {
        log.info("C端查询历史订单:{},{},{}",page,pageSize,status);
        PageResult pageResult = orderService.historyOrders(page,pageSize,status);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getOrderById(@PathVariable Long id){
        log.info("查询订单详情:{}",id);
        OrderVO orderVO = orderService.getOrderById(id);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancelOrder(@PathVariable Long id){
        log.info("取消订单:{}",id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetitionOrder(@PathVariable Long id){
        log.info("再来一单:{}",id);
        orderService.repetitionOrder(id);
        return Result.success();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 用户催单
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable("id") Long id) {
        orderService.reminder(id);
        return Result.success();
    }
}

