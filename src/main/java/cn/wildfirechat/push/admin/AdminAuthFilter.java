package cn.wildfirechat.push.admin;

import cn.wildfirechat.push.admin.entity.AdminUser;
import cn.wildfirechat.push.admin.repository.AdminUserRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AdminAuthFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(AdminAuthFilter.class);

    @Autowired
    private AdminUserRepository adminUserRepository;

    private final AtomicReference<String> cachedSecretKey = new AtomicReference<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();

        if (!uri.startsWith("/admin/") && !uri.startsWith("/api/admin/")) {
            chain.doFilter(request, response);
            return;
        }

        if (uri.equals("/admin/") || uri.equals("/admin/index.html") || uri.startsWith("/admin/assets/")) {
            chain.doFilter(request, response);
            return;
        }

        if (uri.equals("/api/admin/login")) {
            chain.doFilter(request, response);
            return;
        }

        String token = req.getHeader("Authorization");
        if (!StringUtils.isEmpty(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            DecodedJWT jwt = verifyToken(token);
            if (jwt != null) {
                req.setAttribute("admin_username", jwt.getClaim("username").asString());
                chain.doFilter(request, response);
                return;
            }
        }

        if (!uri.startsWith("/api/admin/")) {
            chain.doFilter(request, response);
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}");
        }
    }

    private DecodedJWT verifyToken(String token) {
        try {
            String secretKey = cachedSecretKey.get();
            if (secretKey == null) {
                Optional<AdminUser> userOpt = adminUserRepository.findByUsername("admin");
                if (!userOpt.isPresent()) {
                    return null;
                }
                secretKey = userOpt.get().getSecretKey();
                cachedSecretKey.set(secretKey);
            }
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("push-admin")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            if (jwt.getClaim("role").asString().equals("admin")) {
                return jwt;
            }
            return null;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public void refreshSecretKey() {
        Optional<AdminUser> userOpt = adminUserRepository.findByUsername("admin");
        if (userOpt.isPresent()) {
            cachedSecretKey.set(userOpt.get().getSecretKey());
            LOG.info("JWT secret key cache refreshed");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
