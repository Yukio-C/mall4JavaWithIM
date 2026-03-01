package com.mall.filter;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * XSS 过滤请求包装器
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return clean(value);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return clean(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                values[i] = clean(values[i]);
            }
        }
        return values;
    }

    private String clean(String content) {
        if (content == null) return null;
        // 使用最严格的白名单，剔除所有 HTML 标签
        return Jsoup.clean(content, Safelist.none());
    }
}
