package com.nsu.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component // 将AliOssProperties对象交给Spring容器管理
@ConfigurationProperties(prefix = "sky.alioss") // 读取配置文件中以sky.alioss开头的配置
@Data
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
}
