package com.example.notemanager.service;

import com.example.notemanager.exception.ExceptionMessages;
import com.example.notemanager.exception.NoteServiceException;
import com.example.notemanager.model.Note;
import com.example.notemanager.model.User;
import com.example.notemanager.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserService userService;

    public Page<Note> listAll(PageRequest pageRequest) {
        User currentUser = getAuthenticatedUser();
        return noteRepository.findByUser(currentUser, pageRequest);
    }

    public Note getById(long id) {
        return findNoteByIdAndUser(id, getAuthenticatedUser());
    }

    public Note create(Note note) {
        validateNoteData(note);
        note.setUser(getAuthenticatedUser());
        return noteRepository.save(note);
    }

    @Transactional
    public Note update(Note note) {
        Note existingNote = findNoteByIdAndUser(note.getId(), getAuthenticatedUser());
        existingNote.setTitle(note.getTitle());
        existingNote.setContent(note.getContent());
        return noteRepository.save(existingNote);
    }

    public void delete(long id) {
        Note note = findNoteByIdAndUser(id, getAuthenticatedUser());
        noteRepository.delete(note);
    }

    public Page<Note> search(String keyword, PageRequest pageRequest) {
        User currentUser = getAuthenticatedUser();
        return noteRepository.findByUserAndKeyword(currentUser, keyword, pageRequest);
    }

    private User getAuthenticatedUser() {
        return userService.getAuthenticatedUser();
    }

    private Note findNoteByIdAndUser(long id, User user) {
        return noteRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NoteServiceException(ExceptionMessages.NOTE_NOT_FOUND.getMessage()));
    }

    private void validateNoteData(Note note) {
        if (note.getTitle() == null || note.getTitle().isEmpty()) {
            throw new NoteServiceException(ExceptionMessages.INVALID_NOTE_DATA.getMessage());
        }
    }

}
