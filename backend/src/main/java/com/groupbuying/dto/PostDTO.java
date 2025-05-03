package com.groupbuying.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDTO {
    private Long id;
    private String content;
    private Long userId;
    private String username;
    private String userAvatar;
    private Integer likes;
    private Boolean liked;
    private List<CommentDTO> comments;
    private LocalDateTime createdAt;
} 