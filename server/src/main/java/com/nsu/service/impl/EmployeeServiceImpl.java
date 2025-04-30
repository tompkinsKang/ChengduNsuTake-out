package com.nsu.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nsu.constant.MessageConstant;
import com.nsu.constant.PasswordConstant;
import com.nsu.constant.StatusConstant;
import com.nsu.dto.EmployeeDTO;
import com.nsu.dto.EmployeeLoginDTO;
import com.nsu.dto.EmployeePageQueryDTO;
import com.nsu.entity.Employee;
import com.nsu.exception.AccountLockedException;
import com.nsu.exception.AccountNotFoundException;
import com.nsu.exception.PasswordErrorException;
import com.nsu.mapper.EmployeeMapper;
import com.nsu.result.PageResult;
import com.nsu.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    // 新增员工
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //  对象的属性拷贝
        BeanUtils.copyProperties(employeeDTO,employee);
        // 设置账号状态
        employee.setStatus(StatusConstant.ENABLE);
        // 设置密码
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
//        // 时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        //设置当前记录创建人id和修改人id
//        employee.setCreateUser(BaseContext.getCurrentId());//目前写个假数据，后期修改
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);

    }

    // 分页查询员工信息

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 使用分页插件,设置分页信息
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        // 查询数据
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        // 封装数据
        long total = page.getTotal();
        List<Employee> result = page.getResult();
        return new PageResult(total,result);
    }

    // 启用或停用员工
    @Override
    public void startOrStop(Integer status, Long id) {
        // 为适用更新功能，传入一个对象
//        Employee employee = new Employee();
//        employee.setId(id);
//        employee.setStatus(status);

        // 可使用构建器，在构建对象时，指定属性
        Employee employee = Employee.builder()
                        .status(status)
                        .id(id)
                        .build();

        employeeMapper.update(employee);
    }

    @Override
    public Employee findById(Long id) {
        Employee employee = employeeMapper.findById(id);
        employee.setPassword("******");
        return employee;
    }

    @Override
    public void update(EmployeeDTO dto) {
        Employee employee = new Employee();
        // 对象的属性拷贝
        BeanUtils.copyProperties(dto,employee);
        // 设置修改时间和修改人id,这里的修改人id是当前登录用户的id
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);

    }

}
