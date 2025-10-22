package com.che.bongpyung.domain.dto;

// src/main/java/com/che/bongpyung/dto/ChangePasswordForm.java

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePasswordForm {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
