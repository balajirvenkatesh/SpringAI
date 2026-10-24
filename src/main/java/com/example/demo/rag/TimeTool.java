package com.example.demo.rag;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component 
public class TimeTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTool.class);

    @Tool(name = "getCurrentLocalDateTime", description = "Get the Current Date time in user timezone") 
    String getCurrentLocalTime(){
        LOGGER.info("returning the current date time in user's time zone");
        return LocalDateTime.now().toString();
    }

    @Tool(name = "getCurrentDateTime", description ="Get the current date time in the specified time zone")
    String getCurrentDateTime(@ToolParam(description = "Value representing the time zone") String timeZone ){
        LOGGER.info("returning the current date time of a specified time zone");
        return LocalDateTime.now(ZoneId.of(timeZone)).toString();
    }
}
