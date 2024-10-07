package org.c4marathon.assignment.global.config;

import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import org.c4marathon.assignment.domain.user.exception.UserException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/user/join", "/user/login").permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session
				.maximumSessions(1)
				.maxSessionsPreventsLogin(true)
			)
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					throw new UserException(USER_INVALID_FAIL);
				})
			);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
