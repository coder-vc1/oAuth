package com.cloudEagle.DropboxOAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInEventsListResponseDto {
    private List<SignInEventResponseDto> events;
    private boolean hasMore;
    private String cursor;
}
