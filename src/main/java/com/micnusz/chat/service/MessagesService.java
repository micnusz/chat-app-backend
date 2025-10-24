package com.micnusz.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagesService {

    private final MessagesRepository messagesRepository;
    private final MessagesMapper messagesMapper;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final MessagesEncryptionService messagesEncryptionService;

    // SAVE MESSAGES
    @Transactional
    public MessageResponseDTO saveMessage(User sender, MessageRequestDTO requestDTO, ChatRoom room) {
        ChatRoom chatRoom = chatRoomRepository.findById(room.getId())
            .orElseThrow(() -> new RoomNotFoundException(room.getId()));

        boolean isMember = chatRoomRepository.existsByIdAndUsersId(chatRoom.getId(), sender.getId());
        if (!isMember) {
            throw new AccessDeniedException(sender.getUsername());
        }

        String encryptedContent;
        try {
            encryptedContent = messagesEncryptionService.encrypt(requestDTO.getContent());
        } catch (Exception e) {
            // fallback do plaintext, aby zapis nie zawiódł
            System.err.println("Encryption failed: " + e.getMessage());
            encryptedContent = requestDTO.getContent();
        }

        Message message = new Message(sender, encryptedContent, chatRoom);
        Message saved = messagesRepository.save(message);

        return messagesMapper.toDto(saved);
    }

    // GETTING MESSAGES
    public List<MessageResponseDTO> getMessagesByRoomAsDTO(Long roomId, String username) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        boolean isMember = chatRoomRepository.existsByIdAndUsersId(chatRoom.getId(), user.getId());
        if (!isMember) {
            throw new AccessDeniedException(username);
        }

        return messagesRepository.findByChatRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(msg -> {
                    String decryptedContent;
                    try {
                        decryptedContent = messagesEncryptionService.decrypt(msg.getContent());
                    } catch (Exception e) {
                        System.err.println("Decryption failed for message id " + msg.getId() + ": " + e.getMessage());
                        decryptedContent = msg.getContent(); // fallback do plaintext
                    }
                    msg.setContent(decryptedContent);
                    return messagesMapper.toDto(msg);
                })
                .toList();
    }
}
