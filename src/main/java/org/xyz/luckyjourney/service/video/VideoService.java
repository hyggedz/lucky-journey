package org.xyz.luckyjourney.service.video;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.video.Video;
import org.xyz.luckyjourney.entity.vo.BasePage;


public interface VideoService extends IService<Video> {
    IPage<Video> listByUserIdOpenVideo(Long userId, BasePage basePage);
}
