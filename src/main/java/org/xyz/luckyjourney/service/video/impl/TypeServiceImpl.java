package org.xyz.luckyjourney.service.video.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.video.Type;
import org.xyz.luckyjourney.mapper.video.TypeMapper;
import org.xyz.luckyjourney.service.video.TypeService;

@Service
public class TypeServiceImpl extends ServiceImpl<TypeMapper, Type> implements TypeService {
}
