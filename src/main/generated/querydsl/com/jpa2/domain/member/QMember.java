package com.jpa2.domain.member;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -1310826354L;

    public static final QMember member = new QMember("member1");

    public final com.jpa2.domain.QBaseTimeEntity _super = new com.jpa2.domain.QBaseTimeEntity(this);

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final ListPath<com.jpa2.domain.comment.Comment, com.jpa2.domain.comment.QComment> commentList = this.<com.jpa2.domain.comment.Comment, com.jpa2.domain.comment.QComment>createList("commentList", com.jpa2.domain.comment.Comment.class, com.jpa2.domain.comment.QComment.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final StringPath name = createString("name");

    public final StringPath nickName = createString("nickName");

    public final StringPath password = createString("password");

    public final ListPath<com.jpa2.domain.post.Post, com.jpa2.domain.post.QPost> postList = this.<com.jpa2.domain.post.Post, com.jpa2.domain.post.QPost>createList("postList", com.jpa2.domain.post.Post.class, com.jpa2.domain.post.QPost.class, PathInits.DIRECT2);

    public final StringPath refreshToken = createString("refreshToken");

    public final EnumPath<Role> role = createEnum("role", Role.class);

    public final StringPath username = createString("username");

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

