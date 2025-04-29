package org.xyz.luckyjourney.controller;

import org.hibernate.validator.internal.constraintvalidators.hv.CodePointLengthValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xyz.luckyjourney.entity.Captcha;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.vo.FindPWVO;
import org.xyz.luckyjourney.entity.vo.RegisterVO;
import org.xyz.luckyjourney.service.LoginService;
import org.xyz.luckyjourney.util.JwtUtil;
import org.xyz.luckyjourney.util.R;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@RestController
@RequestMapping("/luckyjourney/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    public R login(@RequestBody @Validated User user) throws Exception{
        loginService.login(user);

        String jwtToken = JwtUtil.getJwtToken(user.getId(), user.getNickName());

        HashMap<Object, Object> map = new HashMap<>();
        map.put("token",jwtToken);
        map.put("user",user);
        map.put("name",user.getNickName());

        return R.ok().data(map);
    }


    /**
     * 获取图形验证码
     *
     * @param response
     * @param uuid
     * @throws Exception
     */
    @GetMapping("captcha.jpg/{uuid}")
    public void captcha (HttpServletResponse response,@PathVariable  String uuid) throws Exception {
        loginService.captcha(uuid, response);
    }

    /**
     * 获取六位验证码
     *
     * @param captcha
     * @return
     * @throws Exception
     */
    @PostMapping("/getCode")
    public R getCode(@RequestBody @Validated Captcha captcha) throws Exception{
        if(!loginService.getCode(captcha)){
            return R.error().message("验证码错误");
        }
        return R.ok().message("验证码已发出，请耐心等待");
    }

    /**
     * 校验验证码
     *
     * @param email
     * @param code
     * @return
     * @throws Exception
     */
    @PostMapping("/check")
    public R checkCode(String email,String code) throws Exception {
        loginService.checkCode(email,code);
        return R.ok().message("验证成功");
    }


    /**
     * 注册
     *
     * @param registerVO
     * @return
     * @throws Exception
     */
    @PostMapping("/register")
    public R register(@RequestBody @Validated RegisterVO registerVO) throws Exception{
         if(!loginService.register(registerVO)){
             return R.error().message("注册失败");
         }
         return R.ok().message("注册成功");
    }

    @PostMapping("/findPW")
    public R findPassword(@RequestBody @Validated FindPWVO findPWVO){
        if(loginService.findPassword(findPWVO)){
            return R.ok().message("修改成功");
        }
        return R.error().message("修改失败，请重试");
    }
}
