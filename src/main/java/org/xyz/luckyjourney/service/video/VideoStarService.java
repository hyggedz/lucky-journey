package org.xyz.luckyjourney.service.video;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xyz.luckyjourney.entity.video.Video;
import org.xyz.luckyjourney.entity.video.VideoStar;

public interface VideoStarService extends IService<VideoStar> {
    boolean starVideo(VideoStar videoStar);
}
