package com.cloudEagle.DropboxOAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberResponseDto {
    private String teamMemberId;
    private String accountId;
    private String email;
    private String displayName;
    private String status;
    private String membershipType;
    private String role;
    private String joinedOn;
}
