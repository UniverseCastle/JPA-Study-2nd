package com.jpa2.domain.post.dto;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public record PostUpdateDto(Optional<String> title,
							Optional<String> content,
							Optional<MultipartFile> uploadFile) {
}