package org.xyz.luckyjourney.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.xyz.luckyjourney.entity.BaseEntity;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = false)
public class Favorites extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "得给你的收藏夹起个名字吧？")
    private String name;

    private String description;

    private Long userId;

    //收藏夹视频总数
    @TableField(exist = false)
    private Long videoCount;
}
