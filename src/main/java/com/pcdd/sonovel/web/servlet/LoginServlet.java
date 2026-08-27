package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.web.auth.WebAuth;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 登录 / 鉴权状态接口。
 */
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // GET /auth/status
        RespUtils.writeJson(resp, Map.of(
                "enabled", WebAuth.isEnabled(),
                "authenticated", WebAuth.isAuthenticated(req),
                "username", WebAuth.isAuthenticated(req) ? WebAuth.username() : ""
        ));
    }

    @Override
    @SneakyThrows
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // POST /login  body: {"username":"...","password":"..."} 或 form
        String username;
        String password;

        String contentType = StrUtil.blankToDefault(req.getContentType(), "");
        if (contentType.contains("application/json")) {
            String body = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject json = JSONUtil.parseObj(body);
            username = json.getStr("username", "");
            password = json.getStr("password", "");
        } else {
            username = StrUtil.blankToDefault(req.getParameter("username"), "");
            password = StrUtil.blankToDefault(req.getParameter("password"), "");
        }

        if (!WebAuth.isEnabled()) {
            RespUtils.writeJson(resp, Map.of("enabled", false, "authenticated", true));
            return;
        }

        if (!WebAuth.matches(username, password)) {
            RespUtils.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "用户名或密码错误");
            return;
        }

        WebAuth.markAuthenticated(req);
        RespUtils.writeJson(resp, Map.of(
                "enabled", true,
                "authenticated", true,
                "username", WebAuth.username()
        ));
    }

}
