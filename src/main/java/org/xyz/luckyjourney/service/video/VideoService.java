package org.xyz.luckyjourney.service.video;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.video.Video;
import org.xyz.luckyjourney.entity.vo.BasePage;

import java.util.Collection;


public interface VideoService extends IService<Video> {
    IPage<Video> listByUserIdOpenVideo(Long userId, BasePage basePage);

    boolean favoritesVideo(Long fid, Long vid);

    Collection<Video> listByFavoritesId(Long favoritesId);

    boolean starVideo(Long id);

    Collection<Video> pushVideos(Long userId);

    Collection<Video> getVideoByTypeId(Long typeId);

    Collection<Video> listSimiliarVideo(Video video);
}
