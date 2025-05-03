package com.groupbuying.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private Long userId;
    private String username;
    private String userAvatar;
    private Long parentId;  // 父评论ID，如果是顶级评论则为null
    private List<CommentDTO> replies;  // 子评论列表
    private LocalDateTime createdAt;
    private Integer replyCount;  // 回复数量
} 