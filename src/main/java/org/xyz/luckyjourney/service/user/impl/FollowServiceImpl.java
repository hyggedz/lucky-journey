package org.xyz.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.xyz.luckyjourney.entity.user.Follow;
import org.xyz.luckyjourney.mapper.user.FollowMapper;
import org.xyz.luckyjourney.service.user.FollowService;

public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Override
    public Long getFansCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId,userId));
    }

    @Override
    public Long getFollowCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId,userId));
    }
}
