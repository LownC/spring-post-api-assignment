package com.back.domain.postComment.repository;

import com.back.domain.postComment.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Integer> {
}
