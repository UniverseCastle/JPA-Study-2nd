package com.jpa2.domain.post.service;

import javax.annotation.processing.FilerException;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jpa2.domain.member.exception.MemberException;
import com.jpa2.domain.member.exception.MemberExceptionType;
import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.domain.post.Post;
import com.jpa2.domain.post.cond.PostSearchCondition;
import com.jpa2.domain.post.dto.PostInfoDto;
import com.jpa2.domain.post.dto.PostPagingDto;
import com.jpa2.domain.post.dto.PostSaveDto;
import com.jpa2.domain.post.dto.PostUpdateDto;
import com.jpa2.domain.post.exception.PostException;
import com.jpa2.domain.post.exception.PostExceptionType;
import com.jpa2.domain.post.repository.PostRepository;
import com.jpa2.global.file.exception.FileException;
import com.jpa2.global.file.exception.FileExceptionType;
import com.jpa2.global.file.service.FileService;
import com.jpa2.global.util.security.SecurityUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final MemberRepository memberRepository;
	private final FileService fileService;
	
	
	@Override
	public void save(PostSaveDto postSaveDto) throws FilerException {
		Post post = postSaveDto.toEntity();
		
		post.confirmWriter(memberRepository.findByUsername(SecurityUtil.getLoginUsername()) // 로그인한 사용자를 작성자로 매핑
				.orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER)));
		
		postSaveDto.uploadFile().ifPresent(
				file -> {
					try {
						post.updateFilePath(fileService.save(file));
					} catch (FilerException e) {
						// 파일 저장 에러!
						throw new FileException(FileExceptionType.FILE_CAN_NOT_SAVE);
					}
				}
		);
		
		postRepository.save(post);
	}
	
	@Override
	public void update(Long id, PostUpdateDto postUpdateDto) {
		Post post = postRepository.findById(id).orElseThrow(() ->
				new PostException(PostExceptionType.POST_NOT_FOUND));
		
		checkAuthority(post, PostExceptionType.NOT_AUTHORITY_UPDATE_POST);
		
		postUpdateDto.title().ifPresent(post::updateTitle);
		postUpdateDto.content().ifPresent(post::updateContent);
		
		if (post.getFilePath() != null) {
			fileService.delete(post.getFilePath()); // 기존에 올린 파일 지우기
		}
		
		postUpdateDto.uploadFile().ifPresentOrElse(
				MultipartFile -> {
					try {
						post.updateFilePath(fileService.save(MultipartFile));
					} catch (FilerException e) {
						e.printStackTrace();
					}
				}, () -> post.updateFilePath(null)
		);
	}
	
	@Override
	public void delete(Long id) {
		Post post = postRepository.findById(id).orElseThrow(() ->
				new PostException(PostExceptionType.POST_NOT_FOUND));
		
		checkAuthority(post, PostExceptionType.NOT_AUTHORITY_DELETE_POST);
		
		if (post.getFilePath() != null) {
			fileService.delete(post.getFilePath()); // 기존에 올린 파일 지우기
		}
		postRepository.delete(post);
	}
	
	@Override
	public PostInfoDto getPostInfo(Long id) {
		return null;
	}
	
	@Override
	public PostPagingDto getPostList(Pageable pageable, PostSearchCondition postSearchCondition) {
		return null;
	}
	
	private void checkAuthority(Post post, PostExceptionType postExceptionType) {
		if(!post.getWriter().getUsername().equals(SecurityUtil.getLoginUsername()))
			throw new PostException(postExceptionType);
	}
}