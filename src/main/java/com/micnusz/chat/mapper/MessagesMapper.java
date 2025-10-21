package com.micnusz.chat.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.dto.MessageResponseDTO;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.Message;
import com.micnusz.chat.model.User;

@Component
public class MessagesMapper {

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT; 

    public Message toEntity(MessageRequestDTO dto, User sender, ChatRoom chatRoom) {
        return new Message(sender, dto.getContent(), chatRoom);
    }


    public MessageResponseDTO toDto(Message message) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setUsername(message.getSender().getUsername());
        dto.setContent(message.getContent());
        dto.setRoomId(message.getChatRoom().getId());
        dto.setId(message.getId());
        dto.setTimestamp(message.getTimestamp() != null ? formatter.format(message.getTimestamp()) : null);
        return dto;
    }
}
