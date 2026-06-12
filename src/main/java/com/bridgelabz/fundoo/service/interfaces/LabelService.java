package com.bridgelabz.fundoo.service.interfaces;

import com.bridgelabz.fundoo.dto.request.LabelRequest;
import com.bridgelabz.fundoo.dto.response.LabelResponse;
import com.bridgelabz.fundoo.dto.response.NoteResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LabelService {
    LabelResponse createLabel(LabelRequest labelRequest);
    List<LabelResponse> getAllLabels();
    LabelResponse getLabelById(Long id);
    LabelResponse updateLabel(Long id, LabelRequest labelRequest);
    void deleteLabel(Long id);

    NoteResponse attachLabel(Long noteId, Long labelId);
    NoteResponse detachLabel(Long noteId, Long labelId);
    Page<NoteResponse> getNotesByLabel(Long labelId, int page, int size, String sortBy, String direction);
}
