package com.jpa2.domain.post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jpa2.domain.post.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	/**
	 * @EntityGraph JPA에서 N+1 문제를 해결하기 위해 사용
	 * 페치조인을 간편하게 사용할 수 있도록 해주는 어노테이션
	 * JPQL: "select p from Post p join fetch p.writer w where p.id = :id"
	 * 
	 * 메서드가 호출될 때 Post 엔티티와 관련된
	 * writer 엔티티를 즉시 로드하도록 지시
	 * 즉, Post 조회할 때 Writer 필드도 함께 가져오기 위함
	 */
	@EntityGraph(attributePaths = {"writer"})
	Optional<Post> findWithWriterById(Long id);
}