package com.jpa2.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;

import com.jpa2.domain.comment.Comment;
import com.jpa2.domain.comment.dto.CommentSaveDto;
import com.jpa2.domain.comment.exception.CommentException;
import com.jpa2.domain.comment.exception.CommentExceptionType;
import com.jpa2.domain.comment.repository.CommentRepository;
import com.jpa2.domain.member.Role;
import com.jpa2.domain.member.dto.MemberSignUpDto;
import com.jpa2.domain.member.serivce.MemberService;
import com.jpa2.domain.post.Post;
import com.jpa2.domain.post.dto.PostSaveDto;
import com.jpa2.domain.post.repository.PostRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class CommentServiceTest {

	@Autowired
	CommentService commentService;
	
	@Autowired
	CommentRepository commentRepository;
	
	@Autowired
	PostRepository postRepository;
	
	@Autowired
	MemberService memberService;
	
	@Autowired
	EntityManager em;
	
	private void clear() {
		em.flush();
		em.clear();
	}
	
	@BeforeEach
    private void signUpAndSetAuthentication() throws Exception {
        memberService.signUp(new MemberSignUpDto("USERNAME","PASSWORD","name","nickName",22));
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        User.builder()
                                .username("USERNAME")
                                .password("PASSWORD")
                                .roles(Role.USER.toString())
                                .build(),
                        null)
        );
        SecurityContextHolder.setContext(emptyContext);
        clear();
    }

    private void anotherSignUpAndSetAuthentication() throws Exception {
        memberService.signUp(new MemberSignUpDto("USERNAME1","PASSWORD123","name","nickName",22));
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        User.builder()
                                .username("USERNAME1")
                                .password("PASSWORD123")
                                .roles(Role.USER.toString())
                                .build(),
                        null)
        );
        SecurityContextHolder.setContext(emptyContext);
        clear();
    }
    
    private Long savePost(){
        String title = "제목";
        String content = "내용";
        PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());

        //when
        Post save = postRepository.save(postSaveDto.toEntity());
        clear();
        return save.getId();
    }
	
	private Long saveComment() {
		CommentSaveDto commentSaveDto = new CommentSaveDto("댓글");
        commentService.save(savePost(),commentSaveDto);
        clear();

        List<Comment> resultList = em.createQuery("select c from Comment c order by c.createdDate desc ", Comment.class).getResultList();
        return resultList.get(0).getId();
	}
	
	private Long saveReComment(Long parentId) {
		CommentSaveDto commentSaveDto = new CommentSaveDto("대댓글");
        commentService.saveReComment(savePost(),parentId,commentSaveDto);
        clear();

        List<Comment> resultList = em.createQuery("select c from Comment c order by c.createdDate desc ", Comment.class).getResultList();
        return resultList.get(0).getId();
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
		
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() ->
		
				new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)
				
		).getChildList().size()).isEqualTo(4);
		
		// when
		commentService.remove(commentId); // 댓글 삭제 메서드
		clear();
		
		// then
		Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)); // 댓글 불러오기
		
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
		Assertions.assertThat(commentRepository.findAll().size()).isSameAs(0);
		assertThat(assertThrows(CommentException.class, () ->
		
				commentRepository.findById(commentId).orElseThrow(()
						
						-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).getExceptionType()
				
		).isEqualTo(CommentExceptionType.NOT_FOUND_COMMENT);
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
		
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() ->
		
					new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()
				
		).isEqualTo(4);
		clear();
		
		commentService.remove(reComment1Id);
		clear();
		
		commentService.remove(reComment2Id);
		clear();
		
		commentService.remove(reComment3Id);
		clear();
		
		commentService.remove(reComment4Id);
		clear();
		
		Assertions.assertThat(commentRepository.findById(reComment1Id).orElseThrow(() ->
				new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
		Assertions.assertThat(commentRepository.findById(reComment2Id).orElseThrow(() ->
				new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
		Assertions.assertThat(commentRepository.findById(reComment3Id).orElseThrow(() ->
				new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
		Assertions.assertThat(commentRepository.findById(reComment4Id).orElseThrow(() ->
				new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
		clear();
		
		
		//when
		commentService.remove(commentId);
		clear();
		
		
		//then
		LongStream.rangeClosed(commentId, reComment4Id).forEach(id ->
		
				assertThat(assertThrows(CommentException.class, () -> commentRepository.findById(id).orElseThrow(() ->
				
						new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).getExceptionType()).isEqualTo(CommentExceptionType.NOT_FOUND_COMMENT)
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
        Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        Assertions.assertThat(commentRepository.findById(reComment1Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isFalse();
        Assertions.assertThat(commentRepository.findById(reComment1Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
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
		
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(3);
        
        // when
		commentService.remove(reComment1Id);
		
		// then
		LongStream.rangeClosed(commentId, reComment3Id).forEach(id ->
		
					assertThat(assertThrows(CommentException.class, () ->
					
						commentRepository.findById(id).orElseThrow(() -> 
						
							new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).getExceptionType()).isEqualTo(CommentExceptionType.NOT_FOUND_COMMENT)
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
		
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(3);
		
		// when
		commentService.remove(reComment2Id);
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
		
		// then
		Assertions.assertThat(commentRepository.findById(reComment2Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
		Assertions.assertThat(commentRepository.findById(reComment2Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
		Assertions.assertThat(commentRepository.findById(reComment1Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
		Assertions.assertThat(commentRepository.findById(reComment3Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
	}
}