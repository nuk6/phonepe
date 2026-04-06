package org.example.persistence;

import org.example.model.User;

import java.util.Optional;

public interface UserDao {

    void save(User user);

    Optional<User> findById(String userId);
}

