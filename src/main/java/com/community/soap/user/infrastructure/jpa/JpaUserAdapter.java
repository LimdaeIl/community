package com.community.soap.user.infrastructure.jpa;

import com.community.soap.user.domain.entity.User;
import com.community.soap.user.application.port.out.UserRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserAdapter extends JpaRepository<User, Long>, UserRepositoryPort {

}
