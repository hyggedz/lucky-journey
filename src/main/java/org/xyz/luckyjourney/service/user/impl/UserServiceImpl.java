package org.xyz.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.constant.RedisConstant;
import org.xyz.luckyjourney.entity.user.Favorites;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.user.UserSubscribe;
import org.xyz.luckyjourney.entity.video.Type;
import org.xyz.luckyjourney.entity.vo.*;
import org.xyz.luckyjourney.exception.BaseException;
import org.xyz.luckyjourney.holder.UserHolder;
import org.xyz.luckyjourney.mapper.user.UserMapper;
import org.xyz.luckyjourney.service.InterestPushService;
import org.xyz.luckyjourney.service.user.FavoritesService;
import org.xyz.luckyjourney.service.user.FollowService;
import org.xyz.luckyjourney.service.user.UserService;
import org.xyz.luckyjourney.service.user.UserSubscribeService;
import org.xyz.luckyjourney.service.video.TypeService;
import org.xyz.luckyjourney.util.RedisCacheUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private FollowService followService;

    @Autowired
    private TypeService typeService;

    @Autowired
    private UserSubscribeService userSubscribeService;

    @Autowired
    private InterestPushService interestPushService;

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
        final Map<Long,User> userMap = this.getBaseUserInfoToMap(map.keySet());
        for(Long followId : followsId){
            User user = userMap.get(followId);
            user.setEach(map.get(user.getId()));
            users.add(user);
        }
        page.setRecords(users);
        page.setTotal(users.size());

        return page;
    }

    @Override
    public Page<User> getFans(Long userId, BasePage basePage) {
        final Page<User> page = new Page<>();
        //查找fansId
        Collection<Long> fans = followService.getFans(userId, basePage);
        if(ObjectUtils.isEmpty(fans)){
            return page;
        }

        //查找FollowsId
        HashSet<Long> follows = new HashSet<>();
        follows.addAll(followService.getFollows(userId,null));

        Map<Long,Boolean> map = new HashMap<>();
        //确定互关状态
        for(Long fan : fans){
            map.put(fan,follows.contains(fan));
        }

        List<User> users = new ArrayList<>();
        Map<Long, User> userMap = this.getBaseUserInfoToMap(map.keySet());
        for(Long fan : fans){
            User user = userMap.get(fan);
            user.setEach(map.get(user.getId()));
            users.add(user);
        }

        //设置page
        page.setRecords(users);
        page.setTotal(users.size());

        return page;
    }

    @Override
    public void subscribe(Set<Long> typeIds) {
        if(ObjectUtils.isEmpty(typeIds)){
          return;
        }
        List<Type> types = typeService.listByIds(typeIds);
        if(types.size() != typeIds.size()){
            throw new BaseException("分类不存在");
        }

        Long userId = UserHolder.get();
        ArrayList<UserSubscribe> userSubscribes = new ArrayList<>();
        for(Long typeId : typeIds){
            UserSubscribe userSubscribe = new UserSubscribe();
            userSubscribe.setTypeId(typeId);
            userSubscribe.setUserId(userId);
            userSubscribes.add(userSubscribe);
        }

        userSubscribeService.remove(new LambdaQueryWrapper<UserSubscribe>().eq(UserSubscribe::getUserId,userId));
        userSubscribeService.saveBatch(userSubscribes);

        final ModelVO modelVO = new ModelVO();

        modelVO.setUserId(userId);
        List<String> labels = new ArrayList<>();
        for(Type type : types){
            labels.addAll(type.buildLabel());
        }

        modelVO.setLabels(labels);
        initModel(modelVO);
    }

    @Override
    public List<Type> listSubscribeType(Long userId) {
        final List<Long> typeIds = userSubscribeService.list(new LambdaQueryWrapper<UserSubscribe>()
                .eq(UserSubscribe::getUserId, userId))
                .stream().map(UserSubscribe::getTypeId)
                .collect(Collectors.toList());

        final List<Type> types = typeService.list(new LambdaQueryWrapper<Type>()
                .in(Type::getId, typeIds).select(Type::getId, Type::getName, Type::getIcon));
        return types;
    }

    @Override
    public List<Type> listNoSubscribe(Long userId) {
        List<Type> list = typeService.list(null);
        List<Type> types = listSubscribeType(userId);

        List<Type> res = new ArrayList<>();
        for(Type type : list){
            if(!types.contains(type)){
                res.add(type);
            }
        }
        return res;
    }

    private Map<Long,User> getBaseUserInfoToMap(Collection<Long> ids){
        List<User> users = new ArrayList<>();
        users  = list(new LambdaQueryWrapper<User>().in(User::getId,ids)
                .select(User::getId,User::getNickName,User::getSex,User::getAvatar,User::getDescription)
        );
        return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    public void initModel(ModelVO modelVO){
        interestPushService.initUserModel(modelVO.getUserId(),modelVO.getLabels());
    }
}
