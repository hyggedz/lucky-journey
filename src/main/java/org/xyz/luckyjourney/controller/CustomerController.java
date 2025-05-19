package org.xyz.luckyjourney.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xyz.luckyjourney.entity.vo.BasePage;
import org.xyz.luckyjourney.entity.vo.UpdateUserVO;
import org.xyz.luckyjourney.entity.vo.UserVO;
import org.xyz.luckyjourney.holder.UserHolder;
import org.xyz.luckyjourney.service.user.UserService;
import org.xyz.luckyjourney.util.R;

import java.util.function.LongFunction;


@RestController
@RequestMapping("luckyjourney/customer")
public class CustomerController {

    @Autowired
    private UserService userService;

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

}
