package com.jpa2.domain.comment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jpa2.domain.comment.dto.CommentSaveDto;
import com.jpa2.domain.comment.dto.CommentUpdateDto;
import com.jpa2.domain.comment.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommentController {

	private final CommentService commentService;
	
	@PostMapping("/comment/{postId}")
	@ResponseStatus(HttpStatus.CREATED)
	public void commentSave(@PathVariable(name = "postId") Long postId, CommentSaveDto commentSaveDto) {
		commentService.save(postId, commentSaveDto);
	}
	
	@PostMapping("/comment/{postId}/{commentId}")
	@ResponseStatus(HttpStatus.CREATED)
	public void reCommentSave(@PathVariable(name = "postId") Long postId,
							  @PathVariable(name = "commentId") Long commentId,
							  CommentSaveDto commentSaveDto) {
		
		commentService.saveReComment(postId, commentId, commentSaveDto);
	}
	
	@PutMapping("/comment/{commentId}")
	public void update(@PathVariable(name = "commentId") Long commentId,
					   CommentUpdateDto commentUpdateDto) {
		
		commentService.update(commentId, commentUpdateDto);
	}
	
	@DeleteMapping("/comment/{commentId}")
	public void delete(@PathVariable(name = "commentId") Long commentId) {
		commentService.remove(commentId);
	}
}