package org.xyz.luckyjourney.service.impl;

import com.sun.mail.imap.protocol.BODY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.objenesis.SpringObjenesis;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.constant.RedisConstant;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.vo.Model;
import org.xyz.luckyjourney.entity.vo.UserModel;
import org.xyz.luckyjourney.service.InterestPushService;
import org.xyz.luckyjourney.service.video.TypeService;
import org.xyz.luckyjourney.util.RedisCacheUtil;
import sun.awt.image.BufImgVolatileSurfaceManager;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class InterestPushServiceImpl implements InterestPushService {

    @Autowired
    private  RedisCacheUtil redisCacheUtil;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private TypeService typeService;

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


    @Override
    public Collection<Long> listVideoIdByUserModel(User user) {
        //构建结果集
        Set<Long> videoIds = new HashSet<>(10);

        if(user != null){
            //获取用户模型
            Map<Object, Object> modelMap = redisCacheUtil.hmget(RedisConstant.USER_MODEL + user.getId());
            if(!ObjectUtils.isEmpty(modelMap)){
                //生成概率数组
                String [] probabilityArray = initProbabilityArray(modelMap);
                Boolean sex = user.getSex();
                //随机抽取标签
                List<String> labels = new ArrayList<>();
                if(probabilityArray != null && probabilityArray.length > 0){
                    Random random = new Random();

                    for(int i = 0;i < 8;++i){
                        int x = random.nextInt(probabilityArray.length);
                        String label = probabilityArray[x];
                        labels.add(label);
                    }
                }
                //从标签库获取视频id
                String t = RedisConstant.SYSTEM_STOCK;

                List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    for (String label : labels) {
                        String key = t + label;
                        connection.sRandMember(key.getBytes());
                    }
                    return null;
                });
                //获取到视频Id
                Set<Long> originIds = list.stream().filter(id -> id != null).map(o -> Long.parseLong(o.toString())).collect(Collectors.toSet());

                //视频Id去重
                String RemoveHistoryKey = RedisConstant.HISTORY_VIDEO;
                List list1 = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                    for(Long id : originIds){
                        String k = RemoveHistoryKey + id + ":" + user.getId();
                        connection.get(k.getBytes());
                    }
                    return null;
                } );

                list1 = (List) list1.stream().filter(o -> !ObjectUtils.isEmpty(o)).collect(Collectors.toList());
                if(!ObjectUtils.isEmpty(list1)){
                    for(Object item:list1){
                        Long id = Long.parseLong(item.toString());
                        if(originIds.contains(id)){
                            originIds.remove(id);
                        }
                    }
                }

                videoIds.addAll(originIds);
                //sex视频
                Long id = randomVideoId(sex);
                if(id != null){
                    videoIds.add(id);
                }

                //返回
                return videoIds;
            }
        }
        //游客
        List<String> labels = typeService.random10Labels();
        List<String> labelsName = new ArrayList<>();
        int size = labels.size();
        Random random = new Random();

        for(int i = 0;i < 10;++i){
            int ranIndex = random.nextInt(size);
            labelsName.add(RedisConstant.SYSTEM_STOCK + labels.get(ranIndex));
        }

        List<Object> list = redisCacheUtil.sRandom(labelsName);
        if(!ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(o -> !ObjectUtils.isEmpty(o)).map(id -> Long.parseLong(id.toString())).collect(Collectors.toSet());
        }
        return videoIds;
    }

    @Override
    public Collection<Long> listVideoIdByTypeId(Long typeId) {
        List<Object> list = redisTemplate.opsForSet().randomMembers(RedisConstant.SYSTEM_TYPE_STOCK + typeId, 12);
        HashSet<Long> res = new HashSet<>();

        if(!ObjectUtils.isEmpty(list)){
            for (Object o : list){
                if(!ObjectUtils.isEmpty(o)){
                    res.add(Long.parseLong(o.toString()));
                }
            }
        }

        return res;
    }

    @Override
    public Collection<Long> listVideoIdByLabes(Collection<String> labelNames) {
        final ArrayList<String> labelKeys = new ArrayList<>();
        for (String labelName : labelNames) {
            labelKeys.add(RedisConstant.SYSTEM_STOCK + labelName);
        }
        Set<Long> videoIds = new HashSet<>();
        final List<Object> list = redisCacheUtil.sRandom(labelKeys);
        if (!ObjectUtils.isEmpty(list)){
            videoIds = list.stream().filter(id ->!ObjectUtils.isEmpty(id)).map(id -> Long.valueOf(id.toString())).collect(Collectors.toSet());
        }
        return videoIds;
    }

    // 初始化概率数组 -> 存储的是标签 : [游戏，游戏，宠物，花朵]
    public  String [] initProbabilityArray(Map<Object,Object> modelMap){
        Map<String,Integer> probabilityMap = new HashMap<>();
        AtomicInteger n = new AtomicInteger(0);

        modelMap.forEach((k,v)-> {
            Double value = (Double) v;
            int probability = (int) (value * 100);
            n.getAndAdd(probability);
            probabilityMap.put(k.toString(),probability);
        });

        String [] probabilityArray = new String[n.get()];
        //初始化数组
        AtomicInteger index = new AtomicInteger(0);
        probabilityMap.forEach((labels,p) -> {
            int i = index.get();
            int limit = i + p;
            while (i < limit){
                probabilityArray[i] = labels;
                i++;
            }
            index.set(limit);
        });

        return probabilityArray;
    }

    public Long randomVideoId(Boolean sex){
        String key = RedisConstant.SYSTEM_STOCK + (sex ? "美女" : "宠物");
        Object o = redisCacheUtil.sRandom(key);
        if(o != null){
            return  Long.parseLong(o.toString());
        }
        return null;
    }
}
