package com.app.service;

import com.app.common.BusinessException;
import com.app.common.PageResult;
import com.app.entity.Gallery;
import com.app.mapper.GalleryMapper;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GalleryService {

    @Autowired
    private GalleryMapper galleryMapper;

    public PageResult<Gallery> list(long current, long size, String keyword, String category, String tags) {
        LambdaQueryWrapper<Gallery> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Gallery::getTitle, keyword)
                    .or()
                    .like(Gallery::getDescription, keyword));
        }
        if (StringUtils.hasText(category) && !"all".equals(category)) {
            wrapper.eq(Gallery::getCategory, category);
        }
        if (StringUtils.hasText(tags)) {
            wrapper.like(Gallery::getTags, tags);
        }
        wrapper.orderByDesc(Gallery::getCreateTime);

        IPage<Gallery> page = galleryMapper.selectPage(new Page<>(current, size), wrapper);
        return new PageResult<>(page.getRecords(), page.getTotal(), current, size);
    }

    public Gallery getById(Long id) {
        Gallery gallery = galleryMapper.selectById(id);
        if (gallery == null) {
            throw new BusinessException(404, "图库不存在");
        }
        gallery.setViewCount(gallery.getViewCount() + 1);
        galleryMapper.updateById(gallery);
        return gallery;
    }

    @Transactional
    public Gallery create(Gallery gallery) {
        Long userId = UserContext.getUserId();
        gallery.setCreateBy(userId);
        gallery.setUpdateBy(userId);
        gallery.setImageCount(0);
        gallery.setViewCount(0);
        galleryMapper.insert(gallery);
        return gallery;
    }

    @Transactional
    public void update(Long id, Gallery gallery) {
        Gallery existing = galleryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "图库不存在");
        }
        gallery.setId(id);
        gallery.setUpdateBy(UserContext.getUserId());
        galleryMapper.updateById(gallery);
    }

    @Transactional
    public void delete(Long id) {
        Gallery gallery = galleryMapper.selectById(id);
        if (gallery == null) {
            throw new BusinessException(404, "图库不存在");
        }
        galleryMapper.deleteById(id);
    }

    @Transactional
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) {
            galleryMapper.deleteById(id);
        }
    }
}
