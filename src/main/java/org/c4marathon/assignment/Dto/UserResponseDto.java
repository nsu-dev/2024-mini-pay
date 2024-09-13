package org.c4marathon.assignment.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String name;
    private String registrationNum;

    @Builder
    public UserResponseDto(Long userId, String name, String registrationNum) {
        this.userId = userId;
        this.name = name;
        this.registrationNum = registrationNum;
    }
}