package com.app.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.app.util.ImageShardContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        DynamicTableNameInnerInterceptor tableNameInterceptor = new DynamicTableNameInnerInterceptor();
        tableNameInterceptor.setTableNameHandler((sql, tableName) -> {
            if ("images".equals(tableName)) {
                String prefix = ImageShardContext.getHashPrefix();
                if (prefix != null && prefix.length() == 2) {
                    return tableName + "_" + prefix;
                }
            }
            return tableName;
        });
        interceptor.addInnerInterceptor(tableNameInterceptor);

        return interceptor;
    }
}
