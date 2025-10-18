package com.micnusz.chat.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable=false)
    @NotBlank(message = "Username cannot be blank.")
    @Size(min = 3, max = 20, message = "Chat room name must be between 3 and 20 characters.")
    private String username;

    @OneToMany(mappedBy = "createdBy")
    private List<ChatRoom> createdRooms = new ArrayList<>();

    @OneToMany(mappedBy = "sender")
    private List<Message> messages = new ArrayList<>();


    public User() {}

    public User(String username) {
        this.username = username;
    }

}
