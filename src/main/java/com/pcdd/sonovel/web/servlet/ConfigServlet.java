package com.pcdd.sonovel.web.servlet;

import cn.hutool.core.bean.BeanUtil;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.web.auth.WebAuth;
import com.pcdd.sonovel.web.util.RespUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public class ConfigServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        AppConfig cfg = AppConfigLoader.APP_CONFIG;
        Map<String, Object> safe = BeanUtil.beanToMap(cfg);
        safe.remove("webPassword");
        safe.put("authEnabled", WebAuth.isEnabled());
        RespUtils.writeJson(resp, safe);
    }

}
