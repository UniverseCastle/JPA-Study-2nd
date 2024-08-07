package com.jpa2.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpa2.domain.post.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}