package com.jpa2.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpa2.domain.comment.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}