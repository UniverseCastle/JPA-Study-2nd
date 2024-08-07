package com.jpa2.domain.post.service;

import javax.annotation.processing.FilerException;

import org.springframework.data.domain.Pageable;

import com.jpa2.domain.post.cond.PostSearchCondition;
import com.jpa2.domain.post.dto.PostInfoDto;
import com.jpa2.domain.post.dto.PostPagingDto;
import com.jpa2.domain.post.dto.PostSaveDto;
import com.jpa2.domain.post.dto.PostUpdateDto;

public interface PostService {
	
	/**
	 * 게시글 등록
	 */
	void save(PostSaveDto postSaveDto) throws FilerException;
	
	/**
	 * 게시글 수정
	 */
	void update(Long id, PostUpdateDto postUpdateDto);
	
	/**
	 * 게시글 삭제
	 */
	void delete(Long id);
	
	/**
	 * 게시글 1개 조회
	 */
	PostInfoDto getPostInfo(Long id);
	
	/**
	 * 검색 조건에 따른 게시글 리스트 조회 + 페이징
	 */
	PostPagingDto getPostList(Pageable pageable, PostSearchCondition postSearchCondition);
}