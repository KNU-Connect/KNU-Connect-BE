package com.example.knu_connect.domain.user.repository;

import com.example.knu_connect.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String Email);
}