package com.jpa2.domain.post.controller;

import javax.annotation.processing.FilerException;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jpa2.domain.post.cond.PostSearchCondition;
import com.jpa2.domain.post.dto.PostInfoDto;
import com.jpa2.domain.post.dto.PostPagingDto;
import com.jpa2.domain.post.dto.PostSaveDto;
import com.jpa2.domain.post.dto.PostUpdateDto;
import com.jpa2.domain.post.service.PostService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;
	
	/**
	 * 게시글 저장
	 */
	@ResponseStatus(HttpStatus.CREATED) // 상태코드 201
	@PostMapping("/post")
	public void save(@Valid @ModelAttribute PostSaveDto postSaveDto) {
		try {
			postService.save(postSaveDto);
		} catch (FilerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 게시글 수정
	 */
	@ResponseStatus(HttpStatus.OK)
	@PutMapping("/post/{postId}")
	public void update(@PathVariable(name = "postId") Long postId,
					   @ModelAttribute PostUpdateDto postUpdateDto) {
		
		postService.update(postId, postUpdateDto);
	}
	
	/**
	 * 게시글 삭제
	 */
	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping("/post/{postId}")
	public void delete(@PathVariable(name = "postId") Long postId) {
		postService.delete(postId);
	}
	
	/**
	 * 게시글 조회
	 */
	@GetMapping("/post/{postId}")
	public ResponseEntity<PostInfoDto> getInfo(@PathVariable(name = "postId") Long postId) {
		return ResponseEntity.ok(postService.getPostInfo(postId));
	}
	
	/**
	 * 게시글 검색
	 */
	@GetMapping("/post")
	public ResponseEntity<PostPagingDto> search(Pageable pageable,
								 @ModelAttribute PostSearchCondition postSearchCondition) {
		
		return ResponseEntity.ok(postService.getPostList(pageable, postSearchCondition));
	}
}
