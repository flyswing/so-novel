package com.pcdd.sonovel.web.servlet;

import com.pcdd.sonovel.web.auth.WebAuth;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.util.Map;

/**
 * 退出登录。
 */
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        logout(req, resp, false);
    }

    @Override
    @SneakyThrows
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        logout(req, resp, true);
    }

    @SneakyThrows
    private void logout(HttpServletRequest req, HttpServletResponse resp, boolean redirect) {
        WebAuth.clear(req);
        if (redirect) {
            resp.sendRedirect(WebAuth.isEnabled() ? "/login.html" : "/");
            return;
        }
        RespUtils.writeJson(resp, Map.of("ok", true));
    }

}
