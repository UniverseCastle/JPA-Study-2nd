package com.jpa2.domain.post.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

import com.jpa2.domain.post.Post;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostPagingDto {

	private int totalPageCount; // 총 몇 페이지가 존재하는지
	private int currentPageNum; // 현재 몇 페이지 인지
	private long totalElementCount; // 존재하는 게시글의 총 개수
	private int currentPageElementCount; // 현재 페이지에 존재하는 게시글 수
	
	private List<BriefPostInfo> simpleLectureDtoList = new ArrayList<>();
	
	public PostPagingDto(Page<Post> searchResults) {
		this.totalPageCount = searchResults.getTotalPages();
		this.currentPageNum = searchResults.getNumber();
		this.totalElementCount = searchResults.getTotalElements();
		this.currentPageElementCount = searchResults.getNumberOfElements();
		this.simpleLectureDtoList = searchResults.getContent().stream().map(BriefPostInfo::new).toList();
	}
}