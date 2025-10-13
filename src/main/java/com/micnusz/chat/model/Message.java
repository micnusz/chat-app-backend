package com.micnusz.chat.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "messages")
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User sender; 

    @Column(length = 1000)
    private String content;

    private String roomId; 

    private LocalDateTime timestamp;

    public Message() {}

    public Message(User sender, String content, String roomId) {
        this.sender = sender;
        this.content = content;
        this.roomId = roomId;
        this.timestamp = LocalDateTime.now();
    }
}
