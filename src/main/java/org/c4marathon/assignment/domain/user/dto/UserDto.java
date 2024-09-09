package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserDto {
    @NotBlank
    String userPhone;
    @NotBlank
    String userPassword;
    @NotBlank
    String userName;
    @NotBlank
    String userBirth;
}