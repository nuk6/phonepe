package org.example.persistence.postgres;

import org.example.persistence.entity.MutualFundEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

@Profile({"local", "qa", "prod"})
public interface JpaMutualFundRepository extends JpaRepository<MutualFundEntity, String> {

    @Modifying
    @Query("UPDATE MutualFundEntity f SET f.currentNav = :nav WHERE f.id = :fundId")
    int updateNavById(@Param("fundId") String fundId, @Param("nav") BigDecimal nav);
}

