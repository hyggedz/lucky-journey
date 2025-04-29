package org.xyz.luckyjourney.entity.vo;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class FindPWVO {

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "密码不能为空")
    private String newPassword;
}
