package com.micnusz.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.micnusz.chat.model.Message;

public interface MessagesRepository extends JpaRepository<Message, Long> {
    
}
