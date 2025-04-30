package com.nsu.mapper;

import com.github.pagehelper.Page;
import com.nsu.annotation.AutoFill;
import com.nsu.dto.EmployeePageQueryDTO;
import com.nsu.entity.Employee;
import com.nsu.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 插入员工
     * @param employee
     */
    @Insert("insert into employee (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user, status) " +
            "VALUES (#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser},#{status})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 修改员工
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);


    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee findById(Long id);
}
