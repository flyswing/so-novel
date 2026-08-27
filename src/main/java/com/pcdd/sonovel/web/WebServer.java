package com.pcdd.sonovel.web;

import cn.hutool.core.lang.Console;
import com.pcdd.sonovel.core.AppConfigLoader;
import com.pcdd.sonovel.web.auth.AuthFilter;
import com.pcdd.sonovel.web.auth.WebAuth;
import com.pcdd.sonovel.web.servlet.*;
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.ee11.servlet.DefaultServlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Jetty;
import org.eclipse.jetty.util.resource.ResourceFactory;

import java.util.EnumSet;

import static org.fusesource.jansi.AnsiRenderer.render;

public class WebServer {

    public void start() {
        int port = AppConfigLoader.APP_CONFIG.getWebPort();
        Server server = new Server(port);
        ServletContextHandler context = createServletContext();
        registerServlets(context);
        server.setHandler(context);
        try {
            server.start();
            Console.log("SoNovel {}", "v" + AppConfigLoader.APP_CONFIG.getVersion());
            Console.log(render("✔ Web server started (Jetty {})", "green"), Jetty.VERSION);
            Console.log(render("➜ Local: http://localhost:{}/", "blue"), port);
            if (WebAuth.isEnabled()) {
                Console.log(render("➜ Login: http://localhost:{}/login.html  (user: {})", "blue"),
                        port, WebAuth.username());
            }
            server.join();
        } catch (Exception e) {
            Console.error(e, render("✖ Startup failed.", "red"));
        }
    }

    private ServletContextHandler createServletContext() {
        ServletContextHandler context = new ServletContextHandler("/", ServletContextHandler.SESSIONS);
        context.setBaseResource(ResourceFactory.of(context)
                .newResource(WebServer.class.getClassLoader().getResource("static")));
        return context;
    }

    private void registerServlets(ServletContextHandler context) {
        context.addFilter(AuthFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        context.addServlet(LoginServlet.class, "/login");
        context.addServlet(LoginServlet.class, "/auth/status");
        context.addServlet(LogoutServlet.class, "/logout");

        context.addServlet(BookFetchServlet.class, "/book-fetch");
        context.addServlet(BookDownloadServlet.class, "/book-download");
        context.addServlet(LocalBookListServlet.class, "/local-books");
        context.addServlet(AggregatedSearchServlet.class, "/search/aggregated");
        context.addServlet(DownloadProgressSseServlet.class, "/download-progress");
        context.addServlet(ConfigServlet.class, "/config");
        context.addServlet(BookDeleteServlet.class, "/book-delete");
        context.addServlet(SourceListServlet.class, "/sources");
        context.addServlet(SourceListServlet.class, "/sources/check");
        context.addServlet(SuggestionServlet.class, "/suggestion");

        ServletHolder staticHolder = new ServletHolder("default", DefaultServlet.class);
        staticHolder.setInitParameter("dirAllowed", "false");
        context.addServlet(staticHolder, "/");
    }

}
