package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> pageDto = new Page<>();

        QueryWrapper<Setmeal> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            wrapper.eq("name", name);
        }
        wrapper.orderByDesc("update_time");
        setmealService.page(pageInfo, wrapper);

        // 将其除套餐信息外的其他信息进行封装
        BeanUtils.copyProperties(pageInfo, pageDto, "records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> collect = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());

        pageDto.setRecords(collect);
        return R.success(pageDto);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.delete(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 根据分类id查询套餐信息
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        Long categoryId = setmeal.getCategoryId();
        if (StringUtils.isEmpty(categoryId)) {
            throw new CustomException("系统错误！！");
        }
        if (StringUtils.isEmpty(setmeal.getStatus())) {
            throw new CustomException("系统错误");
        }

        QueryWrapper<Setmeal> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id", categoryId);
        wrapper.eq("status", 1);

        List<Setmeal> setmeals = setmealService.list(wrapper);
        return R.success(setmeals);
    }

    @GetMapping("{id}")
    public R<SetmealDto> getById(@PathVariable("id") Long id) {
        if (id == null) {
            throw new CustomException("系统错误！");
        }
        Setmeal setmeal = setmealService.getById(id);

        QueryWrapper<SetmealDish> wrapper = new QueryWrapper<>();
        wrapper.eq("setmeal_id", id);

        List<SetmealDish> setmealDishes = setmealDishService.list(wrapper);

        Category category = categoryService.getById(setmeal.getCategoryId());

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(setmealDishes);
        setmealDto.setCategoryName(category.getName());
        return R.success(setmealDto);
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateSetmeal(setmealDto);
        return R.success("修改成功");
    }

    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable("status") int status, @RequestParam List<Long> ids) {
        ids.stream().forEach((id) -> {
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        });

        return R.success("操作成功");
    }

}
