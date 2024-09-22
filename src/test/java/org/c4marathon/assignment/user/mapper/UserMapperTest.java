package org.c4marathon.assignment.user.mapper;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.UserMapper;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserMapper userMapper;

	@DisplayName("회원가입 정보로 유저 데이터를 생성할 때 비밀번호가 인코딩(암호화)된다.")
	@Test
	void toUserFromJoinRequestDto() {
		// given
		JoinRequestDto request = new JoinRequestDto("abc@mini.com", "mini1234", "미니페이");
		given(passwordEncoder.encode(anyString())).willReturn("EncodingPassword");

		// when
		User user = userMapper.toUserFromJoinRequestDto(request, passwordEncoder);

		// then
		assertAll(
			() -> assertThat(user.getPassword()).isEqualTo("EncodingPassword"),
			() -> assertThat(user.getName()).isEqualTo(request.name()),
			() -> assertThat(user.getEmail()).isEqualTo(request.email())
		);

	}
}
