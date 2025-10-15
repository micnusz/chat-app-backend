package com.micnusz.chat.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.micnusz.chat.dto.ChatRoomRequestDTO;
import com.micnusz.chat.dto.ChatRoomResponseDTO;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.User;

@Component
public class ChatRoomMapper {

    public ChatRoom toEntity(ChatRoomRequestDTO dto, User creator) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(dto.getName());
        chatRoom.setPassword(dto.getPassword());
        chatRoom.setCreatedBy(creator);
        return chatRoom;
    }

    public ChatRoomResponseDTO toDto(ChatRoom entity) {
    ChatRoomResponseDTO dto = new ChatRoomResponseDTO();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setRequiresPassword(entity.getPassword() != null && !entity.getPassword().isEmpty());
    dto.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getUsername() : null);
    dto.setCreatedAt(entity.getCreatedAt() != null ?
        entity.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
    return dto;
}

}
