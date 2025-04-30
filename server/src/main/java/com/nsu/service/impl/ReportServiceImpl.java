package com.nsu.service.impl;
/*
  Date:2025/3/27
  Time:15:40
  @author llh 
 */

import com.nsu.dto.GoodsSalesDTO;
import com.nsu.entity.Orders;
import com.nsu.mapper.OrdersMapper;
import com.nsu.mapper.ReportMapper;
import com.nsu.mapper.UserMapper;
import com.nsu.service.ReportService;
import com.nsu.service.WorkSpaceService;
import com.nsu.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private WorkSpaceService workSpaceService;

    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        // 创建一个用于存储日期范围的ArrayList，元素类型为LocalDate
        List<LocalDate> dateList = new ArrayList<>();
        // 将起始日期添加进集合
        dateList.add(begin);
        // 循环遍历，将区间内每一天添加进集合，如果起始日期和结束日期相同，退出循环。
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 创建一个用于存储营业额的ArrayList，元素类型为Double
        List<Double> turnoverList = new ArrayList<>();
        // 创建一个HashMap，存储查询条件
        HashMap<String , Object> map = new HashMap<>();
        // 遍历日期集合，构造每一天的时间，并查询每一天的营业额然后添加进营业额集合
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            map.put("status", Orders.COMPLETED);
            map.put("begin",beginTime);
            map.put("end",endTime);

            // 查询营业额
            Double turnover = reportMapper.sumByMap(map);
            // 处理空值的情况，如无订单，则营业额为0.
            if (turnover == null){
                turnover = 0.0;
            }
            turnoverList.add(turnover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,",")) // 将日期集合转换为字符串，以逗号分隔
                .turnoverList(StringUtils.join(turnoverList,",")) // 将营业额集合转换为字符串，以逗号分隔
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 创建一个用于存储日期范围的ArrayList，元素类型为LocalDate
        ArrayList<LocalDate> dateList = new ArrayList<>();
        // 将每一天都存入集合中。
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String totalUserList = "";
        String newUserList = "";
        HashMap<String, LocalDateTime> map = new HashMap<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 查询每一天的用户总量
            map.put("end",endTime);
            Integer totalUserNum = userMapper.getUserByMap(map);
            if (totalUserNum == null){
                totalUserNum = 0;
            }
            totalUserList += totalUserNum + ",";
            // 查询每一天的新增用户
            map.put("begin",beginTime);
            map.put("end",endTime);
            Integer newNum = userMapper.getUserByMap(map);
            if (newNum == null){
                newNum = 0;
            }
            newUserList += newNum + ",";
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(totalUserList)
                .newUserList(newUserList)
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 创建一个用于存储日期范围的ArrayList，元素类型为LocalDate
        ArrayList<LocalDate> dateList = new ArrayList<>();
        // 将每一天都存入集合中。
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 每日订单数
        String orderCountList = "";
        // 每日有效订单数
        String validOrderCountList = "";
        // 订单总数
        Integer totalOrderCount = 0;
        // 有效订单数
        Integer validOrderCount = 0;
        // map
        HashMap<String, Object> map = new HashMap<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 订单总数
            map.put("begin",beginTime);
            map.put("end",endTime);
            totalOrderCount += ordersMapper.getOrderByMap(map);
            orderCountList += totalOrderCount + ",";
            // 有效订单数
            map.put("status",5);
            validOrderCount +=  ordersMapper.getOrderByMap(map);
            validOrderCountList += validOrderCount + ",";
        }
        // 订单完成率
        Double orderCompletionRate = validOrderCount / (totalOrderCount * 1.0);

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(orderCountList)
                .validOrderCountList(validOrderCountList)
                .totalOrderCount(totalOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .validOrderCount(validOrderCount)
                .build();
    }

    /**
     * 查询销售额前10的商品
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        // 将开始日期begin与当天的最小时间（00:00）组合成LocalDateTime对象，表示起始时间点
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
// 将结束日期end与当天的最大时间（23:59:59.999999999）组合成LocalDateTime对象，表示截止时间点
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
// 调用订单Mapper接口方法，查询在beginTime到endTime时间范围内销量前十的商品数据，返回DTO列表
        List<GoodsSalesDTO> goodsSalesDTOList = ordersMapper.getSalesTop10(beginTime, endTime);

// 使用Stream处理DTO列表，提取商品名称字段，通过逗号拼接成字符串（示例："商品1,商品2,..."）
//        String nameList = StringUtils.join(
//                goodsSalesDTOList.stream()
//                        .map(GoodsSalesDTO::getName)
//                        .collect(Collectors.toList()),
//                ","
//        );

        String nameList = "";
        String numberList = "";
        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList) {
            nameList += goodsSalesDTO.getName() + ",";
            numberList += goodsSalesDTO.getNumber() + ",";
        }

// 使用Stream处理DTO列表，提取商品销量字段，通过逗号拼接成字符串（示例："100,200,..."）
//        String numberList = StringUtils.join(
//                goodsSalesDTOList.stream()
//                        .map(GoodsSalesDTO::getNumber)
//                        .collect(Collectors.toList()),
//                ","
//        );

// 构建并返回销售TOP10报表视图对象，包含拼接好的商品名称列表和销量列表字符串
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**导出近30天的运营数据报表
     * @param response
     **/
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        //查询概览运营数据，提供给Excel模板文件
        BusinessDataVO businessData = workSpaceService.getBusinessData(LocalDateTime.of(begin,LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于提供好的模板文件创建一个新的Excel表格对象
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //获得Excel文件中的一个Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue(begin + "至" + end);
            //获得第4行
            XSSFRow row = sheet.getRow(3);
            //获取单元格
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //准备明细数据
                businessData = workSpaceService.getBusinessData(LocalDateTime.of(date,LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            //通过输出流将文件下载到客户端浏览器中
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭资源
            out.flush();
            out.close();
            excel.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}

