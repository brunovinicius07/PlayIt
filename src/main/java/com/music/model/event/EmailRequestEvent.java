package com.music.model.event;

import com.music.model.enums.EmailType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequestEvent {
    private String to;
    private String subject;
    private EmailType type;
    private Map<String, Object> props;
}
