package com.nsu.service.impl;
/*
  Date:2025/3/20
  Time:19:34
  @author llh 
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.nsu.constant.MessageConstant;
import com.nsu.context.BaseContext;
import com.nsu.dto.OrdersPageQueryDTO;
import com.nsu.dto.OrdersPaymentDTO;
import com.nsu.dto.OrdersRejectionDTO;
import com.nsu.dto.OrdersSubmitDTO;
import com.nsu.entity.*;
import com.nsu.exception.AddressBookBusinessException;
import com.nsu.exception.OrderBusinessException;
import com.nsu.exception.ShoppingCartBusinessException;
import com.nsu.mapper.*;
import com.nsu.result.PageResult;
import com.nsu.service.OrderService;
import com.nsu.utils.HttpClientUtil;
import com.nsu.vo.OrderPaymentVO;
import com.nsu.vo.OrderStatisticsVO;
import com.nsu.vo.OrderSubmitVO;
import com.nsu.vo.OrderVO;
import com.nsu.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
    @Slf4j
    public class OrderServiceImpl implements OrderService {
        @Autowired
        private OrdersMapper ordersMapper;
        @Autowired
        private OrderDetailMapper orderDetailMapper;
        @Autowired
        private ShoppingCartMapper shoppingCartMapper;
        @Autowired
        private AddressBookMapper addressBookMapper;
        @Autowired
        private UserMapper userMapper;

        @Value("${sky.shop.address}")
        private String shopAddress;

        @Value("${sky.baidu.ak}")
        private String ak;

        @Autowired
        WebSocketServer webSocketServer;


    /**
     * 检查客户的收货地址是否超出配送范围
     * @param address
     */
    private void checkOutOfRange(String address) {
        Map map = new HashMap();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address",address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 50000){
            //配送距离超过50000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

        /**
         * 用户下单
         *
         * @param ordersSubmitDTO
         * @return
         */
        @Transactional
        public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
            //异常情况的处理（收货地址为空、超出配送范围、购物车为空）
            AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }

            //检查收货地址是否超出配送范围
            checkOutOfRange(addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());

            // 获取当前登录用户id，构造购物车对象，查询需要使用。
            Long userId = BaseContext.getCurrentId();
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);

            //查询当前用户的购物车数据
            List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
            if (shoppingCartList == null || shoppingCartList.isEmpty()) {
                throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
            }

            //构造订单数据
            Orders order = new Orders();
            BeanUtils.copyProperties(ordersSubmitDTO,order);
            order.setPhone(addressBook.getPhone());
            order.setAddress(addressBook.getDetail());
            order.setConsignee(addressBook.getConsignee());
            order.setNumber(String.valueOf(System.currentTimeMillis()));
            order.setUserId(userId);
            order.setStatus(Orders.PENDING_PAYMENT);
            order.setPayStatus(Orders.UN_PAID);
            order.setOrderTime(LocalDateTime.now());

            //向订单表插入1条数据
            ordersMapper.insert(order);

            //订单明细数据
            List<OrderDetail> orderDetailList = new ArrayList<>();
            for (ShoppingCart cart : shoppingCartList) {
                OrderDetail orderDetail = new OrderDetail();
                BeanUtils.copyProperties(cart, orderDetail);
                orderDetail.setOrderId(order.getId());
                orderDetailList.add(orderDetail);
            }

            //向明细表插入n条数据
            orderDetailMapper.insertBatch(orderDetailList);

            //清理购物车中的数据
            shoppingCartMapper.deleteByUserId(userId);

            //封装返回结果

            return OrderSubmitVO.builder()
                    .id(order.getId())
                    .orderNumber(order.getNumber())
                    .orderAmount(order.getAmount())
                    .orderTime(order.getOrderTime())
                    .build();
        }

    /**
     * 历史订单，返回的是pageResult对象，形参包含数据条数、数据。
     * 其中数据为OrderVO泛型的List集合，而OrderVO中继承了Order类，和一个订单详情的List集合。
     * 先将当前用户的所有订单查询出来，然后遍历订单，将订单和订单详情信息封装到OrderVO对象中。
     * @param  page, pageSize, status
     * @return
     */
    @Override
    public PageResult historyOrders(Integer page, Integer pageSize,Integer status) {
        // 设置分页
        PageHelper.startPage(page,pageSize);
        // 1、先查询当前用户的所有订单
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        // page.getTotal()获取总条数,page.getResult()获取数据
        Page<Orders> ordersPage = ordersMapper.pageQuery(ordersPageQueryDTO);
        // 订单集合
        List<Orders> orders = ordersPage.getResult();
        // 该集合用于存放OrderVO对象，是pageResult的数据
        ArrayList<Object> resultList = new ArrayList<>();
        // 遍历订单，通过订单Id查询所属的订单详情
        for (Orders order : orders) {
            List<OrderDetail> orderDetails = orderDetailMapper.queryByOrderId(order.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order,orderVO);
            orderVO.setOrderDetailList(orderDetails);
            resultList.add(orderVO);
        }
        return new PageResult(ordersPage.getTotal(),resultList);
    }

    @Override
    public OrderVO getOrderById(Long id) {
        // 根据id查询订单
        Orders order = ordersMapper.getById(id);
        // 查询订单对应的订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.queryByOrderId(order.getId());
        // 封装到OrderVO对象中，返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    @Override
    public void cancelOrder(Long id) {
        // 获取订单
        Orders order = ordersMapper.getById(id);
        // 校验订单是否存在
        if (order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 订单状态：1、待付款，2、待接单，3、待配送，4、待收货，5、已完成，6、已取消
        if (order.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();

        // 待接单时取消，要退款
        if (order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            orders.setStatus(Orders.REFUND);
        }

        orders.setId(order.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消订单");
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.update(orders);



    }

    @Override
    public void repetitionOrder(Long id) {
        // 查询当前用户id
        Long currentId = BaseContext.getCurrentId();
        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.queryByOrderId(id);
        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shopList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart);
            shoppingCart.setUserId(currentId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shopList.add(shoppingCart);
        }
        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shopList);
    }

    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = ordersMapper.pageQuery(ordersPageQueryDTO);

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.queryByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

        /**
         * 订单支付
         *
         * @param ordersPaymentDTO
         * @return
         */
        public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO){
            // 当前登录用户id
            Long userId = BaseContext.getCurrentId();
            User user = userMapper.getById(userId);

//            //调用微信支付接口，生成预支付交易单
//            JSONObject jsonObject = weChatPayUtil.pay(
//                    ordersPaymentDTO.getOrderNumber(), //商户订单号
//                    new BigDecimal(0.01), //支付金额，单位 元
//                    "苍穹外卖订单", //商品描述
//                    user.getOpenid() //微信用户的openid
//            );
//
//            if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//                throw new OrderBusinessException("该订单已支付");
//            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", "ORDERPAID");
            OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
            vo.setPackageStr(jsonObject.getString("package"));

            // 替代微信支付成功后的数据库状态更新
            Integer paid = OrderVO.PAID; // 已支付
            Integer toBeConfirmed = Orders.TO_BE_CONFIRMED; // 待接单
            // 支付时间赋值
            LocalDateTime check_out_time = LocalDateTime.now();
            // 获取订单号
            String orderNumber = ordersPaymentDTO.getOrderNumber();
            log.info("调用updateStatus方法，更新订单状态");
            ordersMapper.updateStatus(toBeConfirmed,paid,check_out_time,orderNumber);

            // 付款成功后，通过webSocket向客户端浏览器发送消息 type orderId content
            // 获取订单id
            HashMap<String, Object> map = new HashMap<>();
            map.put("type",1); // 1来单提醒，2催单
            map.put("orderId",orderNumber);
            map.put("content","来单了，订单号:" + orderNumber);
            // 转为Json格式
            String json = JSON.toJSONString(map);
            webSocketServer.sendToAllClient(json);

            return vo;
        }

    /**
     * 各个状态的订单统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        // 待接单数量
        orderStatisticsVO.setConfirmed(ordersMapper.countStatus(Orders.TO_BE_CONFIRMED));
        // 待派送数量
        orderStatisticsVO.setDeliveryInProgress(ordersMapper.countStatus(Orders.CONFIRMED));
        // 派送中数量
        orderStatisticsVO.setToBeConfirmed(ordersMapper.countStatus(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    /**
     * 订单详情查询
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        Orders order = ordersMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.queryByOrderId(id);
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderDetailList(orderDetails);
        BeanUtils.copyProperties(order,orderVO);
        return orderVO;
    }

    /**
     * 确认订单
     * @param id
     */
    @Override
    public void confirmOrder(Long id) {
        Orders orderDB = ordersMapper.getById(id);
        if (orderDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(Orders.CONFIRMED);
        ordersMapper.update(order);
    }

    /**
     * 拒绝接单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejectionOrder(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orderDB = ordersMapper.getById(ordersRejectionDTO.getId());
        // 只有待接单状态的订单才能拒绝接单
        if (orderDB == null || !orderDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders order = new Orders();
        // 更新订单状态，拒绝接单，退款
        order.setId(orderDB.getId());
        order.setStatus(Orders.CANCELLED);
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        ordersMapper.update(order);
    }

    /**
     * 取消订单
     * @param id
     * @param cancelReason
     */
    @Override
    public void cancel(Long id, String cancelReason) {
        Orders orderDB = ordersMapper.getById(id);
        if (orderDB.getPayStatus() == 1){
            log.info("退款");
        }
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(cancelReason);
        ordersMapper.update(order);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orderDB = ordersMapper.getById(id);
        // 如果不存在或者订单状态不是待配送
        if (orderDB == null || !orderDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 更新订单状态，派送中
        Orders order = new Orders();
        order.setId(id);
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        ordersMapper.update(order);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = ordersMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        ordersMapper.update(orders);
    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders order = ordersMapper.getById(id);
        if (order == null){
          throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 基于WebSocket实现催单
        HashMap<String, Object> map = new HashMap<>();
        map.put("type",2); // 1来单提醒，2催单
        map.put("orderId",order.getNumber());
        map.put("content","搞快点，顾客催单了:" + order.getNumber());
        // 转为Json格式
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }
}

