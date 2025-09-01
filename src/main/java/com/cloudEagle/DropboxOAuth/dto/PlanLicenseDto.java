package com.cloudEagle.DropboxOAuth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanLicenseDto {
    private String planType;
    private String licenseType;
    private String accountType;
    private String teamName;
    private Integer numLicensedUsers;
    private Integer numProvisionedUsers;
    private Integer numUsedLicenses;
}
