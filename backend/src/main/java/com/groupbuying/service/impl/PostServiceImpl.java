package com.groupbuying.service.impl;

import com.groupbuying.dto.PostDTO;
import com.groupbuying.entity.Post;
import com.groupbuying.entity.User;
import com.groupbuying.entity.Like;
import com.groupbuying.entity.Comment;
import com.groupbuying.dto.CommentDTO;
import com.groupbuying.exception.BusinessException;
import com.groupbuying.exception.ErrorCode;
import com.groupbuying.repository.PostRepository;
import com.groupbuying.repository.UserRepository;
import com.groupbuying.repository.LikeRepository;
import com.groupbuying.repository.CommentRepository;
import com.groupbuying.service.PostService;
import com.groupbuying.util.DTOConverter;
import com.groupbuying.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final DTOConverter dtoConverter;
    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    @Override
    @Transactional
    public PostDTO createPost(PostDTO postDTO) {
        try {
            log.info("开始创建帖子，内容：{}", postDTO.getContent());
            
            // 获取当前登录用户
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                log.error("创建帖子失败：用户未登录");
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            // 验证帖子内容
            if (postDTO.getContent() == null || postDTO.getContent().trim().isEmpty()) {
                log.error("创建帖子失败：帖子内容为空");
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "帖子内容不能为空");
            }

            // 创建新帖子
            Post post = new Post();
            post.setContent(postDTO.getContent().trim());
            post.setUser(currentUser);
            post.setLikes(0);
            post.setComments(new ArrayList<>());

            // 保存帖子
            log.info("正在保存帖子...");
            Post savedPost = postRepository.save(post);
            log.info("帖子保存成功，ID：{}", savedPost.getId());
            
            // 转换为DTO并返回
            return dtoConverter.convertToPostDTO(savedPost);
        } catch (BusinessException e) {
            log.error("创建帖子失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("创建帖子失败，异常信息：", e);
            throw new BusinessException(ErrorCode.SERVER_ERROR, "创建帖子失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PostDTO updatePost(Long id, PostDTO postDTO) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("帖子不存在"));

        if (!post.getUser().getId().equals(postDTO.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        post.setContent(postDTO.getContent());
        Post updatedPost = postRepository.save(post);
        return dtoConverter.convertToPostDTO(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        // 获取当前登录用户
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        // 获取帖子
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, "帖子不存在"));

        // 验证是否是帖子作者
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有帖子作者才能删除帖子");
        }

        // 删除帖子
        postRepository.delete(post);
    }

    @Override
    public PostDTO getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, "帖子不存在"));
        
        PostDTO dto = dtoConverter.convertToPostDTO(post);
        
        // 获取帖子的所有顶级评论
        List<Comment> topLevelComments = commentRepository.findByPostAndParentIsNullOrderByCreatedAtDesc(post);
        List<CommentDTO> commentDTOs = new ArrayList<>();
        
        // 转换每个顶级评论，并加载其回复
        for (Comment comment : topLevelComments) {
            CommentDTO commentDTO = dtoConverter.convertToCommentDTO(comment);
            // 获取该评论的所有回复
            List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
            if (replies != null && !replies.isEmpty()) {
                commentDTO.setReplies(replies.stream()
                    .map(dtoConverter::convertToCommentDTO)
                    .collect(Collectors.toList()));
            }
            commentDTOs.add(commentDTO);
        }
        
        dto.setComments(commentDTOs);
        
        // 设置点赞状态
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser != null) {
            dto.setLiked(likeRepository.existsByUserAndPost(currentUser, post));
        }
        
        return dto;
    }

    @Override
    public Page<PostDTO> getAllPosts(Pageable pageable) {
        User currentUser = SecurityUtil.getCurrentUser();
        Page<Post> posts = postRepository.findAll(pageable);
        
        return posts.map(post -> {
            PostDTO dto = dtoConverter.convertToPostDTO(post);
            
            // 获取帖子的所有顶级评论
            List<Comment> topLevelComments = commentRepository.findByPostAndParentIsNullOrderByCreatedAtDesc(post);
            List<CommentDTO> commentDTOs = new ArrayList<>();
            
            // 转换每个顶级评论，并加载其回复
            for (Comment comment : topLevelComments) {
                CommentDTO commentDTO = dtoConverter.convertToCommentDTO(comment);
                // 获取该评论的所有回复
                List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
                if (replies != null && !replies.isEmpty()) {
                    commentDTO.setReplies(replies.stream()
                        .map(dtoConverter::convertToCommentDTO)
                        .collect(Collectors.toList()));
                }
                commentDTOs.add(commentDTO);
            }
            
            dto.setComments(commentDTOs);
            
            // 设置点赞状态
            if (currentUser != null) {
                dto.setLiked(likeRepository.existsByUserAndPost(currentUser, post));
            } else {
                dto.setLiked(false);
            }
            
            return dto;
        });
    }

    @Override
    public Page<PostDTO> getUserPosts(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        
        return postRepository.findByUser(user, pageable)
                .map(post -> dtoConverter.convertToPostDTO(post));
    }

    @Override
    @Transactional
    public PostDTO addComment(Long postId, String content) {
        try {
            log.info("开始添加评论，帖子ID：{}，内容：{}", postId, content);
            
            // 获取当前登录用户
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                log.error("添加评论失败：用户未登录");
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            }
            log.info("当前用户：{}", currentUser.getUsername());

            // 获取帖子
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, "帖子不存在"));
            log.info("找到帖子：{}", post.getId());

            // 创建新评论
            Comment comment = new Comment();
            comment.setContent(content.trim());
            comment.setUser(currentUser);
            comment.setPost(post);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setReplyCount(0);

            // 保存评论
            log.info("保存评论...");
            Comment savedComment = commentRepository.save(comment);
            log.info("评论保存成功，ID：{}", savedComment.getId());
            
            // 重新获取帖子以确保评论列表已更新
            post = postRepository.findById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, "帖子不存在"));

            // 返回更新后的帖子信息
            return dtoConverter.convertToPostDTO(post);
        } catch (Exception e) {
            log.error("添加评论失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void toggleLike(Long postId) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Like like = likeRepository.findByUserAndPost(currentUser, post);
        if (like != null) {
            // 如果已经点赞，则取消点赞
            likeRepository.delete(like);
            post.setLikes(post.getLikes() - 1);
        } else {
            // 如果未点赞，则添加点赞
            like = new Like();
            like.setUser(currentUser);
            like.setPost(post);
            likeRepository.save(like);
            post.setLikes(post.getLikes() + 1);
        }
        postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getReplies(Long commentId) {
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "评论不存在"));
        
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(commentId);
        return replies.stream()
                .map(dtoConverter::convertToCommentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostDTO addReply(Long commentId, CommentDTO replyDTO) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }

        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "评论不存在"));

        Comment reply = new Comment();
        reply.setContent(replyDTO.getContent().trim());
        reply.setUser(currentUser);
        reply.setPost(parentComment.getPost());
        reply.setParent(parentComment);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setReplyCount(0);

        // 保存回复
        commentRepository.save(reply);

        // 更新父评论的回复数
        parentComment.setReplyCount(parentComment.getReplyCount() + 1);
        commentRepository.save(parentComment);

        // 重新获取帖子以确保数据是最新的
        Post updatedPost = postRepository.findById(parentComment.getPost().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND, "帖子不存在"));

        // 返回更新后的帖子信息
        return dtoConverter.convertToPostDTO(updatedPost);
    }
} 