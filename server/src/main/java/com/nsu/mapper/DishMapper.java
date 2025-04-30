package com.nsu.mapper;

import com.github.pagehelper.Page;
import com.nsu.annotation.AutoFill;
import com.nsu.dto.DishPageQueryDTO;
import com.nsu.entity.Dish;
import com.nsu.enumeration.OperationType;
import com.nsu.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {
    /**
     * 根据分类id查询菜品数量
     * @param id
     * @return
     */
    @Select("select count(id) from sky_take_out.dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Delete("delete from sky_take_out.dish where id=#{id}")
    void delete(Long ids);

    @Select("select * from sky_take_out.dish where id=#{id}")
    Dish getById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    void deleteByIds(List<Long> ids);

    @Select("select * from sky_take_out.dish where category_id = #{categoryId}")
    List<Dish> getByCategoryId(Long categoryId);

    @Select("select a.* from sky_take_out.dish a left join sky_take_out.setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{id}")
    List<Dish> getBySetmealId(Long id);

    Integer countByMap(Map map);
}
