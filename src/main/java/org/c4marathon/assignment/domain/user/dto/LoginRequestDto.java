package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public record LoginRequestDto(
	@NotBlank(message = "전화번호는 공백일 수 없습니다.") String userPhone,
	@NotBlank(message = "비밀번호는 공백일 수 없습니다.") String userPassword) {}
