package com.pcdd.sonovel.web.auth;

import cn.hutool.core.util.StrUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.AppConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.experimental.UtilityClass;

/**
 * WebUI 登录鉴权辅助。
 * <p>
 * 当 {@code [web] password} 非空时启用登录；用户名默认 {@code admin}。
 */
@UtilityClass
public class WebAuth {

    public static final String SESSION_KEY = "SONOVEL_AUTHENTICATED";

    public boolean isEnabled() {
        return StrUtil.isNotBlank(AppConfigLoader.APP_CONFIG.getWebPassword());
    }

    public String username() {
        AppConfig cfg = AppConfigLoader.APP_CONFIG;
        return StrUtil.blankToDefault(cfg.getWebUsername(), "admin");
    }

    public boolean matches(String username, String password) {
        if (!isEnabled()) {
            return true;
        }
        return username().equals(username)
                && AppConfigLoader.APP_CONFIG.getWebPassword().equals(password);
    }

    public boolean isAuthenticated(HttpServletRequest req) {
        if (!isEnabled()) {
            return true;
        }
        HttpSession session = req.getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute(SESSION_KEY));
    }

    public void markAuthenticated(HttpServletRequest req) {
        HttpSession session = req.getSession(true);
        session.setAttribute(SESSION_KEY, Boolean.TRUE);
        session.setMaxInactiveInterval(7 * 24 * 60 * 60);
    }

    public void clear(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

}
