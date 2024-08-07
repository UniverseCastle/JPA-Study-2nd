package com.jpa2.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.LongStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.jpa2.domain.comment.Comment;
import com.jpa2.domain.comment.repository.CommentRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class CommentServiceTest {

	@Autowired
	CommentService commentService;
	
	@Autowired
	CommentRepository commentRepository;
	
	@Autowired
	EntityManager em;
	
	private void clear() {
		em.flush();
		em.clear();
	}
	
	private Long saveComment() {
		Comment comment = Comment.builder()
				.content("댓글")
				.build();
		Long id = commentRepository.save(comment).getId();
		clear();
		
		return id;
	}
	
	private Long saveReComment(Long parentId) {
		Comment parent = commentRepository.findById(parentId).orElse(null);
		Comment comment = Comment.builder()
				.content("댓글")
				.parent(parent)
				.build();
		Long id = commentRepository.save(comment).getId();
		clear();
		
		return id;
	}
	
	
	//== Test ==//
	
	
	/**
	 * 댓글을 삭제하는 경우
	 * 대댓글이 남아있는 경우
	 * DB와 화면에서는 지워지지 않고, "삭제된 댓글입니다." 표시
	 */
//	@Test
	public void 댓글삭제_대댓글이_남아있는_경우() throws Exception {
		// given
		Long commentId = saveComment();
		
		saveReComment(commentId);
		saveReComment(commentId);
		saveReComment(commentId);
		saveReComment(commentId);
		
		Assertions.assertThat(commentService.findById(commentId).getChildList().size()).isEqualTo(4);
		
		// when
		commentService.remove(commentId); // 댓글 삭제 메서드
		clear();
		
		// then
		Comment findComment = commentService.findById(commentId); // 댓글 불러오기
		
		assertThat(findComment).isNotNull(); // 댓글 유효한지 검사
		assertThat(findComment.isRemoved()).isTrue(); // 댓글 삭제 되었는지 검사
		assertThat(findComment.getChildList().size()).isEqualTo(4); // 대댓글 수 검사
	}

	/**
	 * 댓글 삭제하는 경우
	 * 대댓글이 아예 존재하지 않는 경우 -> 곧바로 DB에서 삭제
	 */
//	@Test
	public void 댓글삭제_대댓글이_없는_경우() throws Exception {
		// given
		Long commentId = saveComment();
		
		// when
		commentService.remove(commentId);
		clear();
		
		// then
		Assertions.assertThat(commentService.findAll().size()).isSameAs(0);
		assertThat(assertThrows(Exception.class, () -> commentService.findById(commentId)).getMessage()).isEqualTo("댓글이 없습니다.");
	}
	
	/**
	 * 댓글을 삭제하는 경우
	 * 대댓글이 존재하나 모두 삭제된 경우
	 * 댓글과 달려있는 대댓글 모두 DB에서 일괄 삭제
	 * 화면상에서도 표시되지 않음
	 */
//	@Test
	public void 댓글삭제_대댓글이_존재하나_모두_삭제된_대댓글인_경우() throws Exception {
		// given
		Long commentId = saveComment();
		Long reComment1Id = saveReComment(commentId);
		Long reComment2Id = saveReComment(commentId);
		Long reComment3Id = saveReComment(commentId);
		Long reComment4Id = saveReComment(commentId);
		
		Assertions.assertThat(commentService.findById(commentId).getChildList().size()).isEqualTo(4);
		clear();
		
		commentService.remove(reComment1Id);
		clear();
		
		commentService.remove(reComment2Id);
		clear();
		
		commentService.remove(reComment3Id);
		clear();
		
		commentService.remove(reComment4Id);
		clear();
		
		Assertions.assertThat(commentService.findById(reComment1Id).isRemoved()).isTrue();
		Assertions.assertThat(commentService.findById(reComment2Id).isRemoved()).isTrue();
		Assertions.assertThat(commentService.findById(reComment3Id).isRemoved()).isTrue();
		Assertions.assertThat(commentService.findById(reComment4Id).isRemoved()).isTrue();
		clear();
		
		
		//when
		commentService.remove(commentId);
		clear();
		
		
		//then
		LongStream.rangeClosed(commentId, reComment4Id).forEach(id ->
		
				assertThat(assertThrows(Exception.class, () ->
				
					commentService.findById(id)).getMessage()).isEqualTo("댓글이 없습니다.")
		);
	}
	
	/**
	 * 대댓글을 삭제하는 경우
	 * 부모 댓글이 삭제되지 않은 경우
	 * 내용만 삭제, DB에서는 삭제x
	 */
//	@Test
	public void 대댓글삭제_부모댓글이_남아있는_경우() throws Exception {
		// given
		Long commentId = saveComment();
		Long reComment1Id = saveReComment(commentId);
		
		// when
		commentService.remove(reComment1Id);
		clear();
		
		//then
        Assertions.assertThat(commentService.findById(commentId)).isNotNull();
        Assertions.assertThat(commentService.findById(reComment1Id)).isNotNull();
        Assertions.assertThat(commentService.findById(commentId).isRemoved()).isFalse();
        Assertions.assertThat(commentService.findById(reComment1Id).isRemoved()).isTrue();
	}
	
	/**
	 * 대댓글을 삭제하는 경우
	 * 부모 댓글이 삭제되어있고, 대댓글도 모두 삭제된 경우
	 * 부모 댓글을 포함한 모든 대댓글을 DB에서 일괄 삭제
	 * 화면상에서도 지움
	 */
//	@Test
	public void 대댓글삭제_부모댓글이_삭제된_경우_모든_대댓글이_삭제된_경우() throws Exception {
		// given
		Long commentId = saveComment();
		Long reComment1Id = saveReComment(commentId);
		Long reComment2Id = saveReComment(commentId);
		Long reComment3Id = saveReComment(commentId);
		
		commentService.remove(reComment2Id);
		clear();
		commentService.remove(commentId);
		clear();
		commentService.remove(reComment3Id);
		clear();
		
		Assertions.assertThat(commentService.findById(commentId)).isNotNull();
		Assertions.assertThat(commentService.findById(commentId).getChildList().size()).isEqualTo(3);
        
        // when
		commentService.remove(reComment1Id);
		
		// then
		LongStream.rangeClosed(commentId, reComment3Id).forEach(id ->
		
					assertThat(assertThrows(Exception.class, () ->
					
						commentService.findById(id)).getMessage()).isEqualTo("댓글이 없습니다.")
		);
	}
	
	/**
	 * 대댓글을 삭제하는 경우
	 * 부모 댓글이 삭제되어있고 다른 대댓글이 아직 삭제되지 않고 남아있는 경우
	 * 해당 대댓글만 삭제
	 * 그러나 DB에서는 삭제되지 않음
	 * 화면상에는 "삭제된 댓글입니다." 라고 표시
	 */
//	@Test
	public void 대댓글삭제_부모댓글이_삭제된_경우_다른_대댓글이_남아있는_경우() throws Exception {
		// given
		Long commentId = saveComment();
		Long reComment1Id = saveReComment(commentId);
		Long reComment2Id = saveReComment(commentId);
		Long reComment3Id = saveReComment(commentId);
		
		commentService.remove(reComment3Id);
		commentService.remove(commentId);
		clear();
		
		Assertions.assertThat(commentService.findById(commentId)).isNotNull();
		Assertions.assertThat(commentService.findById(commentId).getChildList().size()).isEqualTo(3);
		
		// when
		commentService.remove(reComment2Id);
		Assertions.assertThat(commentService.findById(commentId)).isNotNull();
		
		// then
		Assertions.assertThat(commentService.findById(reComment2Id)).isNotNull();
		Assertions.assertThat(commentService.findById(reComment2Id).isRemoved()).isTrue();
		Assertions.assertThat(commentService.findById(reComment1Id).getId()).isNotNull();
		Assertions.assertThat(commentService.findById(reComment3Id).getId()).isNotNull();
		Assertions.assertThat(commentService.findById(commentId).getId()).isNotNull();
	}
}