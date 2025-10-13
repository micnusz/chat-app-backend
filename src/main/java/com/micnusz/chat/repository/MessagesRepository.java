package com.micnusz.chat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.micnusz.chat.model.Message;

public interface MessagesRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByRoomIdOrderByTimestampAsc(String roomId);
}
