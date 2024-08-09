package com.jpa2.domain.comment.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpa2.domain.comment.Comment;
import com.jpa2.domain.comment.dto.CommentSaveDto;
import com.jpa2.domain.comment.exception.CommentException;
import com.jpa2.domain.comment.exception.CommentExceptionType;
import com.jpa2.domain.comment.repository.CommentRepository;
import com.jpa2.domain.comment.service.CommentService;
import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.Role;
import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.domain.member.serivce.MemberService;
import com.jpa2.domain.post.Post;
import com.jpa2.domain.post.dto.PostSaveDto;
import com.jpa2.domain.post.repository.PostRepository;
import com.jpa2.global.jwt.service.JwtService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CommentControllerTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	EntityManager em;
	
	@Autowired
	MemberService memberService;
	
	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	PostRepository postRepository;
	
	@Autowired
	CommentService commentService;
	
	@Autowired
	CommentRepository commentRepository;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	JwtService jwtService;
	
	final String USERNAME = "username1";
	
	private static Member member;
	
	@BeforeEach
	private void signUpAndSetAuthentication() throws Exception {
		member = memberRepository.save(Member.builder()
				.username(USERNAME)
				.password("1234567890")
				.name("USER1")
				.nickName("nickName1")
				.role(Role.USER)
				.age(20)
				.build());
		SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
		emptyContext.setAuthentication(
				new UsernamePasswordAuthenticationToken(
						User.builder()
								.username(USERNAME)
								.password("1234567890")
								.roles(Role.USER.toString())
								.build(),
						null)
		);
		SecurityContextHolder.setContext(emptyContext);
		clear();
	}
	
	private void clear() {
		em.flush();
		em.clear();
	}
	
	private String getAccessToken() {
		return jwtService.createAccessToken(USERNAME);
	}
	private String getNoAuthAccessToken() {
		return jwtService.createAccessToken(USERNAME + 12);
	}
	
	private Long savePost() {
		String title = "제목";
		String content = "내용";
		
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());
		
		// when
		Post save = postRepository.save(postSaveDto.toEntity());
		clear();
		
		return save.getId();
	}
	
	private Long saveComment() {
		CommentSaveDto commentSaveDto = new CommentSaveDto("댓글");
		commentService.save(savePost(), commentSaveDto);
		clear();
		
		List<Comment> resultList = em.createQuery("select c from Comment c order by c.createdDate desc", Comment.class).getResultList();
		
		return resultList.get(0).getId();
	}
	
	private Long saveReComment(Long parentId) {
		CommentSaveDto commentSaveDto = new CommentSaveDto("대댓글");
		commentService.saveReComment(savePost(), parentId, commentSaveDto);
		clear();
		
		List<Comment> resultList = em.createQuery("select c from Comment c order by createdDate desc", Comment.class).getResultList();
		
		return resultList.get(0).getId();
	}
	
	//== Test ==//
	
//	댓글 저장
//	@Test
	public void 댓글저장_성공() throws Exception {
		// given
		Long postId = savePost();
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("content", "comment");
		
		// when
		mockMvc.perform(
				post("/comment/" + postId)
					.header("Authorization", "Bearer " + getAccessToken())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.params(map)
		).andExpect(status().isCreated());
		
		// then
		List<Comment> resultList = em.createQuery("select c from Comment c order by c.createdDate desc", Comment.class).getResultList();
		
		assertThat(resultList.size()).isEqualTo(1);
	}
	
//	대댓글 저장
//	@Test
	public void 대댓글저장_성공() throws Exception {
		// given
		Long postId = savePost();
		Long parentId = saveComment();
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("content", "recomment");
		
		// when
		mockMvc.perform(
				post("/comment/" + postId + "/" + parentId)
					.header("Authorization", "Bearer " + getAccessToken())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.params(map)
		).andExpect(status().isCreated());
		
		// then
		List<Comment> resultList = em.createQuery("select c from Comment c order by c.createdDate desc", Comment.class).getResultList();
		
		assertThat(resultList.size()).isEqualTo(2);
	}
	
//	게시글 없을 때 댓글 실패
//	@Test
	public void 댓글저장_실패_게시물이_없음() throws Exception {
		// given
		Long postId = savePost();
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("content", "comment");
		
		// when, then
		mockMvc.perform(
				post("/comment/" + 1000000)
					.header("Authorization", "Bearer " + getAccessToken())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.params(map)
		).andExpect(status().isNotFound());
	}
	
//	게시글 없을 때 대댓글 실패
//	@Test
	public void 대댓글저장_실패_게시물이_없음() throws Exception {
		// given
		Long postId = savePost();
		Long parentId = saveComment();
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("content", "recomment");
		
		// when, then
		mockMvc.perform(
				post("/comment/" + 10000 + "/" + parentId)
					.header("Authorization", "Bearer " + getAccessToken())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.params(map)
		).andExpect(status().isNotFound());
	}
	
//	댓글 없을 때 대댓글 실패
//	@Test
	public void 대댓글저장_실패_댓글이_없음() throws Exception {
		// given
		Long postId = savePost();
		Long parentId = saveComment();
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("content", "recomment");
		
		// when, then
		mockMvc.perform(
				post("/comment/" + postId + 10000)
					.header("Authorization", "Bearer " + getAccessToken())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.params(map)
		).andExpect(status().isNotFound());
	}
	
//	업데이트
//	@Test
	public void 업데이트_성공() throws Exception {
		// given
		Long postId = savePost();
		Long commentId = saveComment();
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("content", "updateComment");
		
		// when
		mockMvc.perform(
				put("/comment/" + commentId)
					.header("Authorization", "Bearer " + getAccessToken())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.params(map)
		).andExpect(status().isOk());
		
		Comment comment = commentRepository.findById(commentId).orElse(null);
		
		assertThat(comment.getContent()).isEqualTo("updateComment");
	}
	
//	업데이트 권한 없음
//	@Test
	public void 업데이트_실패_권한이없음() throws Exception {
		// given
		Long postId = savePost();
		Long commentId = saveComment();
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("content", "updateComment");
		
		// when
		mockMvc.perform(
				put("/comment/" + commentId)
					.header("Authorization", "Bearer " + getNoAuthAccessToken())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.params(map)
		).andExpect(status().isForbidden());
		
		Comment comment = commentRepository.findById(commentId).orElse(null);
		
		assertThat(comment.getContent()).isEqualTo("댓글");
	}
	
//	@Test
	public void 댓글삭제_실패_권한이_없음() throws Exception {
		// given
		Long postId = savePost();
		Long commentId = saveComment();
		
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("content", "updateComment");
		
		// when
		mockMvc.perform(
				delete("/comment/" + commentId)
					.header("Authorization", "Bearer " + getNoAuthAccessToken())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.params(map)
		).andExpect(status().isForbidden());
		
		Comment comment = commentRepository.findById(commentId).orElse(null);
		
		assertThat(comment.getContent()).isEqualTo("댓글");
	}
	
	/**
	 * 댓글을 삭제하는 경우
	 * 대댓글이 남아있는 경우
	 * DB와 화면에서는 지워지지 않고 "삭제된 댓글입니다." 라고 표시
	 */
//	@Test
	public void 댓글삭제_대댓글이_남아있는_경우() throws Exception {
		// given
		Long commentId = saveComment();
		saveReComment(commentId);
		saveReComment(commentId);
		saveReComment(commentId);
		saveReComment(commentId);
		
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(4);
		
		// when
		mockMvc.perform(
				delete("/comment/" + commentId)
					.header("Authorization", "Bearer " + getAccessToken())
		).andExpect(status().isOk());
		
		// then
		Comment findComment = commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT));
		
		assertThat(findComment).isNotNull();
		assertThat(findComment.isRemoved()).isTrue();
		assertThat(findComment.getChildList().size()).isEqualTo(4);
	}
	
	/**
	 * 댓글을 삭제하는 경우
	 * 대댓글이 아예 존재하지 않는 경우: 곧바로 DB에서 삭제
	 */
//	@Test
	public void 댓글삭제_대댓글이_없는_경우() throws Exception {
		// given
		Long commentId = saveComment();
		
		// when
		mockMvc.perform(
				delete("/comment/" + commentId)
					.header("Authorization", "Bearer " + getAccessToken())
		).andExpect(status().isOk());
		clear();
		
		// then
		Assertions.assertThat(commentRepository.findAll().size()).isSameAs(0);
		assertThat(assertThrows(CommentException.class, () -> commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).getExceptionType()).isEqualTo(CommentExceptionType.NOT_FOUND_COMMENT);
	}
	
	/**
	 * 댓글을 삭제하는 경우
	 * 대댓글이 존재하나 모두 삭제된 경우
	 * 댓글과 달려있는 대댓글 모두 DB에서 일괄 삭제
	 * 화면상에도 표시되지 않음
	 */
//	@Test
	public void 댓글삭제_대댓글이_존재하나_모두_삭제된_대댓글인_경우() throws Exception {
		// given
		Long commentId = saveComment();
		Long reComment1Id = saveReComment(commentId);
		Long reComment2Id = saveReComment(commentId);
		Long reComment3Id = saveReComment(commentId);
		Long reComment4Id = saveReComment(commentId);
		
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(4);
		clear();
		
		commentService.remove(reComment1Id);
		clear();
		
		commentService.remove(reComment2Id);
		clear();
		
		commentService.remove(reComment3Id);
		clear();
		
		commentService.remove(reComment4Id);
		clear();
		
		Assertions.assertThat(commentRepository.findById(reComment1Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
		Assertions.assertThat(commentRepository.findById(reComment2Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
		Assertions.assertThat(commentRepository.findById(reComment3Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
		Assertions.assertThat(commentRepository.findById(reComment4Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
		clear();
		
		// when
		mockMvc.perform(
				delete("/comment/" + commentId)
					.header("Authorization", "Bearer " + getAccessToken())
		).andExpect(status().isOk());
		clear();
		
		// then
		LongStream.range(commentId, reComment4Id).forEach(id ->
				assertThat(assertThrows(CommentException.class, () ->
						commentRepository.findById(id).orElseThrow(() ->
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
		mockMvc.perform(
				delete("/comment/" + reComment1Id)
					.header("Authorization", "Bearer " + getAccessToken())
		).andExpect(status().isOk());
		clear();
		
		// then
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
		Assertions.assertThat(commentRepository.findById(reComment1Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isFalse();
		Assertions.assertThat(commentRepository.findById(reComment1Id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
	}
	
	/**
	 * 대댓글을 삭제하는 경우
	 * 부모 댓글이 삭제되어있고 대댓글들도 모두 삭제된 경우
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
		
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(3);
		
		// when
		mockMvc.perform(
				delete("/comment/" + reComment1Id)
					.header("Authorization", "Bearer " + getAccessToken())
		).andExpect(status().isOk());
		
		// then
		LongStream.range(commentId, reComment3Id).forEach(id ->
				assertThat(assertThrows(CommentException.class, () ->
						commentRepository.findById(id).orElseThrow(() ->
								new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).getExceptionType()).isEqualTo(CommentExceptionType.NOT_FOUND_COMMENT)
		);
	}
	
	/**
	 * 대댓글을 삭제하는 경우
	 * 부모 댓글이 삭제되어있고 다른 대댓글이 아직 삭제되지 않고 남아있는 경우
	 * 해당 대댓글만 삭제, 그러나 DB에서 삭제되지는 않고
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
		
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getChildList().size()).isEqualTo(3);
		
		// when
		mockMvc.perform(
				delete("/comment/" + reComment2Id)
					.header("Authorization", "Bearer " + getAccessToken())
		).andExpect(status().isOk());
		
		Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
		
		// then
		Assertions.assertThat(commentRepository.findById(reComment2Id).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT))).isNotNull();
        Assertions.assertThat(commentRepository.findById(reComment2Id).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).isRemoved()).isTrue();
        Assertions.assertThat(commentRepository.findById(reComment1Id).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
        Assertions.assertThat(commentRepository.findById(reComment3Id).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
        Assertions.assertThat(commentRepository.findById(commentId).orElseThrow(()-> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)).getId()).isNotNull();
	}
}