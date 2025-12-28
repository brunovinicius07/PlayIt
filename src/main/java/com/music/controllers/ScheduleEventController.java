package com.music.controllers;

import com.music.infra.feign.ScheduleEventClient;
import com.music.model.dto.request.ScheduleEventRequest;
import com.music.model.dto.response.ScheduleEventResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/music/event")
public class ScheduleEventController {

    private final ScheduleEventClient service;

    @PostMapping("/post")
    public ResponseEntity<ScheduleEventResponse> registerEvent(
            @RequestBody @Valid ScheduleEventRequest request) {

        ScheduleEventResponse response = service.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/day")
    public ResponseEntity<List<ScheduleEventResponse>> getEventsByUserAndDay(
            @RequestParam String day) {

        return ResponseEntity.ok(
                service.getEventsByUserAndDay(day));
    }

    @GetMapping("/month")
    public ResponseEntity<List<Integer>> getDaysWithEventsInMonth(
            @RequestParam int year,
            @RequestParam int month) {

        return ResponseEntity.ok(
                service.getDaysWithEventsInMonth(year, month));
    }

    @GetMapping("/range")
    public ResponseEntity<List<ScheduleEventResponse>> getEventsByRange(
            @RequestParam String start,
            @RequestParam String end) {

        return ResponseEntity.ok(
                service.getEventsByRange(start, end));
    }

    @PutMapping("/put/{id}")
    public ResponseEntity<ScheduleEventResponse> updateScheduleEvent(
            @PathVariable String id,
            @RequestBody @Valid ScheduleEventRequest scheduleEventRequest) {

        ScheduleEventResponse response = service.updateEvent(id, scheduleEventRequest);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteScheduleEvent(@PathVariable String id) {

        String message = service.deleteEvent(id);

        return ResponseEntity.ok(message);
    }
}
