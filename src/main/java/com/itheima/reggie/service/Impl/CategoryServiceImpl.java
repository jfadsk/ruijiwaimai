package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     * 删除分类
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        // 先进行查询是否存在菜品
        QueryWrapper<Dish> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id", id);
        int count = dishService.count(wrapper);

        if (count > 0) {
            // 表示存在菜品 不能删除
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        QueryWrapper<Setmeal> wrapperSetmeal = new QueryWrapper<>();
        wrapperSetmeal.eq("category_id", id);
        // 查询是否存在套餐
        int setmealCount = setmealService.count(wrapperSetmeal);

        if (setmealCount > 0) {
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

       super.removeById(id);

    }
}
