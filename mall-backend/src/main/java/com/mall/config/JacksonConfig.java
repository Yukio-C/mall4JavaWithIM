package com.mall.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;

/**
 * Jackson 安全配置：全局自动清洗 JSON 字符串
 */
@Configuration
public class JacksonConfig {

    // 定义放行的富文本字段名
    private static final java.util.List<String> RICH_TEXT_FIELDS = java.util.Arrays.asList("detailHtml", "serviceInfo");

    @Bean
    public ObjectMapper xssObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule xssModule = new SimpleModule();

        // 注册自定义的字符串反序列化器
        xssModule.addDeserializer(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getValueAsString();
                if (value == null) return null;

                String fieldName = p.getCurrentName();

                // 1. 如果是富文本字段，使用“宽松白名单”清洗 (允许排版，禁止脚本)
                if (RICH_TEXT_FIELDS.contains(fieldName)) {
                    return Jsoup.clean(value, Safelist.relaxed().addAttributes(":all", "style", "class"));
                }

                // 2. 对于普通字符串字段，使用“无标签白名单” (彻底剥离所有 HTML 标签)
                return Jsoup.clean(value, Safelist.none());
            }
        });

        objectMapper.registerModule(xssModule);
        return objectMapper;
    }
}
