package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.HelpDeskTicket;
import com.example.demo.entity.Status;
import com.example.demo.model.TicketRequest;
import com.example.demo.repository.HelpDeskTicketRepository;

@Service 
public class HelpDeskTicketService {

    @Autowired 
    private HelpDeskTicketRepository helpDeskTicketRepository;

    public HelpDeskTicket createTicket(TicketRequest ticketRequest, String username){
        HelpDeskTicket helpDeskTicket = HelpDeskTicket.builder()
            .issue(ticketRequest.issue())
            .username(username)
            .status(Status.OPEN)
            .createdAt(LocalDateTime.now())
            .eta(LocalDateTime.now().plusDays(5))
            .build();
        return helpDeskTicketRepository.save(helpDeskTicket);
    }

    public List<HelpDeskTicket> getTicketsByUsername(String username){
        return helpDeskTicketRepository.findByUsername(username);
    }

    public List<HelpDeskTicket> saveTickets(List<HelpDeskTicket> helpDeskTickets){
        return helpDeskTicketRepository.saveAll(helpDeskTickets);
    }

}
