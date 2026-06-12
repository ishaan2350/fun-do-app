package com.bridgelabz.fundoo.service.interfaces;

import com.bridgelabz.fundoo.dto.request.ReminderRequest;
import com.bridgelabz.fundoo.dto.response.ReminderResponse;

import java.util.List;

public interface ReminderService {
    ReminderResponse addReminder(Long noteId, ReminderRequest reminderRequest);
    List<ReminderResponse> getRemindersOfNote(Long noteId);
    ReminderResponse getReminderById(Long id);
    ReminderResponse updateReminder(Long id, ReminderRequest reminderRequest);
    void deleteReminder(Long id);
    ReminderResponse snoozeReminder(Long id, Integer snoozeMinutes);
}
