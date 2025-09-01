package com.cloudEagle.DropboxOAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountPlanLicenseDto {
    private String accountId;
    private String email;
    private String displayName;
    private String accountType;
    private String planType;
    private String licenseType;
    private String teamId;
    private String teamName;
    private Integer numLicensedUsers;
    private Integer numProvisionedUsers;
    private Integer numUsedLicenses;
    private String country;
    private String locale;
    private boolean emailVerified;
    private boolean disabled;
    private String referralLink;
    private String profilePhotoUrl;
    private String emmState;
    private String officeAddinStatus;
    private String suggestMembersPolicy;
    private String topLevelContentPolicy;
}
