package ru.yandex.practicum.kafka.telemetry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.kafka.telemetry.entity.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    @Query("SELECT DISTINCT s FROM Scenario s " +
           "LEFT JOIN FETCH s.conditions " +
           "LEFT JOIN FETCH s.actions " +
           "WHERE s.hubId = :hubId")
    List<Scenario> findByHubId(@Param("hubId") String hubId);
    
    @Query("SELECT DISTINCT s FROM Scenario s " +
           "LEFT JOIN FETCH s.conditions " +
           "LEFT JOIN FETCH s.actions " +
           "WHERE s.hubId = :hubId AND s.name = :name")
    Optional<Scenario> findByHubIdAndName(@Param("hubId") String hubId, @Param("name") String name);
}

