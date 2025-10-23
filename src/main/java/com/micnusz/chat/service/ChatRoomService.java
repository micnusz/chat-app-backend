package com.micnusz.chat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.micnusz.chat.dto.ChatRoomRequestDTO;
import com.micnusz.chat.dto.ChatRoomResponseDTO;
import com.micnusz.chat.exception.IncorrectPasswordException;
import com.micnusz.chat.exception.RoomFullException;
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

    // CREATING ROOM
    @Transactional
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

    // GETTING LIST OF ALL ROOMS
    public List<ChatRoomResponseDTO> getAllRooms() {
        return chatRoomRepository.findAll().stream().map(chatRoomMapper::toDto).collect(Collectors.toList());
    }
    
    // JOINING ROOM
    public void joinRoom(Long roomId, String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));

        // password validation
        if (chatRoom.getPassword() != null && !chatRoom.getPassword().isEmpty()) {
            if (password == null || !password.equals(chatRoom.getPassword())) {
                throw new IncorrectPasswordException(password);
            }
        }
        
        // checking max users
        if (chatRoom.getUsers().size() >= chatRoom.getMaxUsers()) {
            throw new RoomFullException(roomId);
        }
        

        // adding user
        chatRoom.getUsers().add(user);
        chatRoomRepository.save(chatRoom);
    }

    // LEAVING ROOM
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
    
    // GETTING ROOM BY ID
    public ChatRoomResponseDTO getRoomById(Long roomId, String username, String password) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
                
        if (chatRoom.getPassword() != null && !chatRoom.getPassword().isEmpty()
                && !chatRoom.getUsers().contains(user)) {
            throw new IncorrectPasswordException(password);
        }        

        return chatRoomMapper.toDto(chatRoom);
    }

    // DELETING ROOM
    public void deleteRoom(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new RoomNotFoundException(roomId);
        }
        chatRoomRepository.deleteById(roomId);
    }


    
}


