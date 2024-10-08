package com.jpa2.domain.comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jpa2.domain.BaseTimeEntity;
import com.jpa2.domain.member.Member;
import com.jpa2.domain.post.Post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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
@Table(name = "COMMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {
	
	@Id
	@GeneratedValue
    @Column(name = "comment_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private Member writer;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Lob
    @Column(nullable = false)
    private String content;

    private boolean isRemoved= false;

    
    //== 부모 댓글을 삭제해도 자식 댓글은 남아있음 ==//
    @OneToMany(mappedBy = "parent")
    private List<Comment> childList = new ArrayList<>();

    
    //== 연관관계 편의 메서드 ==//
    public void confirmWriter(Member writer) {
        this.writer = writer;
        writer.addComment(this);
    }

    public void confirmPost(Post post) {
        this.post = post;
        post.addComment(this);
    }

    public void confirmParent(Comment parent){
        this.parent = parent;
        parent.addChild(this);
    }

    public void addChild(Comment child){
        childList.add(child);
    }
    

    //== 수정 ==//
    public void updateContent(String content) {
        this.content = content;
    }
    //== 삭제 ==//
    public void remove() {
        this.isRemoved = true;
    }
    

    @Builder
    public Comment( Member writer, Post post, Comment parent, String content) {
        this.writer = writer;
        this.post = post;
        this.parent = parent;
        this.content = content;
        this.isRemoved = false;
    }

	//== 비즈니스 로직 ==//
	public List<Comment> findRemovableList() { // 댓글 또는 대댓글에서 삭제 가능한 댓글 리스트를 찾는 메서드
	    List<Comment> result = new ArrayList<>();
	    Optional.ofNullable(this.parent).ifPresentOrElse(
	
				parentComment ->{ // 대댓글인 경우 (부모가 존재하는 경우)
					if(parentComment.isRemoved() && parentComment.isAllChildRemoved()){ // 모든 자식 댓글이 삭제된 경우
						result.addAll(parentComment.getChildList()); // 모든 자식 댓글 추가
						result.add(parentComment); // 부모 댓글 추가
					}
				},
	
				() -> { //댓글인 경우 (부모 댓글이 없는 경우)
					if (isAllChildRemoved()) {
						result.add(this); // 현재 댓글 추가
						result.addAll(this.getChildList()); // 현재 자식 댓글 추가
					}
				}
	    );
	    return result; // 삭제 가능한 댓글 리스트 반환
	}


	// 모든 자식 댓글이 삭제되었는지 판단
	private boolean isAllChildRemoved() {
		return getChildList().stream()
				.map(Comment::isRemoved) // 지워졌는지 여부로 바꾼다
				.filter(isRemove -> !isRemove) // 지워졌으면 true, 안지워졌으면 false이다. 따라서 filter에 걸러지는 것은 false인 녀석들이고, 있다면 false를 없다면 orElse를 통해 true를 반환한다.
				.findAny() // 지워지지 않은게 하나라도 있다면 false를 반환
				.orElse(true); // 모두 지워졌다면 true를 반환
	}
}