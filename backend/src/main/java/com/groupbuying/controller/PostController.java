package com.groupbuying.controller;

import com.groupbuying.common.Result;
import com.groupbuying.dto.CommentDTO;
import com.groupbuying.dto.PostDTO;
import com.groupbuying.exception.BusinessException;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Result<PostDTO> createPost(@RequestBody PostDTO postDTO) {
        try {
            if (postDTO == null || postDTO.getContent() == null || postDTO.getContent().trim().isEmpty()) {
                return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "帖子内容不能为空");
            }
            return Result.success(postService.createPost(postDTO));
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建帖子失败", e);
            return Result.error(ErrorCode.SERVER_ERROR.getCode(), "创建帖子失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<PostDTO> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO) {
        try {
            return Result.success(postService.updatePost(id, postDTO));
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return Result.success();
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<PostDTO> getPost(@PathVariable Long id) {
        try {
            return Result.success(postService.getPost(id));
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }
    }

    @GetMapping("/all")
    public Result<Page<PostDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return Result.success(postService.getAllPosts(pageable));
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Result<Page<PostDTO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            return Result.success(postService.getUserPosts(userId, pageable));
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }
    }

    @PostMapping("/{postId}/comments")
    public Result<PostDTO> addComment(@PathVariable Long postId, @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "评论内容不能为空");
            }
            return Result.success(postService.addComment(postId, content));
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("添加评论失败", e);
            return Result.error(ErrorCode.SERVER_ERROR.getCode(), "添加评论失败：" + e.getMessage());
        }
    }

    @PostMapping("/{postId}/like")
    public Result<Void> toggleLike(@PathVariable Long postId) {
        try {
            postService.toggleLike(postId);
            return Result.success();
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }
    }

    @GetMapping("/comments/{commentId}/replies")
    public Result<List<CommentDTO>> getReplies(@PathVariable Long commentId) {
        try {
            return Result.success(postService.getReplies(commentId));
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        }
    }

    @PostMapping("/comments/{commentId}/reply")
    public Result<PostDTO> addReply(
            @PathVariable Long commentId,
            @RequestBody CommentDTO replyDTO) {
        try {
            if (replyDTO.getContent() == null || replyDTO.getContent().trim().isEmpty()) {
                return Result.error(ErrorCode.INVALID_PARAMETER.getCode(), "回复内容不能为空");
            }
            return Result.success(postService.addReply(commentId, replyDTO));
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("添加回复失败", e);
            return Result.error(ErrorCode.SERVER_ERROR.getCode(), "添加回复失败：" + e.getMessage());
        }
    }
} 