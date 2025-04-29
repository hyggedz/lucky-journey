package org.xyz.luckyjourney.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xyz.luckyjourney.entity.user.Follow;


@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
}
