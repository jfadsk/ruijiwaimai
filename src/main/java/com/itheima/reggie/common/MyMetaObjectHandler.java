package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 使用mybatPlus的自动填充策略
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    // 在创建时填充
    @Override
    public void insertFill(MetaObject metaObject) {
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        // 获取管理员的id
        Long currentId = BaseContext.getCurrentId();
        metaObject.setValue("createUser", currentId);
        metaObject.setValue("updateUser", currentId);

    }

    // 在其更新时填充
    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
