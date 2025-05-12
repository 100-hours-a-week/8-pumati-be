package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.AttendanceWeekly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceWeeklyRepository extends JpaRepository<AttendanceWeekly, Long> {

    Optional<AttendanceWeekly> findByMemberId(Long memberId);

}
