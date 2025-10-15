package com.micnusz.chat.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.micnusz.chat.model.Message;
import com.micnusz.chat.service.MessagesService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessagesController {

    private final MessagesService messageService;

    @GetMapping("/{roomId}")
    public List<Message> getMessages(@PathVariable Long roomId) {
        return messageService.getMessagesByRoom(roomId);
    }
}

