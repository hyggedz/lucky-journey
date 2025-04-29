package org.xyz.luckyjourney.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xyz.luckyjourney.entity.Captcha;

import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;

public interface CaptchaService extends IService<Captcha>{
    BufferedImage getCaptcha(String uuid) throws Exception;

    Boolean validate(Captcha captcha) throws  Exception;
}
