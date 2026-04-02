package com.letsgettesty.backend.user;

import java.util.Optional;

import com.letsgettesty.backend.model.User;

public interface UserRepository {

    Optional<User> findById(int id);
}
