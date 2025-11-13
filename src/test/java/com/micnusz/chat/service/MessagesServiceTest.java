package com.micnusz.chat.service;

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

}
