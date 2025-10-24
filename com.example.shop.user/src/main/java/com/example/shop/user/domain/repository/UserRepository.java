package com.example.shop.user.domain.repository;

import com.example.shop.user.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID userId);

    Optional<User> findByUsername(String username);

    Page<User> searchUsers(String username, String nickname, String email, Pageable pageable);

    long count();
}
