package org.c4marathon.assignment.common.jwt;

import java.io.IOException;

import org.c4marathon.assignment.common.exception.runtime.CustomJwtException;
import org.c4marathon.assignment.common.exception.runtime.ExceptionResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (CustomJwtException e) {

			log.error("JWT 검증 실패로 인한 예외 발생 : {}", e.getMessage());

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401 Unauthorized
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");

			ExceptionResponse errorResponse = new ExceptionResponse(e.getMessage(), e.getCode());

			ObjectMapper mapper = new ObjectMapper();
			String jsonResponse = mapper.writeValueAsString(errorResponse);

			response.getWriter().write(jsonResponse);
		}
	}
}
