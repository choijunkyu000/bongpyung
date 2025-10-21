package com.che.bongpyung.persitence;

import com.che.bongpyung.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);
    Optional<User> findByUserIdAndEnabledTrue(String userId);

    List<User> findAllByOrderByDisplayNameAscUserIdAsc();

    List<User> findAllByUseYnTrueOrderByDisplayNameAscUserIdAsc();

    boolean existsByUserId(String userId);
}