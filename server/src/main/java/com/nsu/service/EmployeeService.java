package com.nsu.service;

import com.nsu.dto.EmployeeDTO;
import com.nsu.dto.EmployeeLoginDTO;
import com.nsu.dto.EmployeePageQueryDTO;
import com.nsu.entity.Employee;
import com.nsu.result.PageResult;

public interface EmployeeService {

    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeDTO employeeDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrStop(Integer status, Long id);

    Employee findById(Long id);

    void update(EmployeeDTO dto);
}
