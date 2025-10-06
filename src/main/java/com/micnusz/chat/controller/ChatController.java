package com.micnusz.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.micnusz.chat.model.Message;

@Controller
public class ChatController {
    

    @MessageMapping("/send-message")
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        return message;
    }
}
