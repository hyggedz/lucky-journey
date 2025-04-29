package org.xyz.luckyjourney.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

//为自动更新gmtCreated和gmtUpdated字段提供方法
@Configuration
public class SetDateFieldConfig implements MetaObjectHandler {

    //插入字段时更新时间
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("gmtCreated",new Date(),metaObject);
        this.setFieldValByName("gmtUpdated",new Date(),metaObject);
    }

    //更新字段时更新时间
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("gmtUpdated",new Date(),metaObject);
    }
}
