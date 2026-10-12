package com.stockyard.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MakerRepository extends JpaRepository<Maker, Long> {
    Optional<Maker> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
