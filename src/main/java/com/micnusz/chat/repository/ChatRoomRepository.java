package com.micnusz.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.micnusz.chat.model.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
}
