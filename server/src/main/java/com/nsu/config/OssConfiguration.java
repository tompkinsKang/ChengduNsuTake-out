package com.nsu.config;
/*
  Date:2025/3/13
  Time:13:12
  @author llh 
 */

import com.nsu.properties.AliOssProperties;
import com.nsu.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {
    @Bean // 将AliOssUtil对象交给Spring容器管理
    @ConditionalOnMissingBean // 当Spring容器中没有AliOssUtil对象时，才创建AliOssUtil对象，只有一个AliOssUtil对象
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        log.info("开始创建阿里文件上传工具对象：{}",aliOssProperties);
        return new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}

