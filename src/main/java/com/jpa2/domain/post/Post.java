package com.jpa2.domain.post;

import java.util.ArrayList;
import java.util.List;

import com.jpa2.domain.BaseTimeEntity;
import com.jpa2.domain.comment.Comment;
import com.jpa2.domain.member.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "POST")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "writer_id")
	private Member writer;
	
	@Column(length = 40, nullable = false)
	private String title;
	
	@Lob
	@Column(nullable = false)
	private String content;
	
	@Column(nullable = true)
	private String filePath;
	
	@Builder
	public Post(String title, String content) {
		this.title = title;
		this.content = content;
	}
	
	//== 게시글을 삭제하면 달려있는 댓글 모두 삭제 ==//
	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Comment> commentList = new ArrayList<>();
	
	//== 연관관계 편의 메서드 ==//
	public void confirmWriter(Member writer) {
		//writer는 변경이 불가능하므로 이렇게만 해주어도 될듯
		this.writer = writer;
		writer.addPost(this);
	}
	
	public void addComment(Comment comment){
		//comment의 Post 설정은 comment에서 함
		commentList.add(comment);
	}

	//== 내용 수정 ==//
	public void updateTitle(String title) {
    	this.title = title;
		}

	public void updateContent(String content) {
		this.content = content;
	}

	public void updateFilePath(String filePath) {
		this.filePath = filePath;
	}
}