package com.jpa2.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;

import com.jpa2.domain.comment.Comment;
import com.jpa2.domain.comment.dto.CommentInfoDto;
import com.jpa2.domain.comment.repository.CommentRepository;
import com.jpa2.domain.member.Member;
import com.jpa2.domain.member.Role;
import com.jpa2.domain.member.dto.MemberSignUpDto;
import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.domain.member.serivce.MemberService;
import com.jpa2.domain.post.Post;
import com.jpa2.domain.post.dto.PostInfoDto;
import com.jpa2.domain.post.dto.PostSaveDto;
import com.jpa2.domain.post.dto.PostUpdateDto;
import com.jpa2.domain.post.exception.PostException;
import com.jpa2.domain.post.repository.PostRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class PostServiceImplTest {
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private PostService postService;
	
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private MemberRepository memberRepository;
	
	@Autowired
	private PostRepository postRepository;
	
	@Autowired
	private CommentRepository commentRepository;
	
	private static final String USERNAME = "username";
	private static final String PASSWORD = "PASSWORD123@@@";
	
	private String title = "제목";
	private String content = "내용";
	
	private void clear() {
		em.flush();
		em.clear();
	}
	
	private void deleteFile(String filePath) {
		File files = new File(filePath);
		files.delete();
	}
	
	private MockMultipartFile getMockUploadFile() throws IOException {
		return new MockMultipartFile("file", "file.jfif", "image/jfif", new FileInputStream("C:/Users/uc/Desktop/uc/Develop/images/thumb/bh.jfif"));
	}
	
	public Post findPost() {
		return em.createQuery("select p from Post p", Post.class).getSingleResult();
	}
	
	@BeforeEach
	private void signUpAndSetAuthentication() throws Exception {
		memberService.signUp(new MemberSignUpDto(USERNAME, PASSWORD, "name", "nickName", 20));
		SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
		emptyContext.setAuthentication( // 인증 정보 설정
				new UsernamePasswordAuthenticationToken(
						User.builder()
						.username(USERNAME)
						.password(PASSWORD)
						.roles(Role.USER.toString())
						.build(),
					null)
		);
		// SecurityContextHolder에 생성한 SecurityContext 설정
		SecurityContextHolder.setContext(emptyContext);
		
		clear();
	}
	
	
//	업로드 파일 없이 포스트 게시
//	@Test
	public void 포스트_저장_성공_업로드_파일_없음() throws Exception {
		// given
		String title = "제목";
		String content = "내용";
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());
		
		// when
		postService.save(postSaveDto);
		clear();
		
		// then
		Post findPost = em.createQuery("select p from Post p", Post.class).getSingleResult();
		Post post = em.find(Post.class, findPost.getId());
		
		assertThat(post.getContent()).isEqualTo(content);
		assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
		assertThat(post.getFilePath()).isNull();
	}
	
//	업로드 파일과 함께 포스트 게시
//	@Test
	public void 포스트_저장_성공_업로드_파일_있음() throws Exception {
		// given
		String title = "제목";
		String content = "내용";
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.ofNullable(getMockUploadFile()));
		
		// when
		postService.save(postSaveDto);
		clear();
		
		// then
		Post findPost = em.createQuery("select p from Post p", Post.class).getSingleResult();
		Post post = em.find(Post.class, findPost.getId());
		
		assertThat(post.getContent()).isEqualTo(content);
		assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
		assertThat(post.getFilePath()).isNotNull();
		
		deleteFile(post.getFilePath()); // 올린 파일 삭제
	}
	
//	제목, 내용이 없어서 저장 실패
//	@Test
	public void 포스트_저장_실패_제목이나_내용이_없음() throws Exception {
		String title = "제목";
		String content = "내용";
		
		PostSaveDto postSaveDto = new PostSaveDto(null, content, Optional.empty());
		PostSaveDto postSaveDto2 = new PostSaveDto(title, null, Optional.empty());
		
		// when, then
		assertThrows(Exception.class, () -> postService.save(postSaveDto)); // save 메서드 실행 시 예외 검증
		assertThrows(Exception.class, () -> postService.save(postSaveDto2));
	}
	
//	업데이트, 업로드파일x
//	@Test
	public void 포스트_업데이트_성공_업로드파일_없음TO없음() throws Exception {
		// given
		String title = "제목";
		String content = "내용";
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());
		postService.save(postSaveDto);
		clear();
		
		// when
		Post findPost = em.createQuery("select p from Post p", Post.class).getSingleResult();
		PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"), Optional.ofNullable("바꾼내용"), Optional.empty());
		postService.update(findPost.getId(), postUpdateDto);
		clear();
		
		// then
		Post post = em.find(Post.class, findPost.getId());
		
		assertThat(post.getContent()).isEqualTo("바꾼내용");
		assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
		assertThat(post.getFilePath()).isNull();
	}
	
//	업데이트, 업로드파일x->o
//	@Test
	public void 포스트_업데이트_성공_업로드파일_없음TO있음() throws Exception {
		// given
		String title = "제목";
		String content = "내용";
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());
		postService.save(postSaveDto);
		clear();
		
		// when
		Post findPost = em.createQuery("select p from Post p", Post.class).getSingleResult();
		PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"), Optional.ofNullable("바꾼내용"), Optional.ofNullable(getMockUploadFile()));
		postService.update(findPost.getId(), postUpdateDto);
		clear();
		
		// then
		Post post = em.find(Post.class, findPost.getId());
		
		assertThat(post.getContent()).isEqualTo("바꾼내용");
		assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
		assertThat(post.getFilePath()).isNotNull();
		
		deleteFile(post.getFilePath()); // 올린 파일 삭제
	}
	
//	업데이트, 업로드파일o->x
//	@Test
	public void 포스트_업데이트_성공_업로드파일_있음TO없음() throws Exception {
		// given
		String title = "제목";
		String content = "내용";
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.ofNullable(getMockUploadFile()));
		postService.save(postSaveDto);
		
		Post findPost = em.createQuery("select p from Post p", Post.class).getSingleResult();
		assertThat(findPost.getFilePath()).isNotNull();
		clear();
		
		// when
		PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"), Optional.ofNullable("바꾼내용"), Optional.empty());
		postService.update(findPost.getId(), postUpdateDto);
		clear();
		
		// then
		Post post = em.find(Post.class, findPost.getId());
		
		assertThat(post.getContent()).isEqualTo("바꾼내용");
		assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
		assertThat(post.getFilePath()).isNull();
	}
	
//	업데이트 업로드파일o->o
//	@Test
	public void 포스트_업데이트_성공_업로드파일_있음TO있음() throws Exception {
		// given
		String title = "제목";
		String content = "내용";
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.ofNullable(getMockUploadFile()));
		postService.save(postSaveDto);
		
		Post findPost = em.createQuery("select p from Post p", Post.class).getSingleResult();
		Post post = em.find(Post.class, findPost.getId());
		String filePath = post.getFilePath();
		clear();
		
		// when
		PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"), Optional.ofNullable("바꾼내용"), Optional.ofNullable(getMockUploadFile()));
		postService.update(findPost.getId(), postUpdateDto);
		clear();
		
		// then
		post = em.find(Post.class, findPost.getId());
		
		assertThat(post.getContent()).isEqualTo("바꾼내용");
		assertThat(post.getWriter().getUsername()).isEqualTo(USERNAME);
		assertThat(post.getFilePath()).isNotEqualTo(filePath);
		
		deleteFile(post.getFilePath()); // 올린 파일 삭제
	}
	
	private void setAnotherAuthentication() throws Exception { // 권한 테스트를 위한 새로운 권한 상태 생성
		memberService.signUp(new MemberSignUpDto(USERNAME + "123", PASSWORD, "name", "nickName", 20));
		SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
		emptyContext.setAuthentication(
				new UsernamePasswordAuthenticationToken(
						User.builder()
						.username(USERNAME + "123")
						.password(PASSWORD)
						.roles(Role.USER.toString())
						.build(),
					null
				)
		);
		SecurityContextHolder.setContext(emptyContext);
		
		clear();
	}
	
//	업데이트 권한이 없음
//	@Test
	public void 포스트_업데이트_실패_권한이없음() throws Exception {
		// given
		String title = "제목";
		String content = "내용";
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());
		postService.save(postSaveDto);
		clear();
		
		// when, then
		setAnotherAuthentication(); // 새로운 권한 (게시자와 다름)
		Post findPost = em.createQuery("select p from Post p", Post.class).getSingleResult();
		PostUpdateDto postUpdateDto = new PostUpdateDto(Optional.ofNullable("바꾼제목"), Optional.ofNullable("바꾼내용"), Optional.empty());
		
		assertThrows(PostException.class, () -> postService.update(findPost.getId(), postUpdateDto));
	}
	
//	포스트 삭제
//	@Test
	public void 포스트삭제_성공() throws Exception {
		// given
		String title = "제목";
		String content = "내용";
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());
		postService.save(postSaveDto);
		clear();
		
		// when
		Post findPost = em.createQuery("select p from Post p", Post.class).getSingleResult();
		postService.delete(findPost.getId());
		
		// then
		List<Post> findPosts = em.createQuery("select p from Post p", Post.class).getResultList();
		
		assertThat(findPosts.size()).isEqualTo(0);
	}
	
//	포스트 삭제 실패
//	@Test
	public void 포스트삭제_실패() throws Exception {
		// given
		String title = "제목";
		String content = "내용";
		PostSaveDto postSaveDto = new PostSaveDto(title, content, Optional.empty());
		postService.save(postSaveDto);
		clear();
		
		// when, then
		setAnotherAuthentication();
		Post findPost = em.createQuery("select p from Post p", Post.class).getSingleResult();
		
		assertThrows(PostException.class, () -> postService.delete(findPost.getId()));
	}
	
//	포스트 조회
//	@Test
	public void 포스트_조회() throws Exception {
		/**
		 * Member 생성
		 */
		Member member1 = memberRepository.save(Member.builder().username("username1").password("1234567890").name("이름1").nickName("별명1").role(Role.USER).age(20).build());
		Member member2 = memberRepository.save(Member.builder().username("username2").password("1234567890").name("이름2").nickName("별명2").role(Role.USER).age(21).build());
		Member member3 = memberRepository.save(Member.builder().username("username3").password("1234567890").name("이름3").nickName("별명3").role(Role.USER).age(22).build());
		Member member4 = memberRepository.save(Member.builder().username("username4").password("1234567890").name("이름4").nickName("별명4").role(Role.USER).age(23).build());
		Member member5 = memberRepository.save(Member.builder().username("username5").password("1234567890").name("이름5").nickName("별명5").role(Role.USER).age(24).build());
		
		Map<Integer, Long> memberIdMap = new HashMap<>();
		memberIdMap.put(1, member1.getId());
		memberIdMap.put(2, member2.getId());
		memberIdMap.put(3, member3.getId());
		memberIdMap.put(4, member4.getId());
		memberIdMap.put(5, member5.getId());
		
		/**
		 * Post 생성
		 */
		Post post = Post.builder().title("게시글").content("내용").build();
		post.confirmWriter(member1);
		postRepository.save(post);
		em.flush();
		
		/**
		 * Comment 생성
		 */
		final int COMMENT_COUNT = 10;
		
		for (int i=1; i<=COMMENT_COUNT; i++) {
			Comment comment = Comment.builder().content("댓글 + i").build();
			comment.confirmWriter(memberRepository.findById(memberIdMap.get(i % 3 + 1)).orElse(null));
			comment.confirmPost(post);
			
			commentRepository.save(comment);
		}
		
		/**
		 * ReComment 생성 (대댓글)
		 */
		final int COMMENT_PER_RECOMMENT_COUNT = 20;
		commentRepository.findAll().stream().forEach(comment -> {
			for (int i=1; i<=20; i++) {
				Comment recomment = Comment.builder().content("대댓글" + i).build();
				recomment.confirmWriter(memberRepository.findById(memberIdMap.get(i % 3 + 1)).orElse(null));
				recomment.confirmPost(comment.getPost());
				recomment.confirmParent(comment);
				
				commentRepository.save(recomment);
			}
		});
		
		clear();
		
		// when
		PostInfoDto postInfo = postService.getPostInfo(post.getId());
		
		// then
		assertThat(postInfo.getPostId()).isEqualTo(post.getId());
		assertThat(postInfo.getContent()).isEqualTo(post.getContent());
		assertThat(postInfo.getWriterDto().getUsername()).isEqualTo(post.getWriter().getUsername());
		
		int recommentCount = 0;
		for (CommentInfoDto commentInfoDto : postInfo.getCommentInfoDtoList()) {
			recommentCount += commentInfoDto.getReCommentListDtoList().size();
		}
		
		assertThat(postInfo.getCommentInfoDtoList().size()).isEqualTo(COMMENT_COUNT);
		assertThat(recommentCount).isEqualTo(COMMENT_PER_RECOMMENT_COUNT * COMMENT_COUNT);
	}
}