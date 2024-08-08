package com.jpa2.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jpa2.domain.post.Post;
import com.jpa2.domain.post.cond.PostSearchCondition;

public interface CustomPostRepository {
	
	Page<Post> search(PostSearchCondition postSearchCondition, Pageable pageable);
}