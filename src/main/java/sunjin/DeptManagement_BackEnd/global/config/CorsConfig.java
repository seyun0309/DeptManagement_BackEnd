package sunjin.DeptManagement_BackEnd.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://54.180.86.172",
                        "http://54.180.86.172:80",
                        "http://localhost:3000",
                        "http://sjdeptmanagement.duckdns.org")// 허용할 origin 목록
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*"); // 허용할 HTTP 헤더
    }
}
