package com.micnusz.chat.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.micnusz.chat.dto.ChatRoomRequestDTO;
import com.micnusz.chat.dto.ChatRoomResponseDTO;
import com.micnusz.chat.exception.MaxRoomsCreatedByUserException;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.mapper.ChatRoomMapper;
import com.micnusz.chat.model.ChatRoom;
import com.micnusz.chat.model.User;
import com.micnusz.chat.repository.ChatRoomRepository;
import com.micnusz.chat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatRoomMapper chatRoomMapper;
    @InjectMocks
    private ChatRoomService chatRoomService;


    //CreateRooom
    @Test
    void createRoom_ShouldCreateRoom_WhenUserExistsAndHasCapacity() {
        //given
        String username = "michal";
        User user = new User();
        user.setUsername(username);
        user.setMaxRooms(3);

        ChatRoomRequestDTO requestDTO = new ChatRoomRequestDTO();
        requestDTO.setName("Room1");
        requestDTO.setPassword("123456");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.countByCreatedBy(user)).thenReturn(1);

        //when
        ChatRoomResponseDTO result = chatRoomService.createRoom(requestDTO, username);

        //then
        assertNotNull(result);
        assertEquals("Room1", result.getName());
        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    void createRoom_ShouldThrow_WhenNoUserFound() {
        String username = "michal";
        User user = new User();

        user.setUsername(username);
        user.setMaxRooms(3);

        ChatRoomRequestDTO requestDTO = new ChatRoomRequestDTO();

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> chatRoomService.createRoom(requestDTO, username));

        assertTrue(exception.getMessage().contains(username));

        verify(chatRoomRepository, never()).save(any());
    }
    
    @Test
    void createRoom_ShouldThrow_WhenUserTryToCreateMoreThanMaxCountRooms() {

        String username = "michal";
        User user = new User();
        user.setMaxRooms(3);

        ChatRoomRequestDTO requestDTO = new ChatRoomRequestDTO();
        requestDTO.setName("Room1");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.countByCreatedBy(user)).thenReturn(3);

        MaxRoomsCreatedByUserException exception = assertThrows(MaxRoomsCreatedByUserException.class,
                () -> chatRoomService.createRoom(requestDTO, username));

        assertTrue(exception.getMessage().contains(username));
        assertTrue(exception.getMessage().contains(String.valueOf(user.getMaxRooms())));

        verify(chatRoomRepository, never()).save(any());

    }
    

    //GetAllRooms
    @Test
    void getAllRooms_ShouldReturnList_WhenRoomsExists() {
        ChatRoom room1 = new ChatRoom();
        room1.setName("room1");
        ChatRoom room2 = new ChatRoom();
        room2.setName("room2");

        when(chatRoomRepository.findAll()).thenReturn(List.of(room1, room2));
        when(chatRoomMapper.toDto(room1)).thenReturn(new ChatRoomResponseDTO(room1));
        when(chatRoomMapper.toDto(room2)).thenReturn(new ChatRoomResponseDTO(room2));

        List<ChatRoomResponseDTO> result = chatRoomService.getAllRooms();

        assertEquals(2, result.size());
        assertEquals("room1", result.get(0).getName());
        assertEquals("room2", result.get(1).getName());
    }

    @Test
    void getAllRooms_ShouldReturnEmptyList_WhenNoRoomsExists() {
        
        when(chatRoomRepository.findAll()).thenReturn(List.of());

        List<ChatRoomResponseDTO> result = chatRoomService.getAllRooms();

        assertTrue(result.isEmpty());
    }
}
