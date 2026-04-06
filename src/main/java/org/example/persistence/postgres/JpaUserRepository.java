package org.example.persistence.postgres;

import org.example.persistence.entity.UserEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile({"local", "qa", "prod"})
public interface JpaUserRepository extends JpaRepository<UserEntity, String> {
}

