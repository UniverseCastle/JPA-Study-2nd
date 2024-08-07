package com.jpa2.domain.comment.dto;

import com.jpa2.domain.comment.Comment;

public record CommentSaveDto(String content) {

	public Comment toEntity() {
		return Comment.builder()
				.content(content)
				.build();
	}
}