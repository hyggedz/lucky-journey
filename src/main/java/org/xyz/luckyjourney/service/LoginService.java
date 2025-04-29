package org.xyz.luckyjourney.service;

import org.xyz.luckyjourney.entity.Captcha;
import org.xyz.luckyjourney.entity.user.User;
import org.xyz.luckyjourney.entity.vo.FindPWVO;
import org.xyz.luckyjourney.entity.vo.RegisterVO;

import javax.servlet.http.HttpServletResponse;

public interface LoginService {

    User login(User user) throws Exception;

    Boolean register(RegisterVO registerVO) throws Exception;

    void captcha(String uuid, HttpServletResponse response) throws  Exception;

    Boolean getCode(Captcha captcha) throws Exception;

    void checkCode(String email,String code) throws  Exception;

    Boolean findPassword(FindPWVO findPWVO);
}
