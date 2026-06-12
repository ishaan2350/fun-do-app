package com.bridgelabz.fundoo.service.impl;

import com.bridgelabz.fundoo.entity.Reminder;
import com.bridgelabz.fundoo.repository.ReminderRepository;
import com.bridgelabz.fundoo.service.interfaces.EmailService;
import com.bridgelabz.fundoo.service.interfaces.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    private final ReminderRepository reminderRepository;
    private final EmailService emailService;
    private final NoteService noteService;

    public SchedulerService(ReminderRepository reminderRepository, EmailService emailService, NoteService noteService) {
        this.reminderRepository = reminderRepository;
        this.emailService = emailService;
        this.noteService = noteService;
    }

    // Checks and triggers reminders every minute
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndSendReminders() {
        log.debug("Scheduler checking for pending reminders...");
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> dueReminders = reminderRepository.findByNotifiedFalseAndRemindAtBefore(now);

        if (!dueReminders.isEmpty()) {
            log.info("Found {} reminders to trigger.", dueReminders.size());
            for (Reminder reminder : dueReminders) {
                try {
                    String recipientEmail = reminder.getNote().getOwner().getEmail();
                    String title = reminder.getNote().getTitle();
                    String description = reminder.getNote().getDescription();

                    // Send email notification
                    emailService.sendReminderEmail(recipientEmail, title, description);

                    // Mark as notified
                    reminder.setNotified(true);
                    reminderRepository.save(reminder);
                    log.info("Successfully triggered and updated reminder ID: {}", reminder.getId());
                } catch (Exception e) {
                    log.error("Failed to process reminder ID: {}: {}", reminder.getId(), e.getMessage());
                }
            }
        }
    }

    // Runs once a day at midnight (00:00) to purge notes in trash > 30 days
    @Scheduled(cron = "0 0 0 * * *")
    public void purgeOldTrashedNotesTask() {
        log.info("Starting scheduled task: Purging old trashed notes...");
        try {
            noteService.purgeOldTrashedNotes();
        } catch (Exception e) {
            log.error("Error occurred while purging old trashed notes: {}", e.getMessage());
        }
    }
}
