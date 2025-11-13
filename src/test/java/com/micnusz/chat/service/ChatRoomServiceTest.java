package com.micnusz.chat.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.micnusz.chat.exception.IncorrectPasswordException;
import com.micnusz.chat.exception.MaxRoomsCreatedByUserException;
import com.micnusz.chat.exception.RoomFullException;
import com.micnusz.chat.exception.RoomNotFoundException;
import com.micnusz.chat.exception.UserNotFoundException;
import com.micnusz.chat.exception.UserNotInRoomException;
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
    
    //JoinRoom
    @Test
    void joinRoom_ShouldJoin_WhenUserExists_WhenThereIsLessThanMaxUsers_WhenPasswordExistsOrIsNull() {
        String username = "michal";
        String chatRoomName = "room1";
        String password = "123456";
        Long roomId = 1L;
        User user = new User();
        user.setUsername(username);


        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(chatRoomName);
        chatRoom.setPassword(password);


        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));

        chatRoomService.joinRoom(roomId, username, password);

        assertTrue(chatRoom.getUsers().contains(user));
        verify(chatRoomRepository).save(chatRoom);
    }

    @Test
    void joinRoom_ShouldThrow_WhenNoUserFound() {
        String username = "michal";
        Long roomId = 1L;

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> chatRoomService.joinRoom(roomId, username, "anyPassword"));

        assertTrue(exception.getMessage().contains(username));

        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    void joinRoom_ShouldThrow_WhenNoRoomFound() {
        String username = "michal";
        Long roomId = 1L;
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

        RoomNotFoundException exception = assertThrows(RoomNotFoundException.class,
                () -> chatRoomService.joinRoom(roomId, username, "TestPassword"));

        assertTrue(exception.getMessage().contains(String.valueOf(roomId)));

        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    void joinRoom_ShouldThrow_WhenRoomPasswordIncorrect() {
        String username = "user";
        String roomPassword = "testPassword";
        String incorrectRoomPassword = "IncorrectPassword";
        Long roomId = 1L;

        User user = new User();
        user.setUsername(username);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(roomId);
        chatRoom.setPassword(roomPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));

        IncorrectPasswordException exception = assertThrows(IncorrectPasswordException.class,
                () -> chatRoomService.joinRoom(roomId, username, incorrectRoomPassword));

        assertTrue(exception.getMessage().contains(incorrectRoomPassword));

        verify(chatRoomRepository, never()).save(any());
    }
    
    @Test
    void joinRoom_ShouldThrow_WhenRoomFull() {
        String username = "user";
        String roomPassword = "testPassword";
        Long roomId = 1L;
        
        User user = new User();
        user.setUsername(username);
        
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(roomId);
        chatRoom.setPassword(roomPassword);
        
        for (int i = 0; i < chatRoom.getMaxUsers(); i++) {
            chatRoom.getUsers().add(new User());
        }

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));

        RoomFullException exception = assertThrows(RoomFullException.class,
                () -> chatRoomService.joinRoom(roomId, username, roomPassword));

        assertTrue(exception.getMessage().contains(String.valueOf(roomId)));

        verify(chatRoomRepository, never()).save(any());
    
    }
    
    //LeaveRoom
    @Test
    void leaveRoom_ShouldThrow_WhenUserNotFound() {
        Long roomId = 1L;
        String username = "michal";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> chatRoomService.leaveRoom(roomId, username));

        assertTrue(exception.getMessage().contains(username));
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    void leaveRoom_ShouldThrow_WhenRoomNotFound() {
        Long roomId = 1L;
        String username = "michal";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

        RoomNotFoundException exception = assertThrows(RoomNotFoundException.class,
                () -> chatRoomService.leaveRoom(roomId, username));

        assertTrue(exception.getMessage().contains(String.valueOf(roomId)));
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    void leaveRoom_ShouldThrow_WhenUserNotInRoom() {
        Long roomId = 1L;
        String username = "michal";
        User user = new User();
        user.setUsername(username);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(roomId);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));

        UserNotInRoomException exception = assertThrows(UserNotInRoomException.class,
                () -> chatRoomService.leaveRoom(roomId, username));

        assertTrue(exception.getMessage().contains(username));
        verify(chatRoomRepository, never()).save(any());

    }
        
    @Test
    void leaveRoom_ShouldRemoveUser_WhenUserInRoom() {
        Long roomId = 1L;
        String username = "michal";

        User user = new User();
        user.setUsername(username);

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(roomId);
        chatRoom.getUsers().add(user);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));

        chatRoomService.leaveRoom(roomId, username);

        assertFalse(chatRoom.getUsers().contains(user));
        verify(chatRoomRepository).save(chatRoom);
    }

    //GetRoomById
    @Test
    void getRoomById_ShouldThrow_WhenRoomNotFound() {
        Long roomId = 1L;
        String username = "user";

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

        RoomNotFoundException exception = assertThrows(RoomNotFoundException.class,
                () -> chatRoomService.getRoomById(roomId, username));

        assertTrue(exception.getMessage().contains(String.valueOf(roomId)));
    }

    @Test
    void getRoomById_ShouldThrow_WhenUserNotFound() {
        Long roomId = 1L;
        String username = "user";
        ChatRoom room = new ChatRoom();

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> chatRoomService.getRoomById(roomId, username));

        assertTrue(exception.getMessage().contains(username));
    }

    @Test
    void getRoomById_ShouldThrow_WhenUserNotInRoom() {
        Long roomId = 1L;
        String username = "user";
        ChatRoom room = new ChatRoom();
        User user = new User();
        user.setUsername(username);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserNotInRoomException exception = assertThrows(UserNotInRoomException.class,
                () -> chatRoomService.getRoomById(roomId, username));

        assertTrue(exception.getMessage().contains(username));
    }

    @Test
    void getRoomById_ShouldReturnDto_WhenUserInRoom() {
        Long roomId = 1L;
        String username = "user";

        User user = new User();
        user.setUsername(username);

        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.getUsers().add(user);

        ChatRoomResponseDTO dto = new ChatRoomResponseDTO();
        dto.setId(roomId);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomMapper.toDto(room)).thenReturn(dto);

        ChatRoomResponseDTO result = chatRoomService.getRoomById(roomId, username);

        assertEquals(dto, result);
        verify(chatRoomMapper).toDto(room);
    }

    //DeleteRoom
    @Test
    void deleteRoom_ShouldThrow_WhenRoomNotExists() {
        Long roomId = 1L;
        when(chatRoomRepository.existsById(roomId)).thenReturn(false);

        RoomNotFoundException exception = assertThrows(RoomNotFoundException.class,
                () -> chatRoomService.deleteRoom(roomId));

        assertTrue(exception.getMessage().contains(String.valueOf(roomId)));
        verify(chatRoomRepository, never()).deleteById(any());
    }

    @Test
    void deleteRoom_ShouldDelete_WhenRoomExists() {
        Long roomId = 1L;
        when(chatRoomRepository.existsById(roomId)).thenReturn(true);

        chatRoomService.deleteRoom(roomId);

        verify(chatRoomRepository).deleteById(roomId);
    }



}
