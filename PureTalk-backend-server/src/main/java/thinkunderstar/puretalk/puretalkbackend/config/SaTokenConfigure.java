package thinkunderstar.puretalk.puretalkbackend.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import thinkunderstar.puretalk.puretalkbackend.interceptor.ServiceAuthInterceptor;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    private final ServiceAuthInterceptor serviceAuthInterceptor;

    public SaTokenConfigure(ServiceAuthInterceptor serviceAuthInterceptor) {
        this.serviceAuthInterceptor = serviceAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/register/**",
                        "/user/login/code",
                        "/admin/login",
                        "/post/search",
                        "/api/**"
                );

        // 注册服务间认证拦截器，只拦截 /api/** 路径
        registry.addInterceptor(serviceAuthInterceptor)
                .addPathPatterns("/api/**");
    }
}

