package org.xyz.luckyjourney.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.objenesis.SpringObjenesis;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.constant.RedisConstant;
import org.xyz.luckyjourney.entity.vo.Model;
import org.xyz.luckyjourney.entity.vo.UserModel;
import org.xyz.luckyjourney.service.InterestPushService;
import org.xyz.luckyjourney.util.RedisCacheUtil;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InterestPushServiceImpl implements InterestPushService {

    @Autowired
    private  RedisCacheUtil redisCacheUtil;

    @Override
    @Async
    public void initUserModel(Long userId, List<String> labels) {
        String key = RedisConstant.USER_MODEL + userId;
        Map<Object,Object> map = new HashMap<>();
        if(!ObjectUtils.isEmpty(labels)){
            int size = labels.size();
            double probabilityValue = 100 / size;
            for(String label : labels) {
                map.put(label, probabilityValue);
            }
        }

        redisCacheUtil.hmset(key,map);
    }

    @Override
    @Async
    public void updateUserModel(UserModel userModel) {
        Long userId = userModel.getUserId();
        //游客不需要更新模型
        if(userId != null){
            final List<Model> models = userModel.getModels();

            //获取用户模型
            String key = RedisConstant.USER_MODEL + userId;
            Map<Object, Object> modelMap = redisCacheUtil.hmget(key);
            if(modelMap == null){
               modelMap = new HashMap<>();
            }

            for(Model model : models){
                if(modelMap.containsKey(model.getLabel())){
                    modelMap.put(model.getLabel(),Double.parseDouble(modelMap.get(model.getLabel()).toString()) + model.getScore());
                    Object o = redisCacheUtil.get(model.getLabel());
                    if(o == null || Double.parseDouble(o.toString()) <= 0.0){
                        modelMap.remove(o);
                    }
                }else {
                    modelMap.put(model.getLabel(),model.getScore());
                }
            }

            //防止数据膨胀
            final int labelSize = modelMap.size();
            for(Object o : modelMap.keySet()){
                modelMap.put(o,(Double.parseDouble(modelMap.get(o).toString()) + labelSize) / labelSize);
            }

            redisCacheUtil.hmset(key,modelMap);
        }
    }
}
