package com.community.soap.user.application;

import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.user.application.exception.UserErrorCode;
import com.community.soap.user.application.exception.UserException;
import com.community.soap.user.application.request.SignInRequest;
import com.community.soap.user.application.request.SignUpRequest;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.application.response.SignInResponse;
import com.community.soap.user.application.response.SignUpResponse;
import com.community.soap.user.domain.entity.User;
import com.community.soap.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Snowflake snowflake;

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.EMAIL_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private void checkEmailDuplication(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException(UserErrorCode.EMAIL_DUPLICATED);
        }
    }

    private void checkPassword(String rowPassword, String encryptedPassword) {
        if (rowPassword == null || rowPassword.isEmpty()) {
            throw new UserException(UserErrorCode.PASSWORD_NULL);
        }

        if (!passwordEncoder.matches(rowPassword, encryptedPassword)) {
            throw new UserException(UserErrorCode.PASSWORD_INCORRECT);
        }
    }

    @Transactional
    @Override
    public SignUpResponse signup(SignUpRequest request) {
        checkEmailDuplication(request.email());

        User register = User.register(
                snowflake.nextId(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname()
        );

        userRepository.save(register);

        return SignUpResponse.from(register);
    }

    @Transactional
    @Override
    public SignInResponse signIn(SignInRequest request) {
        User user = findUserByEmail(request.email());
        checkPassword(request.password(), user.getPassword());

        return SignInResponse.from(user);
    }

    @Transactional(readOnly = true)
    @Override
    public MyPageResponse myPage(Long userId) {
        User byUserId = findUserById(userId);

        return MyPageResponse.from(byUserId);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User byUserId = findUserById(userId);
        byUserId.delete(userId);
    }
}
