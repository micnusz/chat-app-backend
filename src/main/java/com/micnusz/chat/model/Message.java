package com.micnusz.chat.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "messages", 
        indexes = {
        @Index(name = "idx_chat_room_id", columnList = "chat_room_id")
    })
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender; 

    @Column(length = 1000, nullable=false)
    private String content;

    private String timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chat_room_id")
    private ChatRoom chatRoom;

    public Message() {}

    public Message(User sender, String content, ChatRoom chatRoom) {
        this.sender = sender;
        this.content = content;
        this.timestamp = Instant.now().toString();
        this.chatRoom = chatRoom;
    }
}
