package com.jpa2.domain.post.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jpa2.domain.comment.Comment;
import com.jpa2.domain.comment.dto.CommentInfoDto;
import com.jpa2.domain.member.dto.MemberInfoDto;
import com.jpa2.domain.post.Post;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostInfoDto {

	private Long postId; // Post의 ID
	private String title; // 제목
	private String content; // 내용
	private String filePath; // 업로드 파일 경로
	
	private MemberInfoDto writerDto; // 작성자에 대한 정보
	
	private List<CommentInfoDto> commentInfoDtoList; // 댓글 정보들
	
	public PostInfoDto(Post post) {
		this.postId = post.getId();
		this.title = post.getTitle();
		this.content = post.getContent();
		this.filePath = post.getFilePath();
		
		this.writerDto = new MemberInfoDto(post.getWriter());
		
		/**
         * CommentList는 댓글과 대댓글이 모두 섞여있는 상태
         * Comment와 Recomment는 단지 parent가 있는지 없는지로만 구분지어지므로, JPA는 댓글과 대댓글을 구분할 방법이 없음
         * 따라서 CommentList를 통해 댓글과 대댓글을 모두 가져옴
         * (추가로 이때, 배치 사이즈를 100으로 설정해 주었기 때문에 쿼리는 1번 혹은 N/100만큼 발생)
         */
		Map<Comment, List<Comment>> commentListMap = post.getCommentList().stream()
				.filter(comment -> comment.getParent() != null) // Comment의 parent가 null이 아닌, 즉 댓글이 아닌 대댓글인 것들만 가져옴
				.collect(Collectors.groupingBy(Comment::getParent)); // Map에는 <댓글, List<해당 댓글에 달린 대댓글>>의 형식으로 그룹핑
		
		/**
         * 그룹지은 것들 중 keySet, 즉 댓글들을 가지고 옴
         * 댓글들을 CommentInfoDto로 변환
         * 이때 CommentInfoDto의 생성자로 댓글과 해당 댓글에 달린 대댓글들을 인자로 넣어줌
         */
		commentInfoDtoList = commentListMap.keySet().stream()
				.map(comment -> new CommentInfoDto(comment, commentListMap.get(comment)))
				.toList();
	}
}