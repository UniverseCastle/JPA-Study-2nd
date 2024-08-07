package com.jpa2.domain.comment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jpa2.domain.comment.Comment;
import com.jpa2.domain.comment.dto.CommentSaveDto;
import com.jpa2.domain.comment.dto.CommentUpdateDto;
import com.jpa2.domain.comment.exception.CommentException;
import com.jpa2.domain.comment.exception.CommentExceptionType;
import com.jpa2.domain.comment.repository.CommentRepository;
import com.jpa2.domain.member.exception.MemberException;
import com.jpa2.domain.member.exception.MemberExceptionType;
import com.jpa2.domain.member.repository.MemberRepository;
import com.jpa2.domain.post.exception.PostException;
import com.jpa2.domain.post.exception.PostExceptionType;
import com.jpa2.domain.post.repository.PostRepository;
import com.jpa2.global.util.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final MemberRepository memberRepository;
	private final PostRepository postRepository;

	
	@Override
	public void save(Long postId, CommentSaveDto commentSaveDto) {
		Comment comment = commentSaveDto.toEntity();
		
		comment.confirmWriter(memberRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER)));
		comment.confirmPost(postRepository.findById(postId).orElseThrow(() -> new PostException(PostExceptionType.POST_NOT_FOUND)));
		
		commentRepository.save(comment);
	}
	
	@Override
	public void saveReComment(Long postId, Long parentId, CommentSaveDto commentSaveDto) {
		Comment comment = commentSaveDto.toEntity();
		
		comment.confirmWriter(memberRepository.findByUsername(SecurityUtil.getLoginUsername()).orElseThrow(() -> new MemberException(MemberExceptionType.NOT_FOUND_MEMBER)));
		comment.confirmPost(postRepository.findById(postId).orElseThrow(() -> new PostException(PostExceptionType.POST_NOT_FOUND)));
		comment.confirmParent(commentRepository.findById(parentId).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT)));
		
		commentRepository.save(comment);
	}
	
	@Override
	public void update(Long id, CommentUpdateDto commentUpdateDto) {
		Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT));
		
		if (!comment.getWriter().getUsername().equals(SecurityUtil.getLoginUsername())) {
			throw new CommentException(CommentExceptionType.NOT_AUTHORITY_UPDATE_COMMENT);
		}
		commentUpdateDto.content().ifPresent(comment::updateContent);
	}

	@Override
	public void remove(Long id) throws CommentException {
		Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentException(CommentExceptionType.NOT_FOUND_COMMENT));
		
		if (!comment.getWriter().getUsername().equals(SecurityUtil.getLoginUsername())) {
			throw new CommentException(CommentExceptionType.NOT_AUTHORITY_DELETE_COMMENT);
		}
		
		comment.remove();
		
		List<Comment> removableCommentList = comment.findRemovableList();
		
		commentRepository.deleteAll(removableCommentList);
	}
}