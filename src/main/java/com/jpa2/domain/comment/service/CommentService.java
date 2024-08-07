package com.jpa2.domain.comment.service;

import com.jpa2.domain.comment.dto.CommentSaveDto;
import com.jpa2.domain.comment.dto.CommentUpdateDto;
import com.jpa2.domain.comment.exception.CommentException;

public interface CommentService {

	void save(Long postId, CommentSaveDto commentSaveDto);
	
	void saveReComment(Long postId, Long parentId, CommentSaveDto commentSaveDto);
	
	void update(Long id, CommentUpdateDto commentUpdateDto);
	
	void remove(Long id) throws CommentException;
}