package org.c4marathon.assignment.user.service;

import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.dto.UserMapper;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.c4marathon.assignment.domain.user.dto.JoinResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class UserServiceTest {

    @MockBean
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @DisplayName("요청 받은 유저 정보를 저장하고, 응답 메시지를 검증한다.")
    @Test
    void join(){
        //given
        UserDto userDto = new UserDto("010-8337-6023", "조아빈", "20000604", "pw123");
        Mockito.when(userRepository.save(any(User.class))).thenReturn(UserMapper.toUser(userDto));

        //when
        JoinResponseDto joinResponseDto = userService.join(userDto);

        //then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getUserPhone()).isEqualTo(userDto.getUserPhone());
        assertThat(capturedUser.getUserPassword()).isEqualTo(userDto.getPassword());
        assertThat(capturedUser.getUserName()).isEqualTo(userDto.getUserName());
        assertThat(capturedUser.getUserBirth()).isEqualTo(userDto.getUserBirth());

        assertThat(joinResponseDto.responseMsg()).isEqualTo("회원가입 성공!");
    }
}
