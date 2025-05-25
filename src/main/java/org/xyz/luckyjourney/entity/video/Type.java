package org.xyz.luckyjourney.entity.video;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.xyz.luckyjourney.entity.BaseEntity;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Type extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "分类名称不可为空")
    private String name;

    private String description;

    private String icon;

    private Boolean open;

    private String labelNames;

    private Integer sort;

    @TableField(exist = false)
    private Boolean used;

    public List<String> buildLabel() {
        return Arrays.asList(labelNames.split(","));
    }
}