package com.mall.config;

import com.mall.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {

    @Value("${Yukio.alioss.endpoint}")
    private String endpoint;

    @Value("${Yukio.alioss.access-key-id}")
    private String accessKeyId;

    @Value("${Yukio.alioss.access-key-secret}")
    private String accessKeySecret;

    @Value("${Yukio.alioss.bucket-name}")
    private String bucketName;

    @Bean
    public AliOssUtil aliOssUtil() {
        log.info("Creating AliOssUtil bean with endpoint: {}, bucket: {}", endpoint, bucketName);
        return new AliOssUtil(endpoint, accessKeyId, accessKeySecret, bucketName);
    }
}
