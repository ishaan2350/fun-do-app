package com.bridgelabz.fundoo.repository;

import com.bridgelabz.fundoo.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByNoteId(Long noteId);
    List<Reminder> findByNotifiedFalseAndRemindAtBefore(LocalDateTime dateTime);
}
