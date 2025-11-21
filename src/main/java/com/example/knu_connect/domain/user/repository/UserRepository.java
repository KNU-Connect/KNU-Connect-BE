package com.example.knu_connect.domain.user.repository;

import com.example.knu_connect.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // User
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    // Mentor
    Optional<User> findByIdAndMentor(Long id, boolean mentor);
}