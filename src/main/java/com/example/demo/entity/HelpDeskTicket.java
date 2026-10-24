package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter 
@Setter 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class HelpDeskTicket {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id; 

    private String username;

    private String issue;

    private Status status;

    private LocalDateTime createdAt;

    private LocalDateTime eta;
}
