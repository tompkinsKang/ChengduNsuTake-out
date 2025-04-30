package com.nsu.controller.admin;
/*
  Date:2025/3/27
  Time:15:34
  @author llh 
 */

import com.nsu.result.Result;
import com.nsu.service.ReportService;
import com.nsu.vo.OrderReportVO;
import com.nsu.vo.SalesTop10ReportVO;
import com.nsu.vo.TurnoverReportVO;
import com.nsu.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController("adminReportController")
@RequestMapping("/admin/report")
@Api(tags = "管理端报表相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额数据统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额数据统计")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {
        log.info("根据时间区间统计数据营业额：begin:{},end:{}", begin, end);
        TurnoverReportVO turnoverReportVO = reportService.getTurnover(begin,end);
        return Result.success(turnoverReportVO);
    }

    /**
     * 用户数据统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户数据统计")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("用户数量统计,begin:{},end:{}", begin, end);
        UserReportVO userReportVO = reportService.getUserStatistics(begin,end);
        return Result.success(userReportVO);
    }

    /**
     * 订单数据统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单数据统计")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("订单数据统计：{}，{}",begin,end);
        OrderReportVO orderReportVO = reportService.getOrderStatistics(begin,end);
        return Result.success(orderReportVO);
    }

    @GetMapping("/top10")
    @ApiOperation("查询销售额前10的商品")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("查询销售额前10的商品：{}，{}",begin,end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.getSalesTop10(begin,end);
        return Result.success(salesTop10ReportVO);
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @GetMapping("/export")
    @ApiOperation("导出运营数据报表")
    public void export(HttpServletResponse response){
        reportService.exportBusinessData(response);
    }

}

