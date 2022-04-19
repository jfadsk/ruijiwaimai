package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品添加controller
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        QueryWrapper<Dish> wrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(name)) {
            wrapper.like("name", name);
        }
        // 对其进行排序
        wrapper.orderByDesc("update_time");

        // 进行查询
        dishService.page(pageInfo, wrapper);

        // 当前的数据在其pageInfo中 但不复制records中的内容
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        // 对其集合中的数据进行处理
        List<DishDto> dishDtoList = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            // 将其Dish中的数据复制到其DishDto
            BeanUtils.copyProperties(item, dishDto);
            // 获取分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (!StringUtils.isEmpty(category)) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable("id") Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        QueryWrapper<Dish> wrapper = new QueryWrapper<>();

        if (!StringUtils.isEmpty(dish.getCategoryId())) {
            wrapper.eq("category_id", dish.getCategoryId());
        }

        // 菜品的状态必须为1
        wrapper.eq("status", 1);

        List<Dish> list = dishService.list(wrapper);

        // 根据菜品id进行查询口味信息
        List<DishDto> collect = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            // 查询分类的name
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            dishDto.setCategoryName(category.getName());

            QueryWrapper<DishFlavor> wrapperFlavor = new QueryWrapper<>();
            wrapperFlavor.eq("dish_id", item.getId());


            List<DishFlavor> flavorList = dishFlavorService.list(wrapperFlavor);
            dishDto.setFlavors(flavorList);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(collect);
    }

    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") int status,@RequestParam("ids") List<Long> ids) {
        int count = getCount(ids);
        if (count > 0) {
            // 表示该菜品在其套餐中不能进行删除
            throw new CustomException("菜品在其套餐中使用无法进行停售");
        }
        ids.stream().forEach((item) -> {
            Dish dish = dishService.getById(item);
            dish.setStatus(status);
            dishService.updateById(dish);
        });

        return R.success("修改成功");
    }

    /**
     * 查看菜品是否与其套餐关联
     * @param ids
     * @return
     */
    private int getCount(List<Long> ids) {
        // 在停售前先查看是否在其套餐中使用
        QueryWrapper<SetmealDish> wrapper = new QueryWrapper<>();
        wrapper.in("dish_id", ids);

        int count = setmealDishService.count(wrapper);
        return count;
    }

    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        // 查看有没有被套餐使用
        int count = this.getCount(ids);
        if (count > 0) {
            throw new CustomException("该菜品正在套餐中售卖 无法进行删除");
        }
        // 需要将其口味信息一并进行删除

        ids.forEach((id) -> {
            Dish dish = dishService.getById(id);
            if (dish.getStatus() == 1) {
                throw new CustomException("正在售卖中无法删除");
            }
        });

        ids.stream().forEach((item) -> {
            // 删除菜品
            dishService.removeById(item);
            // 删除菜品关联的口味信息
            QueryWrapper<DishFlavor> wrapper = new QueryWrapper<>();
            wrapper.eq("dish_id", item);
            dishFlavorService.remove(wrapper);
        });

        return R.success("删除成功！！");
    }
}
