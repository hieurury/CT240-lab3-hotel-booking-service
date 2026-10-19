package com.ctu.repository;

import com.ctu.model.User;

public interface UserRepository {
    boolean existsByEmail(String email);
    User save(User user);
}
