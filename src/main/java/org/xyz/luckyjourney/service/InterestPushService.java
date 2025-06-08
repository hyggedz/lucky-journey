package org.xyz.luckyjourney.service;

import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.vo.UserModel;

import java.util.Collection;
import java.util.List;


public interface InterestPushService {
    void initUserModel(Long userId, List<String> labels);

    void updateUserModel(UserModel userModel);

    Collection<Long> listVideoIdByUserModel(User user);

    Collection<Long> listVideoIdByTypeId(Long typeId);

    Collection<Long> listVideoIdByLabes(Collection<String> labelNames);
}
