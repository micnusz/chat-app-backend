package com.micnusz.chat.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.micnusz.chat.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MessagesServiceTest {

    @Mock
    private MessagesRepository messagesRepository;
    @InjectMocks
    private MessagesService messagesService;
    @Mock
    private MessagesMapper messagesMapper;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessagesEncryptionService messagesEncryptionService;
    

    //SaveMessage
    @Test
    void saveMessage_shouldEncryptAndSave_whenUserMember() throws Exception {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("michal");

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(1L);

        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setContent("Hello");

        String encrypted = "ENCRYPTED_TEXT";

        Message savedMessage = new Message(sender, encrypted, chatRoom);
        MessageResponseDTO responseDTO = new MessageResponseDTO();
        responseDTO.setContent(encrypted);

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.existsByIdAndUsersId(chatRoom.getId(), sender.getId())).thenReturn(true);
        when(messagesEncryptionService.encrypt(dto.getContent())).thenReturn(encrypted);
        when(messagesRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messagesMapper.toDto(savedMessage)).thenReturn(responseDTO);

        MessageResponseDTO result = messagesService.saveMessage(sender, dto, chatRoom);

        assertTrue(result.getContent().contains("ENCRYPTED"));
        verify(messagesRepository).save(any(Message.class));
        verify(messagesEncryptionService).encrypt(dto.getContent());
    }

    @Test
    void saveMessage_shouldPersistMessageWithCorrectSenderAndRoom() throws Exception {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("michal");

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(1L);

        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setContent("Hello");

        String encrypted = "ENCRYPTED_CONTENT";

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.existsByIdAndUsersId(chatRoom.getId(), sender.getId())).thenReturn(true);
        when(messagesEncryptionService.encrypt(dto.getContent())).thenReturn(encrypted);
        when(messagesRepository.save(any(Message.class))).thenAnswer(i -> i.getArgument(0));
        when(messagesMapper.toDto(any(Message.class))).thenReturn(new MessageResponseDTO());

        messagesService.saveMessage(sender, dto, chatRoom);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messagesRepository).save(captor.capture());

        Message captured = captor.getValue();
        assertTrue(captured.getSender().equals(sender));
        assertTrue(captured.getChatRoom().equals(chatRoom));
        assertTrue(captured.getContent().equals(encrypted));
    }

    @Test
    void saveMessage_shouldSaveUnencrypted_whenEncryptionFails() throws Exception {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("michal");

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(1L);

        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setContent("Plain text");

        Message savedMessage = new Message(sender, dto.getContent(), chatRoom);
        MessageResponseDTO responseDTO = new MessageResponseDTO();
        responseDTO.setContent(dto.getContent());

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.existsByIdAndUsersId(chatRoom.getId(), sender.getId())).thenReturn(true);
        when(messagesEncryptionService.encrypt(dto.getContent())).thenThrow(new RuntimeException("encryption failed"));
        when(messagesRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messagesMapper.toDto(savedMessage)).thenReturn(responseDTO);

        MessageResponseDTO result = messagesService.saveMessage(sender, dto, chatRoom);

        assertTrue(result.getContent().equals(dto.getContent()));
        verify(messagesEncryptionService).encrypt(dto.getContent());
        verify(messagesRepository).save(any(Message.class));
    }

    @Test
    void saveMessage_shouldThrow_whenRoomNotFound() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("username");

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(1L);

        MessageRequestDTO requestDTO = new MessageRequestDTO();
        requestDTO.setContent("content");

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.empty());

        RoomNotFoundException exception = assertThrows(RoomNotFoundException.class,
                () -> messagesService.saveMessage(sender, requestDTO, chatRoom));

        assertTrue(exception.getMessage().contains(String.valueOf(chatRoom.getId())));
        verify(messagesRepository, never()).save(any());
    }
    
    @Test
    void saveMessage_shouldThrow_whenUserNotMember() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("michal");

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(1L);

        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setContent("test");

        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        when(chatRoomRepository.existsByIdAndUsersId(chatRoom.getId(), sender.getId())).thenReturn(false);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> messagesService.saveMessage(sender, dto, chatRoom));

        assertTrue(exception.getMessage().contains(sender.getUsername()));
        verify(messagesRepository, never()).save(any());
    }

    //GetMessage
    @Test
    void getMessagesByRoomAsDTO_shouldThrow_whenRoomNotFound() {
        Long roomId = 1L;
        String username = "user";

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

        RoomNotFoundException exception = assertThrows(RoomNotFoundException.class,
                () -> messagesService.getMessagesByRoomAsDTO(roomId, username));

        assertTrue(exception.getMessage().contains(roomId.toString()));
        verify(messagesRepository, never()).findByChatRoomIdOrderByTimestampAsc(any());
    }

    @Test
    void getMessagesByRoomAsDTO_shouldThrow_whenUserNotFound() {
        Long roomId = 1L;
        String username = "user";

        ChatRoom room = new ChatRoom();
        room.setId(roomId);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> messagesService.getMessagesByRoomAsDTO(roomId, username));

        verify(messagesRepository, never()).findByChatRoomIdOrderByTimestampAsc(any());
    }

    @Test
    void getMessagesByRoomAsDTO_shouldThrow_whenUserNotMember() {
        Long roomId = 1L;
        String username = "user";

        ChatRoom room = new ChatRoom();
        room.setId(roomId);

        User user = new User();
        user.setId(10L);
        user.setUsername(username);

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.existsByIdAndUsersId(roomId, user.getId())).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> messagesService.getMessagesByRoomAsDTO(roomId, username));

        verify(messagesRepository, never()).findByChatRoomIdOrderByTimestampAsc(any());
    }

    @Test
    void getMessagesByRoomAsDTO_shouldReturnDecryptedMessages_whenUserMember() throws Exception {
        Long roomId = 1L;
        String username = "user";

        ChatRoom room = new ChatRoom();
        room.setId(roomId);

        User user = new User();
        user.setId(10L);
        user.setUsername(username);

        Message msg1 = new Message();
        msg1.setId(1L);
        msg1.setContent("encrypted1");

        Message msg2 = new Message();
        msg2.setId(2L);
        msg2.setContent("encrypted2");

        MessageResponseDTO dto1 = new MessageResponseDTO();
        dto1.setContent("decrypted1");
        MessageResponseDTO dto2 = new MessageResponseDTO();
        dto2.setContent("decrypted2");

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.existsByIdAndUsersId(roomId, user.getId())).thenReturn(true);
        when(messagesRepository.findByChatRoomIdOrderByTimestampAsc(roomId)).thenReturn(List.of(msg1, msg2));
        when(messagesEncryptionService.decrypt("encrypted1")).thenReturn("decrypted1");
        when(messagesEncryptionService.decrypt("encrypted2")).thenReturn("decrypted2");
        when(messagesMapper.toDto(msg1)).thenReturn(dto1);
        when(messagesMapper.toDto(msg2)).thenReturn(dto2);

        List<MessageResponseDTO> result = messagesService.getMessagesByRoomAsDTO(roomId, username);

        assertTrue(result.size() == 2);
        assertTrue(result.get(0).getContent().equals("decrypted1"));
        assertTrue(result.get(1).getContent().equals("decrypted2"));
    }

    @Test
    void getMessagesByRoomAsDTO_shouldReturnOriginalContent_whenDecryptionFails() throws Exception {
        Long roomId = 1L;
        String username = "user";

        ChatRoom room = new ChatRoom();
        room.setId(roomId);

        User user = new User();
        user.setId(10L);
        user.setUsername(username);

        Message msg = new Message();
        msg.setId(1L);
        msg.setContent("encrypted");

        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setContent("encrypted");

        when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(chatRoomRepository.existsByIdAndUsersId(roomId, user.getId())).thenReturn(true);
        when(messagesRepository.findByChatRoomIdOrderByTimestampAsc(roomId)).thenReturn(List.of(msg));
        when(messagesEncryptionService.decrypt("encrypted")).thenThrow(new RuntimeException("fail"));
        when(messagesMapper.toDto(msg)).thenReturn(dto);

        List<MessageResponseDTO> result = messagesService.getMessagesByRoomAsDTO(roomId, username);

        assertTrue(result.size() == 1);
        assertTrue(result.get(0).getContent().equals("encrypted"));
    }





}
