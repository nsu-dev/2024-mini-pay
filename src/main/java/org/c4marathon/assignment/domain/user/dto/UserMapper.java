package org.c4marathon.assignment.domain.user.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.c4marathon.assignment.domain.user.entity.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {
    public static User toUser(UserDto userDto){
        return User.builder()
                .userPhone(userDto.userPhone)
                .userPassword(userDto.userPassword)
                .userName(userDto.userName)
                .userBirth(userDto.userBirth)
                .build();
    }
}
