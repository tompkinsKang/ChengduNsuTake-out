package com.nsu.controller.admin;

import com.nsu.constant.JwtClaimsConstant;
import com.nsu.dto.EmployeeDTO;
import com.nsu.dto.EmployeeLoginDTO;
import com.nsu.dto.EmployeePageQueryDTO;
import com.nsu.entity.Employee;
import com.nsu.properties.JwtProperties;
import com.nsu.result.PageResult;
import com.nsu.result.Result;
import com.nsu.service.EmployeeService;
import com.nsu.utils.JwtUtil;
import com.nsu.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    // 分页查询员工信息
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询：{}",employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping()
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工：{}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用或停用员工")
    public Result startOrStop(@PathVariable("status") Integer status,Long id){
    log.info("启用或停用员工：status={},id={}",status,id);
    employeeService.startOrStop(status,id);
    return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
    public Result<Employee> findById(@PathVariable Long id){
        log.info("根据id查询员工：id={}",id);
        Employee employee = employeeService.findById(id);
        return Result.success(employee);
    }

    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result update(@RequestBody EmployeeDTO dto){
        log.info("编辑员工信息,参数：{}",dto);
        employeeService.update(dto);
        return Result.success();
    }
}
