package com.example.notemanager.repository;

import com.example.notemanager.model.Note;
import com.example.notemanager.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    @Query("SELECT n FROM Note n JOIN FETCH n.user WHERE n.user = :user")
    Page<Note> findByUser(User user, Pageable pageable);

    @Query("SELECT n FROM Note n JOIN FETCH n.user WHERE n.id = :id AND n.user = :user")
    Optional<Note> findByIdAndUser(@Param("id") Long id, @Param("user") User user);

    @Query("SELECT n FROM Note n WHERE n.user = :user AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Note> findByUserAndKeyword(@Param("user") User user,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);
}
