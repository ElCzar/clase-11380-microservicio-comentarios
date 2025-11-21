package com.ms_example.comentarios.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ms_example.comentarios.model.Comment;

import com.ms_example.comentarios.service.CommentService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<Comment> getAllComments() {
        return commentService.getAllComments();
    }

    @GetMapping("/{id}")
    public Comment getCommentById(@PathVariable Long id) {
        return commentService.getCommentsById(id);
    }

    @GetMapping("/service-id/{id}")
    public List<Comment> getCommentByServiceId(@PathVariable Long id) {
        return commentService.getCommentByServiceId(id);
    }

    @GetMapping("/service-uuid/{serviceUuid}")
    public List<Comment> getCommentByServiceUuid(@PathVariable String serviceUuid) {
        // Convertir UUID a Long usando el mismo método que al guardar
        UUID uuid = UUID.fromString(serviceUuid);
        Long serviceLongId = Math.abs((long) uuid.hashCode());
        return commentService.getCommentByServiceId(serviceLongId);
    }

    @GetMapping("/profile-id/{id}")
    public List<Comment> getCommentByProfileId(@PathVariable Long id) {
        return commentService.getCommentByProfileId(id);
    }

    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        return commentService.createComment(comment);
    }

    @PutMapping("/update/{id}")
    public Comment updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        if (commentService.getCommentsById(id) == null) {
            return null;
        }
        return commentService.updateComment(id, comment);
    }

    @DeleteMapping("/delete/{id}")
    public Boolean deleteComment(@PathVariable Long id) {
        if (commentService.deleteComment(id)) {
            return true;
        }

        return false;
    }

}
