package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByTermAndNumber(int term, int number);

    boolean existsByTermAndNumber(int term, int number);

}
