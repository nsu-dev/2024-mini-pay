package org.c4marathon.assignment.common.support;

import java.util.Date;

import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.common.fixture.UserFixture;
import org.c4marathon.assignment.common.jwt.JwtProvider;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class ApiTestSupport {

	protected User loginUser;
	protected String token;
	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected UserRepository userRepository;
	@Autowired
	protected AccountRepository accountRepository;
	@Autowired
	protected PasswordEncoder passwordEncoder;
	@Autowired
	protected JwtProvider jwtProvider;

	protected String toJson(Object object) throws JsonProcessingException {
		return objectMapper.writeValueAsString(object);
	}

	public void setUpMember() {
		if (loginUser != null) {
			return;
		}
		this.loginUser = userRepository.save(UserFixture.userWithEncodingPassword(passwordEncoder));
		this.token = jwtProvider.createToken(loginUser, new Date());
	}

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		setUpMember();
	}
}
