package com.nsu.mapper;

import com.github.pagehelper.Page;
import com.nsu.annotation.AutoFill;
import com.nsu.dto.SetmealPageQueryDTO;
import com.nsu.entity.Setmeal;
import com.nsu.enumeration.OperationType;
import com.nsu.vo.DishItemVO;
import com.nsu.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {
    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from sky_take_out.setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    @Select("SELECT * from sky_take_out.setmeal where id = #{id}")
    Setmeal getById(Long id);

    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 动态条件查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from sky_take_out.setmeal_dish sd left join sky_take_out.dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    @Update("update sky_take_out.setmeal set status = #{status} where id = #{id}")
    @AutoFill(OperationType.UPDATE)
    void openOrCloseSetmeal(Long id, Integer status);

    Integer countByMap(Map map);
}
