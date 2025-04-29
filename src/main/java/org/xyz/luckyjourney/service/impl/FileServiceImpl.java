package org.xyz.luckyjourney.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.entity.File;
import org.xyz.luckyjourney.mapper.FileMapper;
import org.xyz.luckyjourney.service.FileService;


@Service
public class FileServiceImpl extends ServiceImpl<FileMapper,File> implements FileService {
}
