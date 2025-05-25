package org.xyz.luckyjourney.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.video.Type;
import org.xyz.luckyjourney.entity.vo.*;

import java.util.List;
import java.util.Set;

public interface UserService extends IService<User> {

    Boolean register(RegisterVO registerVO) throws Exception;

    Boolean findPassword(FindPWVO findPWVO);

    UserVO getInfo(Long userId);

    void updateUser(UpdateUserVO updateUserVO,Long userId);

    List<User> list(Set<Long> userIds);

    Boolean follows(Long followsId);

    IPage getFollows(Long userId, BasePage basepage);

    IPage getFans(Long userId,BasePage basePage);

    void subscribe(Set<Long> typeIds);

    List<Type> listSubscribeType(Long userId);

    List<Type> listNoSubscribe(Long userId);
}
