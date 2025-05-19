package org.xyz.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.ObjectUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.constant.RedisConstant;
import org.xyz.luckyjourney.entity.user.Favorites;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.vo.*;
import org.xyz.luckyjourney.exception.BaseException;
import org.xyz.luckyjourney.holder.UserHolder;
import org.xyz.luckyjourney.mapper.user.UserMapper;
import org.xyz.luckyjourney.service.user.FavoritesService;
import org.xyz.luckyjourney.service.user.FollowService;
import org.xyz.luckyjourney.service.user.UserService;
import org.xyz.luckyjourney.util.RedisCacheUtil;

import java.util.*;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private FollowService followService;

    @Override
    public UserVO getInfo(Long userId) {
        User user = this.getById(userId);
        if(ObjectUtils.isEmpty(user)){
            return new UserVO();
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);

        userVO.setFans(followService.getFansCount(userId));
        userVO.setFollow(followService.getFollowCount(userId));

        return userVO;
    }

    @Override
    public Boolean register(RegisterVO registerVO) throws Exception {
        //检测当前邮箱是否已经注册过
        final long count = count(new LambdaQueryWrapper<User>().eq(User::getEmail,registerVO.getEmail()));
        if (count == 1){
            throw new BaseException("邮箱已被注册");
        }

        //获取邮箱验证码
        final String code = registerVO.getCode();

        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + registerVO.getEmail());
        if(o == null){
            throw new BaseException("验证码为空");
        }
        //对比邮箱验证码是否正确
        if(!code.equals(o)){
            return false;
        }

        //创建User、填充字段
        User user = new User();
        user.setEmail(registerVO.getEmail());
        user.setNickName(registerVO.getNickName());
        user.setDescription("这个人很懒。。。");
        user.setPassword(registerVO.getPassword());

        //存入数据库
        save(user);

        Favorites favorites = new Favorites();
        favorites.setUserId(user.getId());
        favorites.setName("默认收藏夹");
        favoritesService.save(favorites);

        user.setDefaultFavoritesId(favorites.getId());
        updateById(user);

        //注册成功
        return true;
    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {
       //验证
        Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + findPWVO.getEmail());
        if(o == null){
            return  false;
        }

        if(!o.toString().equals(findPWVO.getCode())){
            return false;
        }


        //修改
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getEmail,findPWVO.getEmail());

        wrapper.set(User::getPassword,findPWVO.getNewPassword());

        return update(wrapper);
    }

    @Override
    public List<User> list(Set<Long> userIds) {
        return list(new LambdaQueryWrapper<User>().in(User::getId,userIds)
                .select(User::getId,User::getSex,User::getAvatar,User::getDescription));

    }

    @Override
    public void updateUser(UpdateUserVO updateUserVO, Long userId) {
        User oldUser = getById(userId);

        //昵称
        if(!oldUser.getNickName().equals(updateUserVO.getNickName())){
            //TODO 审核

            oldUser.setNickName(updateUserVO.getNickName());
        }

        //头像
        if(!Objects.equals(oldUser.getAvatar(),updateUserVO.getAvatar())){
            //TODO 审核

            oldUser.setAvatar(updateUserVO.getAvatar());
        }

        //简介 description
        if(!ObjectUtils.isEmpty(updateUserVO.getDescription()) && !updateUserVO.getDescription().equals(oldUser.getDescription())){
            //TODO 审核

            oldUser.setDescription(updateUserVO.getDescription());
        }

        //性别
        oldUser.setSex(updateUserVO.getSex());


        //收藏夹
        if(!ObjectUtils.isEmpty(updateUserVO.getDefaultFavoritesId())){
            favoritesService.exist(userId,updateUserVO.getDefaultFavoritesId());
        }

        oldUser.setDefaultFavoritesId(updateUserVO.getDefaultFavoritesId());
    }

    @Override
    public Boolean follows(Long followsId) {
        Long userId = UserHolder.get();
        return followService.follows(userId,followsId);
    }

    @Override
    public Page<User> getFollows(Long userId, BasePage basepage) {
        Page<User> page = new Page<>();

        //获取followsId
        final Collection<Long> followsId = followService.getFollows(userId,basepage);
        if(ObjectUtils.isEmpty(followsId)){
            return  page;
        }

        //获取粉丝列表
        HashSet<Long> fans = new HashSet<>();
        fans.addAll(followService.getFans(userId,null));

        HashMap<Long,Boolean> map = new HashMap<>();
        for(Long followId : followsId){
            map.put(followId,fans.contains(followId));
        }

        //查询User并且设置字段
        final ArrayList<User> users = new ArrayList<>();
        final HashMap<Long,User> userMap = this.getBaseUserInfoToMap(followsId);
        for(Long followId : followsId){
            User user = userMap.get(followId);
            user.setEach(map.get(user.getId()));
            users.add(user);
        }
        page.setRecords(users);
        page.setTotal(users.size());

        return page;
    }
}
