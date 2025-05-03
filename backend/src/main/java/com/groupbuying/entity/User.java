package com.groupbuying.entity;

import javax.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String nickname;

    @Column(unique = true)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(length = 64)
    private String paymentPassword;

    @Column(length = 255)
    private String securityQuestion;  // 密保问题

    @Column(length = 255)
    private String securityAnswer;    // 密保答案

    private BigDecimal accountBalance = BigDecimal.ZERO;

    private boolean active = true;

    @Column(length = 255)
    private String avatar;

    public boolean verifyPaymentPassword(String inputPassword) {
        if (paymentPassword == null || inputPassword == null) {
            return false;
        }
        return paymentPassword.equals(inputPassword);
    }

    public void setPaymentPassword(String paymentPassword) {
        this.paymentPassword = paymentPassword;
    }
} 