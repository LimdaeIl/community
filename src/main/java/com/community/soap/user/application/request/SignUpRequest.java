package com.community.soap.user.application.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(
        @NotBlank(message = "이메일: 이메일은 필수입니다.")
        @Email(message = "이메일: 유효하지 않은 이메일 형식입니다.")
        String email,

        @NotBlank(message = "비밀번호: 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}|\\[\\]:\";'<>?,./]).{8,}$",
                message = "비밀번호: 비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다."
        )
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,

        @NotBlank(message = "닉네임: 닉네임은 필수입니다.")
        @Pattern(
                regexp = "^[A-Za-z0-9가-힣!@#$%^&*()_+\\-={}|\\[\\]:\";'<>?,./]{1,12}$",
                message = "닉네임: 닉네임은 12자 이내이며, 영문 대소문자, 한글, 숫자, 특수기호만 사용할 수 있습니다."
        )
        String nickname
) {

}
