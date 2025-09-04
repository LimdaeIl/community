package com.community.soap.user.domain.repository;

import com.community.soap.user.domain.entity.User;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    User save(User user);

    Optional<User> findByUserId(Long userId);

    boolean existsByEmail(String email);
}
