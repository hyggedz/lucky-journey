package org.xyz.luckyjourney.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.user.Follow;

@Service
public interface FollowService extends IService<Follow>{
    /**
     * 获取粉丝数量
     *
     * @param userId
     * @return
     */
    Long getFansCount(Long userId);


    /**
     * 获取关注数量
     *
     * @param userId
     * @return
     */
    Long getFollowCount(Long userId);
}
