package com.groupbuying.repository;

import com.groupbuying.entity.GroupBuy;
import com.groupbuying.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {
    Optional<GroupBuy> findByInviteCode(String inviteCode);
    
    List<GroupBuy> findByStatusAndExpiryTimeBefore(GroupBuy.Status status, LocalDateTime time);
    
    @Query("SELECT g FROM GroupBuy g WHERE g.status = 'IN_PROGRESS' AND g.currentSize < g.requiredSize AND g.expiryTime > ?1")
    List<GroupBuy> findActiveGroups(LocalDateTime now);
    
    List<GroupBuy> findByLeaderId(Long leaderId);
    
    List<GroupBuy> findByParticipantsContaining(User user);
    
    List<GroupBuy> findByProductIdAndStatusAndExpiryTimeAfter(Long productId, GroupBuy.Status status, LocalDateTime time);
} 