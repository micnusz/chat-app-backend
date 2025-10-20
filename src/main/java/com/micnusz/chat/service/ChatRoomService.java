package com.micnusz.chat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.micnusz.chat.dto.ChatRoomRequestDTO;
import com.micnusz.chat.dto.ChatRoomResponseDTO;
import com.micnusz.chat.exception.IncorrectPasswordException;
import com.micnusz.chat.exception.RoomNotFoundException;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.exception.UserNotInRoomException;
import com.micnusz.chat.mapper.ChatRoomMapper;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.ChatRoomRepository;
import com.micnusz.chat.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatRoomMapper chatRoomMapper;

    public ChatRoomResponseDTO createRoom(ChatRoomRequestDTO request, String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));

    ChatRoom chatRoom = new ChatRoom();
    chatRoom.setName(request.getName());
    chatRoom.setPassword(request.getPassword());
    chatRoom.setCreatedBy(user);
    
    chatRoomRepository.save(chatRoom);

    return new ChatRoomResponseDTO(chatRoom);
}

    public List<ChatRoomResponseDTO> getAllRooms() {
        return chatRoomRepository.findAll().stream().map(chatRoomMapper::toDto).collect(Collectors.toList());
    }
    
    
    public void joinRoom(Long roomId, String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));

        if (chatRoom.getPassword() != null && !chatRoom.getPassword().isEmpty()) {
            if (password == null || !password.equals(chatRoom.getPassword())) {
                throw new IncorrectPasswordException(password);
            }
        }

        chatRoom.getUsers().add(user);
        chatRoomRepository.save(chatRoom);
    }

    public void leaveRoom(Long roomId, String username) {
        User user = userRepository.findByUsername(username)
                 .orElseThrow(() -> new UserNotFoundException(username));
                
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        if (!chatRoom.getUsers().contains(user)) {
            throw new UserNotInRoomException(username, roomId);
    }

        chatRoom.getUsers().remove(user);
        chatRoomRepository.save(chatRoom);
    }
    

    public ChatRoomResponseDTO getRoomById(Long id) {
        ChatRoom chatRoom = chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found with id: " + id));

        return chatRoomMapper.toDto(chatRoom);
    }

    public void deleteRoom(Long id) {
        if (!chatRoomRepository.existsById(id)) {
            throw new RuntimeException("Chatroom not found with id: " + id);
        }
        chatRoomRepository.deleteById(id);
    }


    
}


