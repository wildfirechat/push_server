package cn.wildfirechat.push.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(-100)
public class PortAccessFilter extends OncePerRequestFilter {

    @Value("${server.port:8085}")
    private int pushPort;

    @Value("${admin.server.port:8086}")
    private int adminPort;

    private static final Set<String> PUSH_PATHS = new HashSet<>();
    static {
        PUSH_PATHS.add("/android/push");
        PUSH_PATHS.add("/ios/push");
        PUSH_PATHS.add("/harmony/push");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        int port = request.getServerPort();
        String uri = request.getRequestURI();

        if (port == pushPort) {
            // 推送端口：只允许推送接口
            if (!PUSH_PATHS.contains(uri)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Not Found");
                return;
            }
        } else if (port == adminPort) {
            // Admin 端口：只允许 admin 接口和静态资源
            if (!uri.startsWith("/api/admin") && !uri.startsWith("/admin")) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Not Found");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
