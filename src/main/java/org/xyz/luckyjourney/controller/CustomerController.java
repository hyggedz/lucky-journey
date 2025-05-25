package org.xyz.luckyjourney.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xyz.luckyjourney.entity.user.Favorites;
import org.xyz.luckyjourney.entity.vo.BasePage;
import org.xyz.luckyjourney.entity.vo.UpdateUserVO;
import org.xyz.luckyjourney.entity.vo.UserVO;
import org.xyz.luckyjourney.holder.UserHolder;
import org.xyz.luckyjourney.service.user.FavoritesService;
import org.xyz.luckyjourney.service.user.UserService;
import org.xyz.luckyjourney.util.R;

import java.util.HashSet;
import java.util.List;
import java.util.function.LongFunction;


@RestController
@RequestMapping("luckyjourney/customer")
public class CustomerController {

    @Autowired
    private UserService userService;

    @Autowired
    private FavoritesService favoritesService;

    @GetMapping("/getInfo/{userId}")
    public R getInfo(@PathVariable  Long userId){
        UserVO userVO = userService.getInfo(userId);
        return R.ok().data(userVO);
    }

    @GetMapping("/getInfo")
    public R getDefaultInfo(){
        UserVO userVO = userService.getInfo(UserHolder.get());
        return R.ok().data(userVO);
    }

    @PostMapping
    public R updateUser(@RequestBody @Validated UpdateUserVO updateUserVO){
        Long userId = UserHolder.get();
        userService.updateUser(updateUserVO,userId);
        return R.ok().message("修改成功");
    }

    /**
     * 关注/取关
     *
     * @param followId
     * @return
     */
    @PostMapping("/follows/{followId}")
    public R follows(@PathVariable Long followId){
        return R.ok().message(userService.follows(followId) ? "已关注" : "已取关");
    }

    /**
     * 获取关注人员
     *
     * @param userId
     * @param basePage
     * @return
     */
    @GetMapping("/follows/{userId}")
    public R getFollows(@PathVariable Long userId, BasePage basePage){
        return R.ok().data(userService.getFollows(userId,basePage));
    }

    /**
     * 获取粉丝
     *
     *
     * @param userId
     * @param basePage
     * @return
     */
    @GetMapping("/fans/{userId}")
    public R getFans(@PathVariable Long userId,BasePage basePage){
      return R.ok().data(userService.getFans(userId,basePage));
    };

    /**
     * 获取所有收藏夹
     *
     * @return
     */
    @GetMapping("/favorites")
    public R listFavorites(){
        Long id = UserHolder.get();
        List<Favorites> favorites = favoritesService.list(new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId,id));
        return R.ok().data(favorites);
    }

    /**
     * 获取指定收藏夹
     *
     * @param id
     * @return
     */
    @GetMapping("/favorites/{id}")
    public R getFavorites(@PathVariable Long id){
        return R.ok().data(favoritesService.getById(id));
    }

    /**
     * 增加/修改收藏夹
     *
     * @author xyz
     * @param favorites
     * @return
     */
    @PostMapping("/favorites")
    public R saveOrUpdateFavorites(@RequestBody @Validated Favorites favorites){
        Long userId = UserHolder.get();
        Long id = favorites.getId();
        favorites.setUserId(userId);

        final Long count = favoritesService.count(new LambdaQueryWrapper<Favorites>()
                .eq(Favorites::getName,favorites.getName())
                .eq(Favorites::getUserId,favorites.getUserId())
                .ne(Favorites::getId,favorites.getId()));

        if(count == 1){
            return R.error().message("该收藏夹已经存在");
        }
        favoritesService.saveOrUpdate(favorites);
        return R.ok().message(id == null ? "已创建" : "已修改");
    }

    /**
     * 删除收藏夹
     *
     * @param id
     * @return
     */
    @DeleteMapping("/favorites/{id}")
    public R  deleteFavorites(@PathVariable Long id){
        favoritesService.remove(id,UserHolder.get());
        return R.ok().message("已删除");
    }

    /**
     *  订阅分类
     *
     * @param types
     * @return
     */
    @PostMapping("/subscribe")
    public R subscribe(@RequestParam(required = false) String types){
        HashSet<Long> hashSet = new HashSet<>();
        String msg = "订阅失败";

        if(!ObjectUtils.isEmpty(types)){
            String[] splits = types.split(",");
            for(String s : splits){
                long l = Long.parseLong(s);
                hashSet.add(l);
            }
            msg = "已订阅";
        }

        userService.subscribe(hashSet);
        return R.ok().message(msg);
    }

    @GetMapping("/subscribe")
    public R listSubscribeType(){
        return R.ok().data(userService.listSubscribeType(UserHolder.get()));
    }

    @GetMapping("/noSubscribe")
    public R listNoSubscribe(){
        return R.ok().data(userService.listNoSubscribe(UserHolder.get()));
    }
}
