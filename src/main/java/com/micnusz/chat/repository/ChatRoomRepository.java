package com.micnusz.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.User;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    boolean existsByIdAndUsersId(Long roomId, Long userId);
    int countByCreatedBy(User user);
    
}
