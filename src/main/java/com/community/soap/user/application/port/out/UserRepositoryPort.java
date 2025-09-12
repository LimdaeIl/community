package com.community.soap.user.application.port.out;

import com.community.soap.user.domain.entity.User;
import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByEmail(String email);

    User save(User user);

    Optional<User> findByUserId(Long userId);

    boolean existsByEmail(String email);
}
