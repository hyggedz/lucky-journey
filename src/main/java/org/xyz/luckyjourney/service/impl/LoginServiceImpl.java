package org.xyz.luckyjourney.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.constant.RedisConstant;
import org.xyz.luckyjourney.entity.Captcha;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.vo.FindPWVO;
import org.xyz.luckyjourney.entity.vo.RegisterVO;
import org.xyz.luckyjourney.exception.BaseException;
import org.xyz.luckyjourney.service.CaptchaService;
import org.xyz.luckyjourney.service.LoginService;
import org.xyz.luckyjourney.service.user.UserService;
import org.xyz.luckyjourney.util.RedisCacheUtil;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;


@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public User login(User user) throws Exception {
        String password = user.getPassword();
        //查找用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        User check = userService.getOne(userWrapper.eq(User::getEmail, user.getEmail()));

        if(ObjectUtils.isEmpty(check)){
            throw  new BaseException("用户不存在");
        }

        //校验密码
        if(!password.equals(user.getPassword())){
            throw  new BaseException("密码不一致");
        }
        return check;
    }

    @Override
    public void captcha(String uuid, HttpServletResponse response) throws Exception {
        if(ObjectUtils.isEmpty(uuid)){
            throw new IllegalArgumentException("uuid为空");
        }
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpg");

        BufferedImage captcha = captchaService.getCaptcha(uuid);
        ServletOutputStream outputStream = response.getOutputStream();
        ImageIO.write(captcha,"image",outputStream);
        IOUtils.closeQuietly(outputStream);
    }

    @Override
    public Boolean getCode(Captcha captcha) throws Exception {
        return captchaService.validate(captcha);
    }

    @Override
    public void checkCode(String email, String code) throws Exception {
        if(ObjectUtils.isEmpty(email) || ObjectUtils.isEmpty(code)){
            throw new BaseException("邮箱或验证码为空");
        }

        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if(user == null){
            throw new BaseException("邮箱不存在");
        }

        String key = RedisConstant.EMAIL_CODE + email;
        Object value = redisCacheUtil.get(key);
        if(value == null){
            throw new BaseException("验证码已过期");
        }

        String StoredCode = String.valueOf(value);

        if(!StoredCode.equals(code)){
            throw new BaseException("验证码输入错误");
        }

        redisCacheUtil.del(key);
    }

    @Override
    public Boolean register(RegisterVO registerVO) throws Exception {
        if(userService.register(registerVO)){
            captchaService.removeById(registerVO.getUuid());
            return true;
        }
        return false;
    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {
        return userService.findPassword(findPWVO);
    }
}
