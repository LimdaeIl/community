package com.community.soap.user.application;

import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.user.application.request.SignInRequest;
import com.community.soap.user.application.request.SignUpRequest;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.application.response.SignInResponse;
import com.community.soap.user.application.response.SignUpResponse;
import com.community.soap.user.domain.entity.User;
import com.community.soap.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final Snowflake snowflake;

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with email " + email + " not found"));
    }

    private User findByUserId(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with id " + userId + " not found"));
    }

    @Transactional
    @Override
    public SignUpResponse signup(SignUpRequest request) {
        User register = User.register(
                snowflake.nextId(),
                request.email(),
                request.password()
        );

        userRepository.save(register);

        return SignUpResponse.from(register);
    }

    @Transactional
    @Override
    public SignInResponse signIn(SignInRequest request) {
        User byEmail = findByEmail(request.email());

        if (!byEmail.getPassword().equals(request.password())) {
            throw new IllegalStateException("Passwords don't match");
        }

        return SignInResponse.from(byEmail);
    }

    @Transactional(readOnly = true)
    @Override
    public MyPageResponse myPage(Long userId) {
        User byUserId = findByUserId(userId);

        return MyPageResponse.from(byUserId);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User byUserId = findByUserId(userId);
        byUserId.delete(userId);
    }
}
