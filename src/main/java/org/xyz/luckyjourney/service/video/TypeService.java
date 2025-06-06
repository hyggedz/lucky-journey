package org.xyz.luckyjourney.service.video;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.video.Type;

import java.util.List;


public interface TypeService extends IService<Type> {
    List<String> random10Labels();
}
