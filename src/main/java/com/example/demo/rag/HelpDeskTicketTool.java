package com.example.demo.rag;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.entity.HelpDeskTicket;
import com.example.demo.model.TicketRequest;
import com.example.demo.service.HelpDeskTicketService;

@Component 
public class HelpDeskTicketTool {

    @Autowired 
    private HelpDeskTicketService helpDeskTicketService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTool.class);

    @Tool(name = "createHelpDestTicket", description = "Create Help Dest Ticket to user") 
    String createTicket(@ToolParam(description = "Details to create a support ticket")TicketRequest ticketRequest, ToolContext toolContext ){
        LOGGER.info("starterd creating a new issue.");
        String username = (String)toolContext.getContext().get("username");
        HelpDeskTicket savedHelpDeskTicket = helpDeskTicketService.createTicket(ticketRequest, username);
        return "ticket "+ savedHelpDeskTicket.getId()+" created successfuly for the user:"+savedHelpDeskTicket.getUsername();
    }

    @Tool( description ="Fetch the status of the OPEN ticket based on given username")
    List<HelpDeskTicket> getTicketStatus(ToolContext toolContext ){
        String username = (String)toolContext.getContext().get("username");
        LOGGER.info("returning the current date time of a specified time zone");
        return helpDeskTicketService.getTicketsByUsername(username);
    }

    @Tool(description = "Update the status of the user's HelpDeskTicket with the given id")
    String updateStatus(@ToolParam (description = "Ticket id and the new status (OPEN, IN_PROGRESS or CLOSED)") HelpDeskTicket helpDeskTicket,
                        ToolContext toolContext){
        // Only the current user's tickets, so one user can't update another user's ticket
        String username = (String)toolContext.getContext().get("username");
        List<HelpDeskTicket> savedHelpDeskTicketList = helpDeskTicketService.getTicketsByUsername(username);
        List<HelpDeskTicket> updatedTickets = savedHelpDeskTicketList.stream()
            .filter(ticket -> ticket.getId().equals(helpDeskTicket.getId()))
            .map(ticket -> {
                ticket.setStatus(helpDeskTicket.getStatus());
                return ticket;
            })
            .toList();
        if (updatedTickets.isEmpty()) {
            return "No ticket " + helpDeskTicket.getId() + " found for the user:" + username;
        }
        helpDeskTicketService.saveTickets(updatedTickets);
        return "ticket " + helpDeskTicket.getId() + " status updated to " + helpDeskTicket.getStatus() + " for the user:" + username;
    }

}
