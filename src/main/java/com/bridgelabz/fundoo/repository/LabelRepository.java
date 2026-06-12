package com.bridgelabz.fundoo.repository;

import com.bridgelabz.fundoo.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findByUserId(Long userId);
    Optional<Label> findByIdAndUserId(Long id, Long userId);
    Optional<Label> findByNameAndUserId(String name, Long userId);
}
