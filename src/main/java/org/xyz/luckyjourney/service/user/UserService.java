package org.xyz.luckyjourney.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.vo.FindPWVO;
import org.xyz.luckyjourney.entity.vo.RegisterVO;
import org.xyz.luckyjourney.entity.vo.UpdateUserVO;
import org.xyz.luckyjourney.entity.vo.UserVO;

import java.util.List;
import java.util.Set;

public interface UserService extends IService<User> {

    Boolean register(RegisterVO registerVO) throws Exception;

    Boolean findPassword(FindPWVO findPWVO);

    UserVO getInfo(Long userId);

    void updateUser(UpdateUserVO updateUserVO,Long userId);

    List<User> list(Set<Long> userIds);
}
