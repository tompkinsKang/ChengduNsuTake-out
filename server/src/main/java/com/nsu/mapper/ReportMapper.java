package com.nsu.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface ReportMapper {

    /**
     * 根据动态条件统计营业额
     *
     * @param map
     */
    Double sumByMap(Map map);

}
