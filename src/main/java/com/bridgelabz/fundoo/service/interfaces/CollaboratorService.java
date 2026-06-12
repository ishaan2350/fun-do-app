package com.bridgelabz.fundoo.service.interfaces;

import com.bridgelabz.fundoo.dto.request.CollaboratorRequest;
import com.bridgelabz.fundoo.dto.response.CollaboratorResponse;

import java.util.List;

public interface CollaboratorService {
    CollaboratorResponse addCollaborator(Long noteId, CollaboratorRequest collaboratorRequest);
    List<CollaboratorResponse> getCollaboratorsOfNote(Long noteId);
    CollaboratorResponse getCollaboratorById(Long id);
    void removeCollaborator(Long noteId, Long collaboratorId);
}
