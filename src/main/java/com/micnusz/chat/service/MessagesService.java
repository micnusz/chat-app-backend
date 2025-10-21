package com.micnusz.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.micnusz.chat.dto.MessageRequestDTO;
import com.micnusz.chat.dto.MessageResponseDTO;
import com.micnusz.chat.exception.AccessDeniedException;
import com.micnusz.chat.exception.RoomNotFoundException;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.mapper.MessagesMapper;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.Message;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.ChatRoomRepository;
import com.micnusz.chat.repository.MessagesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagesService {

    private final MessagesRepository messagesRepository;
    private final MessagesMapper messagesMapper;
    private final ChatRoomRepository chatRoomRepository;
    private final UserService userService;



    public Message saveMessage(User sender, MessageRequestDTO requestDTO, ChatRoom room) {
        Message message = new Message(sender, requestDTO.getContent(), room);
        return messagesRepository.save(message);
    }


    public List<MessageResponseDTO> getMessagesByRoomAsDTO(Long roomId, String username) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
            
        User user = userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        if (!chatRoom.getUsers().contains(user)) {
            throw new AccessDeniedException(username);
        }

        return messagesRepository.findByChatRoomIdOrderByTimestampAsc(roomId).stream().map(messagesMapper::toDTO)
                .toList();
}


    
}
