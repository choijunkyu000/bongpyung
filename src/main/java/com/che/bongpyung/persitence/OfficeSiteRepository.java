package com.che.bongpyung.persitence;


import com.che.bongpyung.domain.OfficeSite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfficeSiteRepository extends JpaRepository<OfficeSite, Long> {
    Optional<OfficeSite> findFirstByActiveTrue();
}
