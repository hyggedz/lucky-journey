package org.xyz.luckyjourney.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xyz.luckyjourney.entity.user.Favorites;

import java.util.Collection;
import java.util.List;

public interface FavoritesService extends IService<Favorites> {
    void exist(Long userId,Long defaultFavoritesId);

    void remove(Long id, Long userId);

    boolean favoritesVideo(Long fid,Long vid);

    Collection<Long>  listByFavoritesId(Long favoritesId,Long userId);
}
