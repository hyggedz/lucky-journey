package org.xyz.luckyjourney.entity.vo;

import lombok.Data;

import java.awt.image.LookupOp;

@Data
public class UserVO {
    private Long id;

    private String nickName;

    private Boolean sex;

    private Long avatar;

    private String description;

    private Long follow;

    private Long fans;
}


