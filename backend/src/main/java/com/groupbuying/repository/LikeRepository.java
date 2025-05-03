package com.groupbuying.repository;

import com.groupbuying.entity.Like;
import com.groupbuying.entity.Post;
import com.groupbuying.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Like findByUserAndPost(User user, Post post);
    boolean existsByUserAndPost(User user, Post post);
} 