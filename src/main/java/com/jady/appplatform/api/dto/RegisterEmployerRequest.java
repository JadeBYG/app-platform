package com.jady.appplatform.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterEmployerRequest {
    @Email @NotBlank
    public String email;

    @NotBlank
    public String password;

    @NotBlank
    public String inviteCode;
}