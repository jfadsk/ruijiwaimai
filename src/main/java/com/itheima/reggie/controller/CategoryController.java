package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;



    /**
     * 新增分类
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     */

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("sort");

        categoryService.page(pageInfo, wrapper);
        return R.success(pageInfo);
    }

    // 删除分类
    @DeleteMapping
    public R<String> delete(Long ids) {
        // 在删除前先进行查询是否存在菜品或者套餐
        categoryService.deleteById(ids);
        return R.success("删除成功");
    }

    // 修改分类
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        // 通过type的类型进行查询
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        if (category.getType() != null) {
            wrapper.eq("type", category.getType());
        }
        List<Category> list = categoryService.list(wrapper);
        return R.success(list);
    }
}
