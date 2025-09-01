package com.cloudEagle.DropboxOAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfoResponseDto {
    private String accountId;
    private String email;
    private String displayName;
    private String accountType;
    private String teamId;
    private String teamName;
    private String country;
    private String locale;
    private boolean emailVerified;
    private boolean disabled;
}
