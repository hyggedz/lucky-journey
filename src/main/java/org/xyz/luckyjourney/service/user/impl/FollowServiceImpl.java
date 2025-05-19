package org.xyz.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.constant.RedisConstant;
import org.xyz.luckyjourney.entity.user.Follow;
import org.xyz.luckyjourney.entity.vo.BasePage;
import org.xyz.luckyjourney.exception.BaseException;
import org.xyz.luckyjourney.mapper.user.FollowMapper;
import org.xyz.luckyjourney.service.user.FollowService;
import org.xyz.luckyjourney.util.RedisCacheUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Long getFansCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId,userId));
    }

    @Override
    public Long getFollowCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId,userId));
    }

    @Override
    public Boolean follows(Long userId, Long followsId) {
        if(userId.equals(followsId)){
            throw new BaseException("你不能关注自己");
        }

        //保存唯一索引，失败则删除
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setFollowId(followsId);
        try {
            save(follow);
            final Date date = new Date();
            //自己关注表增加
            redisTemplate.opsForZSet().add(RedisConstant.USER_FOLLOW + userId,followsId,date.getTime());
            //对方粉丝表增加
            redisTemplate.opsForZSet().add(RedisConstant.USER_FANS + followsId,userId,date.getTime());
        }catch (Exception e){
            remove(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId,userId).eq(Follow::getFollowId,followsId));

            //TODO 获取关注者的视频
            //TODO 删除收件箱的视频

            redisTemplate.opsForZSet().remove(RedisConstant.USER_FOLLOW + userId,followsId);
            redisTemplate.opsForZSet().remove(RedisConstant.USER_FANS + followsId,userId);

            return false;
        }
        return true;
    }

    @Override
    public Collection<Long> getFollows(Long userId, BasePage basePage) {
        if(basePage == null){
            final Set<Object> followsId = redisCacheUtil.zGet(RedisConstant.USER_FOLLOW + userId);
            if(ObjectUtils.isEmpty(followsId)){
                return Collections.EMPTY_SET;
            }
            return followsId.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
        }

        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(RedisConstant.USER_FOLLOW + userId, basePage.getPage(), basePage.getLimit());
        //redis崩了，从db拿
        if(ObjectUtils.isEmpty(typedTuples)){
            List<Follow> records = page(basePage.page(), new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId).orderByDesc(Follow::getGmtCreated)).getRecords();
            if(ObjectUtils.isEmpty(records)){
                return Collections.EMPTY_LIST;
            }
            return records.stream().map(Follow::getFollowId).collect(Collectors.toList());
        }
        return typedTuples.stream().map(t -> Long.parseLong(t.getValue().toString())).collect(Collectors.toList());
    }
}
