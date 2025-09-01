package com.cloudEagle.DropboxOAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInEventResponseDto {
    private String timestamp;
    private String eventCategory;
    private String eventType;
    private String ipAddress;
    private String userAgent;
    private String country;
    private String city;
    private String accountId;
    private String email;
}
