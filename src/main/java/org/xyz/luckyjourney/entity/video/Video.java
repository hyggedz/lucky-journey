package org.xyz.luckyjourney.entity.video;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.util.ObjectUtils;
import org.xyz.luckyjourney.entity.BaseEntity;
import org.xyz.luckyjourney.entity.vo.UserVO;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
public class Video extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String yv;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    private Long url;

    private Long userId;

    private Long typeId;

    private Boolean open;

    private Long cover;

    private Integer auditStatus;

    private String msg;

    private Boolean auditQueueStatus;

    private Long startCount;

    private Long shareCount;

    private Long historyCount;

    private Long favoritesCount;

    private String duration;

    private String videoType;

    private String labelNames;

    @TableField(exist = false)
    private UserVO user;

    @TableField(exist = false)
    private String typeName;

    @TableField(exist = false)
    private Boolean start;

    @TableField(exist = false)
    private Boolean favorites;

    @TableField(exist = false)
    private Boolean follow;

    @TableField(exist = false)
    private String auditStatusName;

    @TableField(exist = false)
    private String openName;

    public List<String> buildLabel(){
        if(ObjectUtils.isEmpty(this.labelNames)) return Collections.EMPTY_LIST;
        return Arrays.asList(this.labelNames.split(","));
    }
}
