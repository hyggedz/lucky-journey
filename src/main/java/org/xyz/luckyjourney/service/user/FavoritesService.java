package org.xyz.luckyjourney.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xyz.luckyjourney.entity.user.Favorites;

public interface FavoritesService extends IService<Favorites> {
    void exist(Long userId,Long defaultFavoritesId);
}
