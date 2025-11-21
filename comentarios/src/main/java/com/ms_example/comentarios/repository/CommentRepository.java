package com.ms_example.comentarios.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ms_example.comentarios.model.Comment;
import java.util.List;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{

    List<Comment> findByServiceId(Long serviceId);
    List<Comment> findByProfileId(Long profileId);
     
}
