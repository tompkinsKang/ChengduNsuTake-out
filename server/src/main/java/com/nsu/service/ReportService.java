package com.nsu.service;
/*
  Date:2025/3/27
  Time:15:40
  @author llh 
 */

import com.nsu.vo.OrderReportVO;
import com.nsu.vo.SalesTop10ReportVO;
import com.nsu.vo.TurnoverReportVO;
import com.nsu.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {

    /**
     * 根据时间区间统计营业额
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnover(LocalDate begin, LocalDate end);

    /**
     * 根据时间区间统计用户数据
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    void exportBusinessData(HttpServletResponse response);
}

