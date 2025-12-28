package com.music.infra.feign;

import com.music.model.dto.request.ScheduleEventRequest;
import com.music.model.dto.response.ScheduleEventResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "schedule-event-service", url = "${SCHEDULE_EVENT_SERVICE_URL}", configuration = FeignAuthConfig.class)
public interface ScheduleEventClient {

        @PostMapping("v1/schedule/event/post")
        ScheduleEventResponse createEvent(@RequestBody ScheduleEventRequest request);

        @GetMapping("v1/schedule/event/get")
        List<ScheduleEventResponse> getEvents();

        @GetMapping("v1/schedule/event/day")
        List<ScheduleEventResponse> getEventsByUserAndDay(@RequestParam String day);

        @GetMapping("v1/schedule/event/month")
        List<Integer> getDaysWithEventsInMonth(@RequestParam int year,
                        @RequestParam int month);

        @GetMapping("v1/schedule/event/range")
        List<ScheduleEventResponse> getEventsByRange(@RequestParam String start,
                        @RequestParam String end);

        @PutMapping("v1/schedule/event/put/{id}")
        ScheduleEventResponse updateEvent(@PathVariable String id,
                        @RequestBody ScheduleEventRequest request);

        @DeleteMapping("v1/schedule/event/delete/{id}")
        String deleteEvent(@PathVariable String id);
}
