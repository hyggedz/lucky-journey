package org.xyz.luckyjourney.entity.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.xyz.luckyjourney.entity.BaseEntity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public class User extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @Email(message = "邮箱格式不正确，请重新输入")
    private String email;

    private String nickName;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String description;

    //true为男，false为女
    private Boolean sex;

    private Long avatar;

    private Long defaultFavoritesId;

    @TableField(exist = false)
    private Boolean each;

    @TableField(exist = false)
    private Set<String> roleName;
}
