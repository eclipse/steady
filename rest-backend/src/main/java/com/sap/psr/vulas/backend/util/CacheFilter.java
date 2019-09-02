package com.sap.psr.vulas.backend.util;

import org.springframework.stereotype.Component;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

// Component duplicated in rest-lib-utils/src/main/java/com/sap/psr/vulas/backend/util/CacheFilter.java

@Component
public class CacheFilter implements Filter {
    @Override
    public void destroy() {
        // Nothing
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String cache = request.getParameter("cache");
        if (cache != null && cache.equals("true")) {
            // Instructs Nginx to cache the response for 2 hours
            HttpServletResponse httpServletResponse=(HttpServletResponse)response;
            httpServletResponse.setHeader("X-Accel-Expires", "7200");
        }
        chain.doFilter(request, response);
    }
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing
    }
}
