package org.xyz.luckyjourney.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.xyz.luckyjourney.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = false)
public class FavoritesVideo extends BaseEntity {
    final private static long serialVersionUID = 1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;

    private Long favoritesId;

    private Long videoId;

    private Long userId;
}
