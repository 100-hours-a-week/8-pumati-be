package com.tebutebu.apiserver.repository;

import com.tebutebu.apiserver.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByContentIgnoreCase(String content);

}
