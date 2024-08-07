package com.jpa2.domain.comment.service;

import java.util.List;

import com.jpa2.domain.comment.Comment;

public interface CommentService {

	void save(Comment comment);
	
	Comment findById(Long id) throws Exception;
	
	List<Comment> findAll();
	
	void remove(Long id) throws Exception;
}