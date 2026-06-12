package com.bridgelabz.fundoo.repository;

import com.bridgelabz.fundoo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndDeletedFalse(String email);
    boolean existsByEmail(String email);
    
    Optional<User> findByIdAndDeletedFalse(Long id);
    Page<User> findByDeletedFalse(Pageable pageable);
    Page<User> findByEmailContainingIgnoreCaseAndDeletedFalse(String email, Pageable pageable);
}
