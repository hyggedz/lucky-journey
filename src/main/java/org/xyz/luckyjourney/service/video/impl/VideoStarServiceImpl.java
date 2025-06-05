package org.xyz.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.video.VideoStar;
import org.xyz.luckyjourney.mapper.video.VideoStarMapper;
import org.xyz.luckyjourney.service.video.VideoService;
import org.xyz.luckyjourney.service.video.VideoStarService;

@Service
public class VideoStarServiceImpl extends ServiceImpl<VideoStarMapper, VideoStar> implements VideoStarService {

    @Override
    public boolean starVideo(VideoStar videoStar) {
        try {
            this.save(videoStar);
        }catch (Exception e){
            this.remove(new LambdaQueryWrapper<VideoStar>()
                    .eq(VideoStar::getVideoId,videoStar.getVideoId())
                    .eq(VideoStar::getUserId,videoStar.getUserId()));
            return false;
        }
        return true;
    }
}
