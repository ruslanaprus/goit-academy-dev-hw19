package com.example.notemanager.mvc.controller;

import com.example.notemanager.exception.ExceptionMessages;
import com.example.notemanager.exception.NoteServiceException;
import com.example.notemanager.model.Note;
import com.example.notemanager.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteMvcController {
    private static final String REDIRECT_NOTE_LIST = "redirect:/note/list";

    private final NoteService noteService;

    @GetMapping("/list")
    public ModelAndView listAll(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Note> notePage = noteService.listAll(pageRequest);

        ModelAndView modelAndView = new ModelAndView("note/list");
        modelAndView.addObject("notes", notePage.getContent());
        modelAndView.addObject("currentPage", notePage.getNumber());
        modelAndView.addObject("totalPages", notePage.getTotalPages());
        modelAndView.addObject("totalItems", notePage.getTotalElements());
        modelAndView.addObject("size", size);
        return modelAndView;
    }

    @PostMapping("/delete")
    public ModelAndView delete(@RequestParam("id") Long id) {
        noteService.delete(id);
        return new ModelAndView(REDIRECT_NOTE_LIST);
    }

    @GetMapping("/edit")
    public ModelAndView edit(@RequestParam Long id) {
        ModelAndView editNote = new ModelAndView("note/edit");
        editNote.addObject("note", noteService.getById(id));
        return editNote;
    }

    @PostMapping("/edit")
    public ModelAndView editById(@Valid @ModelAttribute Note note, BindingResult result) {
        if(result.hasErrors()) {
            throw new NoteServiceException(ExceptionMessages.INVALID_NOTE_DATA.getMessage());
        }
        noteService.update(note);
        return new ModelAndView(REDIRECT_NOTE_LIST);
    }

    @GetMapping("/create")
    public ModelAndView create() {
        ModelAndView createNote = new ModelAndView("note/create");
        return createNote;
    }

    @PostMapping("/create")
    public ModelAndView create(@Valid @ModelAttribute Note note, BindingResult result) {
        if(result.hasErrors()) {
            throw new NoteServiceException(ExceptionMessages.INVALID_NOTE_DATA.getMessage());
        }
        noteService.create(note);
        return new ModelAndView(REDIRECT_NOTE_LIST);
    }

    @GetMapping("/search")
    public ModelAndView searchNotes(@RequestParam String keyword,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Note> notePage = noteService.search(keyword, pageRequest);

        ModelAndView modelAndView = new ModelAndView("note/list");
        modelAndView.addObject("notes", notePage.getContent());
        modelAndView.addObject("currentPage", notePage.getNumber());
        modelAndView.addObject("totalPages", notePage.getTotalPages());
        modelAndView.addObject("totalItems", notePage.getTotalElements());
        modelAndView.addObject("size", size);
        modelAndView.addObject("keyword", keyword);
        return modelAndView;
    }
}
