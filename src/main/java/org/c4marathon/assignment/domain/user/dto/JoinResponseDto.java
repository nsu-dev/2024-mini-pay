package org.c4marathon.assignment.domain.user.dto;

import lombok.Builder;

@Builder
public record JoinResponseDto(String responseMsg) {
    @Override
    public String toString(){
        return responseMsg;
    }
}
