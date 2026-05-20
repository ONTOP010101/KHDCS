package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.entity.Gallery;
import com.app.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gallery")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

    @GetMapping
    public Result<PageResult<Gallery>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tags) {
        return Result.success(galleryService.list(current, size, keyword, category, tags));
    }

    @GetMapping("/{id}")
    public Result<Gallery> getById(@PathVariable Long id) {
        return Result.success(galleryService.getById(id));
    }

    @PostMapping
    public Result<Gallery> create(@RequestBody Gallery gallery) {
        return Result.success("创建成功", galleryService.create(gallery));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Gallery gallery) {
        galleryService.update(id, gallery);
        return Result.ok("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        galleryService.delete(id);
        return Result.ok("删除成功");
    }

    @PostMapping("/batch-delete")
    public Result<Void> deleteBatch(@RequestBody Long[] ids) {
        galleryService.deleteBatch(ids);
        return Result.ok("批量删除成功");
    }
}
