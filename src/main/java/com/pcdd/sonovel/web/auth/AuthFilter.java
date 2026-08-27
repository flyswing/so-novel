package com.pcdd.sonovel.web.auth;

import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * 拦截未登录请求：页面跳转登录页，接口返回 401。
 */
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_EXACT = Set.of(
            "/login.html",
            "/login",
            "/favicon.ico",
            "/auth/status"
    );

    private static final Set<String> PUBLIC_PREFIX = Set.of(
            "/js/"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (!WebAuth.isEnabled() || isPublic(req) || WebAuth.isAuthenticated(req)) {
            chain.doFilter(request, response);
            return;
        }

        String path = req.getRequestURI();
        if (isApiPath(path) || wantsJson(req)) {
            RespUtils.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "未登录或登录已过期");
            return;
        }

        resp.sendRedirect("/login.html");
    }

    private boolean isPublic(HttpServletRequest req) {
        String path = req.getRequestURI();
        if (PUBLIC_EXACT.contains(path)) {
            return true;
        }
        for (String prefix : PUBLIC_PREFIX) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isApiPath(String path) {
        return path.startsWith("/book-")
                || path.startsWith("/search/")
                || path.startsWith("/sources")
                || path.startsWith("/local-books")
                || path.startsWith("/config")
                || path.startsWith("/suggestion")
                || path.startsWith("/download-progress")
                || path.startsWith("/logout")
                || path.startsWith("/auth/");
    }

    private boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

}
