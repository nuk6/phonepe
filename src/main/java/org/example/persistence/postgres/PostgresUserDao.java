package org.example.persistence.postgres;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.persistence.UserDao;
import org.example.persistence.entity.UserEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile({"qa", "prod"})
@RequiredArgsConstructor
public class PostgresUserDao implements UserDao {

    private final JpaUserRepository jpaRepo;

    @Override
    public void save(User user) {
        jpaRepo.save(UserEntity.fromDomain(user));
    }

    @Override
    public Optional<User> findById(String userId) {
        return jpaRepo.findById(userId).map(UserEntity::toDomain);
    }
}

