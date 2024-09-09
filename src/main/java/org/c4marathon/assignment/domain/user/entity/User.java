package org.c4marathon.assignment.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long userId;

    @Column(name = "userPhone", unique = true, nullable = false)
    String userPhone;

    @Column(name = "userPassword", nullable = false)
    String userPassword;

    @Column(name = "userName", nullable = false)
    String userName;

    @Column(name = "userBirth", nullable = false)
    String userBirth;



    @Builder
    private User(String userPhone, String userPassword, String userName, String userBirth){
        this.userPhone = userPhone;
        this.userPassword = userPassword;
        this.userName = userName;
        this.userBirth = userBirth;
    }
}