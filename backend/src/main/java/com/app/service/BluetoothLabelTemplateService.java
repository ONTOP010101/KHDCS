package com.app.service;

import com.app.entity.BluetoothLabelTemplate;
import com.app.mapper.BluetoothLabelTemplateMapper;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BluetoothLabelTemplateService {

    @Autowired
    private BluetoothLabelTemplateMapper mapper;

    public List<BluetoothLabelTemplate> list() {
        LambdaQueryWrapper<BluetoothLabelTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BluetoothLabelTemplate::getUpdateTime);
        return mapper.selectList(wrapper);
    }

    public BluetoothLabelTemplate getById(Long id) {
        return mapper.selectById(id);
    }

    public BluetoothLabelTemplate create(BluetoothLabelTemplate template) {
        LocalDateTime now = LocalDateTime.now();
        Long userId = UserContext.getUserId();
        template.setCreateBy(userId);
        template.setUpdateBy(userId);
        template.setCreateTime(now);
        template.setUpdateTime(now);
        mapper.insert(template);
        return template;
    }

    public void update(Long id, BluetoothLabelTemplate template) {
        template.setId(id);
        template.setUpdateBy(UserContext.getUserId());
        template.setUpdateTime(LocalDateTime.now());
        mapper.updateById(template);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
