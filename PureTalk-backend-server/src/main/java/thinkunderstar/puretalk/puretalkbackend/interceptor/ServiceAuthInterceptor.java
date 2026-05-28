package thinkunderstar.puretalk.puretalkbackend.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ServiceAuthInterceptor implements HandlerInterceptor {

    @Value("${puretalk.api.token}")
    private String apiToken;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // 只拦截需要保护的接口（比如搜索）
        String path = request.getRequestURI();
        if (!path.startsWith("/api")) {
            return true;
        }

        // 校验请求头中的密钥,并设置loginId
        String token = request.getHeader("X-Service-Token");
        long userId = Long.parseLong(request.getHeader("X-User-Id"));
        if (apiToken.equals(token)) {
            StpUtil.login(userId);
            return true;
        }

        response.setStatus(403);
        return false;
    }
}