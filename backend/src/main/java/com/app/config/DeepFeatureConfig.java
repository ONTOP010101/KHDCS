package com.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DeepFeatureConfig implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(DeepFeatureConfig.class);

    @Value("${search.image.siamese-model-path:}")
    private String siameseModelPath;

    @Override
    public void afterPropertiesSet() {
        if (siameseModelPath != null && !siameseModelPath.isEmpty()) {
            log.info("Configured Siamese model path: {}", siameseModelPath.trim());
        } else {
            log.info("No Siamese model path configured, MobileNetV2 will be auto-detected");
        }
    }
}
