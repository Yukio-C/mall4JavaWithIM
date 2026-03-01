package com.mall.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 全局 XSS 过滤器
 */
@Component
@WebFilter(urlPatterns = "/*", filterName = "xssFilter")
@Slf4j
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getServletPath();

        // 排除名单：如果某些特定的表单字段必须接收 HTML (如商品详情编辑)
        // 注意：JSON 格式的 Body 过滤在下一道防线处理
        if (path.startsWith("/admin/product/update")) {
            chain.doFilter(request, response);
            return;
        }

        log.debug("XSS 过滤器拦截请求: {}", path);
        chain.doFilter(new XssHttpServletRequestWrapper(req), response);
    }
}
