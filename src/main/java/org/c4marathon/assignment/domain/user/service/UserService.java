package org.c4marathon.assignment.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.domain.user.dto.JoinResponse;
import org.c4marathon.assignment.domain.user.dto.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public String join(UserDto userDto){
        String userPhone = userDto.getUserPhone();
        if(duplicatedUser(userPhone)){
            User user = User.builder()
                    .userPhone(userDto.getUserPhone())
                    .userPassword(userDto.getUserPassword())
                    .userName(userDto.getUserName())
                    .userBirth(userDto.getUserBirth())
                    .build();

            userRepository.save(user);
            // 메인계좌 생성 메서드 호출, 호출부는 트랜잭션 이벤트리스너

            JoinResponseDto joinResponseDto = JoinResponseDto.builder()
                    .responseMsg(JoinResponse.SUCCESS.toString())
                    .build();
            return joinResponseDto.toString();
        } else{
            throw new HttpClientErrorException(HttpStatusCode.valueOf(400));
        }
    }

    private boolean duplicatedUser(String userPhone){
        if(userRepository.existsByUserPhone(userPhone)){
            return false;
        }
        return true;
    }
}
