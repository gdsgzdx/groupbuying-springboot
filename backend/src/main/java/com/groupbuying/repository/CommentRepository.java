package com.groupbuying.repository;

import com.groupbuying.entity.Comment;
import com.groupbuying.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndParentIsNullOrderByCreatedAtDesc(Post post);
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
} 