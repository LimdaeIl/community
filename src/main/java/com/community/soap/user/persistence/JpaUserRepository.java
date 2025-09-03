package com.community.soap.user.persistence;

import com.community.soap.user.domain.entity.User;
import com.community.soap.user.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {

}
