package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.AttendanceDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceDailyRepository extends JpaRepository<AttendanceDaily, Long> {

    List<AttendanceDaily> findByMemberId(Long memberId);

    List<AttendanceDaily> findByMemberIdAndCheckedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    boolean existsByMemberIdAndCheckedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

}
