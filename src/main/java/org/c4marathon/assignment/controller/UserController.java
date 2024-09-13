package org.c4marathon.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //사용자 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody UserRequestDto userRequestDto){
        UserResponseDto responseDto = userService.registerUser(userRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    //적금 계좌 추가
    @PostMapping("/{userId}/savings")
    public ResponseEntity<Void> addSavingsAccount(@PathVariable Long userId,
                                                  @RequestParam String type,
                                                  @RequestParam int balance){
        userService.addSavingsAccount(userId, type, balance);
        return ResponseEntity.ok().build();
    }

    //메인 계좌에서 적금 계좌로 송금
    @PostMapping("/{userId}//move-to-savings")
    public ResponseEntity<String> transferToSavings(@PathVariable Long userId,
                                                    @RequestParam Long savingsAccountId,
                                                    @RequestParam int money){
        boolean success = userService.transferToSavings(userId, savingsAccountId, money);
        return success ? ResponseEntity.ok("송금 성공") : ResponseEntity.badRequest().body("송금 실패");
    }
}
