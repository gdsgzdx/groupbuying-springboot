package com.groupbuying.service;

import com.groupbuying.dto.PostDTO;
import com.groupbuying.dto.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PostService {
    PostDTO createPost(PostDTO postDTO);
    PostDTO updatePost(Long id, PostDTO postDTO);
    void deletePost(Long id);
    PostDTO getPost(Long id);
    Page<PostDTO> getAllPosts(Pageable pageable);
    Page<PostDTO> getUserPosts(Long userId, Pageable pageable);
    PostDTO addComment(Long postId, String content);
    void toggleLike(Long postId);
    List<CommentDTO> getReplies(Long commentId);
    PostDTO addReply(Long commentId, CommentDTO replyDTO);
} 