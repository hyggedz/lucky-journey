package org.xyz.luckyjourney.entity.video;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.xyz.luckyjourney.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = false)
public class VideoStar extends BaseEntity {
    private static final Long serialVersionUID = 1L;

    private Long videoId;

    private Long userId;
}
