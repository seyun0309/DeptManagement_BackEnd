package sunjin.DeptManagement_BackEnd.global.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import sunjin.DeptManagement_BackEnd.global.auth.exception.CustomException;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 커스텀 예외를 던지거나 에러 응답을 작성할 수 있습니다.
        throw new CustomException("접근 권한이 없습니다.");
    }
}
