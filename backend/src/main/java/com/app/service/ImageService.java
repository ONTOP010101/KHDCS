package com.app.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.app.common.BusinessException;
import com.app.entity.Gallery;
import com.app.entity.Image;
import com.app.mapper.GalleryMapper;
import com.app.mapper.ImageMapper;
import com.app.util.ImageShardContext;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImageService {

    @Value("${app.upload.image-path}")
    private String imagePath;

    @Value("${app.upload.thumbnail-path}")
    private String thumbnailPath;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private GalleryMapper galleryMapper;

    private static final String SHARD_HEX = "0123456789abcdef";

    @Transactional
    public Image upload(MultipartFile file, Long galleryId, Long sampleId, String description, String tags) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName);
        if (!isImage(ext)) {
            throw new BusinessException(400, "不支持的文件类型: " + ext);
        }

        try {
            byte[] fileBytes = file.getBytes();
            String hash = DigestUtil.sha256Hex(fileBytes);
            String hashPrefix = hash.substring(0, 2).toLowerCase();

            ImageShardContext.setHashPrefix(hashPrefix);
            try {
                LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Image::getHash, hash);
                Image existing = imageMapper.selectOne(wrapper);
                if (existing != null) {
                    return existing;
                }
            } finally {
                ImageShardContext.clear();
            }

            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String hashDir = hash.substring(0, 2) + "/" + hash.substring(2, 4);
            String storeName = hash + "." + ext;

            String relativePath = dateDir + "/" + hashDir + "/" + storeName;
            Path fullPath = Paths.get(imagePath, relativePath);
            FileUtil.mkParentDirs(fullPath.toFile());
            Files.write(fullPath, fileBytes);

            int width = 0, height = 0;
            try {
                BufferedImage bi = ImageIO.read(new ByteArrayInputStream(fileBytes));
                if (bi != null) { width = bi.getWidth(); height = bi.getHeight(); }
            } catch (Exception ignored) {}

            String thumbnailRelPath = null;
            try {
                thumbnailRelPath = generateThumbnail(fileBytes, hash, ext, width, height);
            } catch (Exception ignored) {}

            Image image = new Image();
            image.setGalleryId(galleryId);
            image.setSampleId(sampleId);
            image.setFileName(originalName);
            image.setFilePath(relativePath);
            image.setThumbnailPath(thumbnailRelPath);
            image.setFileSize(file.getSize());
            image.setFileType(ext);
            image.setWidth(width);
            image.setHeight(height);
            image.setHash(hash);
            image.setDescription(description);
            image.setTags(tags);
            image.setCreateBy(UserContext.getUserId());

            ImageShardContext.setHashPrefix(hashPrefix);
            try {
                imageMapper.insert(image);
            } finally {
                ImageShardContext.clear();
            }

            if (galleryId != null) {
                Gallery gallery = galleryMapper.selectById(galleryId);
                if (gallery != null) {
                    gallery.setImageCount(gallery.getImageCount() + 1);
                    galleryMapper.updateById(gallery);
                }
            }

            return image;
        } catch (BusinessException e) { throw e; }
        catch (Exception e) { throw new BusinessException(500, "上传失败: " + e.getMessage()); }
    }

    @Transactional
    public List<Image> uploadBatch(MultipartFile[] files, Long galleryId) {
        List<Image> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(upload(file, galleryId, null, null, null));
        }
        return results;
    }

    public byte[] loadImage(String hash) {
        String hashPrefix = hash.substring(0, 2).toLowerCase();
        ImageShardContext.setHashPrefix(hashPrefix);
        try {
            LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Image::getHash, hash);
            Image image = imageMapper.selectOne(wrapper);
            if (image == null) throw new BusinessException(404, "图片不存在");

            Path fullPath = Paths.get(imagePath, image.getFilePath());
            return Files.readAllBytes(fullPath);
        } catch (BusinessException e) { throw e; }
        catch (IOException e) { throw new BusinessException(500, "读取图片失败"); }
        finally { ImageShardContext.clear(); }
    }

    public byte[] loadImageById(Long id) {
        Image image = findImageById(id);
        if (image == null) throw new BusinessException(404, "图片不存在");

        try {
            Path fullPath = Paths.get(imagePath, image.getFilePath());
            return Files.readAllBytes(fullPath);
        } catch (IOException e) {
            throw new BusinessException(500, "读取图片失败");
        }
    }

    public Image loadImageInfo(Long id) {
        Image image = findImageById(id);
        if (image == null) throw new BusinessException(404, "图片不存在");
        return image;
    }

    public byte[] loadThumbnail(Long id) {
        Image image = findImageById(id);
        if (image == null) return null;

        if (image.getThumbnailPath() != null) {
            try {
                Path fullPath = Paths.get(thumbnailPath, image.getThumbnailPath());
                return Files.readAllBytes(fullPath);
            } catch (IOException ignored) {}
        }
        return loadImageById(id);
    }

    public List<Image> listByGallery(Long galleryId) {
        List<Image> allImages = new ArrayList<>();
        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Image::getGalleryId, galleryId)
                            .orderByAsc(Image::getSortOrder)
                            .orderByDesc(Image::getCreateTime);
                    allImages.addAll(imageMapper.selectList(wrapper));
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        return allImages;
    }

    @Transactional
    public void delete(Long id) {
        Image image = findImageById(id);
        if (image == null) return;

        if (image.getGalleryId() != null) {
            Gallery gallery = galleryMapper.selectById(image.getGalleryId());
            if (gallery != null && gallery.getImageCount() > 0) {
                gallery.setImageCount(gallery.getImageCount() - 1);
                galleryMapper.updateById(gallery);
            }
        }

        String hashPrefix = image.getHash() != null ? image.getHash().substring(0, 2).toLowerCase() : null;
        if (hashPrefix != null) {
            ImageShardContext.setHashPrefix(hashPrefix);
            try {
                imageMapper.deleteById(id);
            } finally {
                ImageShardContext.clear();
            }
        }
    }

    @Transactional
    public void deleteBatch(Long[] ids) {
        for (Long id : ids) { delete(id); }
    }

    private Image findImageById(Long id) {
        for (int i = 0; i < SHARD_HEX.length(); i++) {
            for (int j = 0; j < SHARD_HEX.length(); j++) {
                String prefix = "" + SHARD_HEX.charAt(i) + SHARD_HEX.charAt(j);
                ImageShardContext.setHashPrefix(prefix);
                try {
                    Image image = imageMapper.selectById(id);
                    if (image != null) return image;
                } finally {
                    ImageShardContext.clear();
                }
            }
        }
        return null;
    }

    private String generateThumbnail(byte[] imageBytes, String hash, String ext, int origWidth, int origHeight) throws IOException {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (source == null) return null;

        int thumbWidth = 300, thumbHeight = 300;
        if (origWidth > 0 && origHeight > 0) {
            double ratio = Math.min((double) thumbWidth / origWidth, (double) thumbHeight / origHeight);
            thumbWidth = (int) (origWidth * ratio);
            thumbHeight = (int) (origHeight * ratio);
        }

        BufferedImage thumb = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, thumbWidth, thumbHeight, null);
        g.dispose();

        String relPath = hash.substring(0, 2) + "/" + hash.substring(2, 4) + "/" + hash + "_thumb." + ext;
        Path fullPath = Paths.get(thumbnailPath, relPath);
        FileUtil.mkParentDirs(fullPath.toFile());

        String format = "png".equalsIgnoreCase(ext) ? "png" : "jpg";
        ImageIO.write(thumb, format, fullPath.toFile());

        return relPath;
    }

    private boolean isImage(String ext) {
        if (ext == null) return false;
        ext = ext.toLowerCase();
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext)
                || "gif".equals(ext) || "bmp".equals(ext) || "webp".equals(ext);
    }
}
