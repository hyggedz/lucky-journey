package org.xyz.luckyjourney.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.constant.RedisConstant;
import org.xyz.luckyjourney.entity.Captcha;
import org.xyz.luckyjourney.exception.BaseException;
import org.xyz.luckyjourney.mapper.CaptchaMapper;
import org.xyz.luckyjourney.service.CaptchaService;
import org.xyz.luckyjourney.service.EmailService;
import org.xyz.luckyjourney.util.DateUtil;
import org.xyz.luckyjourney.util.RedisCacheUtil;


import javax.validation.constraints.Email;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Date;


@Service
public class CaptchaServiceImpl extends ServiceImpl<CaptchaMapper, Captcha> implements CaptchaService {

    @Autowired
    private Producer producer;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public BufferedImage getCaptcha(String uuid) throws Exception {
        //生成文本
        String text = this.producer.createText();

        Captcha captcha = new Captcha();
        captcha.setUuid(uuid);
        captcha.setCode(text);
        captcha.setExpireTime(DateUtil.addDateMinutes(new Date(),5));

        //保存进数据库
        this.save(captcha);

        //生成图片
        return this.producer.createImage(text);
    }

    /**
     * 验证图形验证码 + 发送六位的邮箱验证码
     *
     * @param captcha
     * @return
     * @throws Exception
     */
    @Override
    public Boolean validate(Captcha captcha) throws Exception {
        //获取邮箱和图形验证码
        String email = captcha.getEmail();
        String code = captcha.getCode();

        //校验
        Captcha captcha1 = this.getOne(new LambdaQueryWrapper<Captcha>().eq(Captcha::getUuid, captcha.getUuid()));
        if(captcha1 == null){
            throw new BaseException("uuid为空");
        }

        this.remove(new LambdaQueryWrapper<Captcha>().eq(Captcha::getUuid,captcha1.getUuid()));
        if(!captcha1.getCode().equals(code)){
            throw new BaseException("验证码错误");
        }

        if(captcha1.getExpireTime().getTime() <= System.currentTimeMillis()){
            throw  new BaseException("验证码过期");
        }

        String code1 = getSixCode();

        //验证码存入redis
        redisCacheUtil.set(RedisConstant.EMAIL_CODE + email,code1, RedisConstant.EMAIL_CODE_TIME);

        //调用邮件服务发送验证码
        emailService.send(email,"注册验证码: " + code + " 验证码五分钟之内有效");
        return true;
    }

    private String getSixCode(){
        SecureRandom secureRandom = new SecureRandom();
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
}
