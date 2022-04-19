package com.itheima.reggie.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;


    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 添加套餐信息
        this.save(setmealDto);

        // 保存套餐关系表
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> collect = setmealDishes.stream().map((item) -> {
            Long id = setmealDto.getId();
            item.setSetmealId(id);
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(collect);
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        // 查询是否可以删除 只有停售才可以删除
        QueryWrapper<Setmeal> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        wrapper.eq("status", 1);
        int count = this.count(wrapper);
        // 如果大于0则不可以删除
        if (count > 0) {
            // 表示还在售卖中不可以删除
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        // 说明可以删除
        this.removeByIds(ids);

        // 删除套餐的关联信息
        QueryWrapper<SetmealDish> wrapperD = new QueryWrapper<>();
        wrapperD.in("setmeal_id", ids);
        setmealDishService.remove(wrapperD);
    }

    @Override
    public void updateSetmeal(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        // 将其关联的菜品先进行删除在添加
        QueryWrapper<SetmealDish> wrapperDelete = new QueryWrapper<>();
        wrapperDelete.eq("setmeal_id", setmealDto.getId());
        setmealDishService.remove(wrapperDelete);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        // 给每个套餐关联的菜品信息添加套餐id信息
        List<SetmealDish> collect = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(collect);
    }


}
