package org.xyz.luckyjourney.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.user.Follow;
import org.xyz.luckyjourney.entity.vo.BasePage;

import java.util.Collection;
import java.util.Collections;


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

    Boolean follows(Long userId,Long followsId);

    Collection<Long> getFollows(Long userId, BasePage basePage);

    Collection<Long> getFans(Long userId, BasePage basePage);
}
