package com.ms_example.comentarios.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ms_example.comentarios.model.Comment;
import com.ms_example.comentarios.repository.CommentRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Comment getCommentsById(Long id) {
        return commentRepository.findById(id).orElse(null);
    }

    public List<Comment> getCommentByServiceId(Long serviceId) {
        return commentRepository.findByServiceId(serviceId);
    }

    public List<Comment> getCommentByProfileId(Long profileId) {
        return commentRepository.findByProfileId(profileId);
    }

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long id, Comment updatedComment) {
        if (commentRepository.existsById(id)) {
            updatedComment.setId(id);
            commentRepository.save(updatedComment);
            return updatedComment;
        }
        return null;
    }

    public boolean deleteComment(Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
