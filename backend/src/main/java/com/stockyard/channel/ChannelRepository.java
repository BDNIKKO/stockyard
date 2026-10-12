package com.stockyard.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    List<Channel> findByMakerIdOrderByNameAsc(Long makerId);
    Optional<Channel> findByIdAndMakerId(Long id, Long makerId);
    boolean existsByMakerIdAndNameIgnoreCase(Long makerId, String name);
}
