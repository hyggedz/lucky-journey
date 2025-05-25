package org.xyz.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.user.UserSubscribe;
import org.xyz.luckyjourney.mapper.user.UserSubscribeMapper;
import org.xyz.luckyjourney.service.user.UserSubscribeService;

@Service
public class UserSubscribeServiceImpl extends ServiceImpl<UserSubscribeMapper, UserSubscribe> implements UserSubscribeService {
}
