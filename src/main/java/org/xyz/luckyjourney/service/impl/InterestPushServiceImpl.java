package org.xyz.luckyjourney.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.objenesis.SpringObjenesis;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.constant.RedisConstant;
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
        Map<String,Object> map = new HashMap<>();
        if(!ObjectUtils.isEmpty(labels)){
            int size = labels.size();
            double probabilityValue = 100 / size;
            for(String label : labels) {
                map.put(label, probabilityValue);
            }
        }

        redisCacheUtil.hmset(key,map);
    }
}
