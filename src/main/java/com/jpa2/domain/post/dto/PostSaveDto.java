package com.jpa2.domain.post.dto;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.jpa2.domain.post.Post;

import jakarta.validation.constraints.NotBlank;

public record PostSaveDto(@NotBlank(message = "제목을 입력해주세요.") String title,
						  @NotBlank(message = "내용을 입력해주세요.") String content,
						  Optional<MultipartFile> uploadFile) {
	
	public Post toEntity() {
		return Post.builder()
				.title(title)
				.content(content)
				.build();
	}
}