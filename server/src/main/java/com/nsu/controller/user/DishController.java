package com.nsu.controller.user;

import com.nsu.constant.StatusConstant;
import com.nsu.entity.Dish;
import com.nsu.result.Result;
import com.nsu.service.DishService;
import com.nsu.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        // 构造redis存储的key，形式为dish_分类id
        String Key = "dish_" + categoryId;
        // 查询redis数据库中是否存在该数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(Key);
        // 如果存在则直接返回，不需要操作mysql
        if (list != null && list.size() > 0) {
            return Result.success(list);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        // 如果不存在，就查询mysql，将数据放到redis中
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(Key,list);
        return Result.success(list);
    }

}
