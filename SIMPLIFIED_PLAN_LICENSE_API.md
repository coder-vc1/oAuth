# Simplified Plan & License API - Essential Information Only

## Overview

Based on your request to focus specifically on "To get the plan type or the license assigned to the account", I've created a simplified endpoint that returns only the essential plan and license details without the comprehensive account information.

## New Simplified Endpoint

### **Endpoint**: `GET /cloudEagle/dropbox/plan-license`

### **Purpose**: Get only the essential plan type and license information

### **Response**: Simplified `PlanLicenseDto` with 7 focused fields

## API Details

### **Request**
```bash
GET /cloudEagle/dropbox/plan-license
Authorization: Bearer <JWT_TOKEN>
```

### **Response Structure**
```json
{
  "planType": "Business Plan",
  "licenseType": "Business Starter",
  "accountType": "business",
  "teamName": "Ei",
  "numLicensedUsers": 5,
  "numProvisionedUsers": 1,
  "numUsedLicenses": 1
}
```

## Field Descriptions

### **Core Plan & License Information**
- **`planType`**: The type of plan (e.g., "Business Plan", "Pro Plan", "Basic Plan")
- **`licenseType`**: The specific license tier (e.g., "Business Starter", "Business Standard", "Professional License")
- **`accountType`**: The account category (e.g., "business", "pro", "basic")

### **Team Context (for Business Accounts)**
- **`teamName`**: The name of the team/organization
- **`numLicensedUsers`**: Total number of licensed users in the team
- **`numProvisionedUsers`**: Number of provisioned user accounts
- **`numUsedLicenses`**: Number of licenses currently in use

## Comparison: Comprehensive vs. Simplified

### **Comprehensive Endpoint** (`/account-plan-license`)
- **Fields**: 25 comprehensive fields
- **Includes**: Full account details, user information, policies, settings
- **Use Case**: Complete account management and administration

### **Simplified Endpoint** (`/plan-license`)
- **Fields**: 7 essential fields
- **Includes**: Only plan and license information
- **Use Case**: Quick plan status checks and license monitoring

## Usage Examples

### **Frontend Integration**
```javascript
// Get simplified plan and license information
const getPlanAndLicense = async () => {
  const response = await fetch('/cloudEagle/dropbox/plan-license', {
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });
  
  if (response.ok) {
    const data = await response.json();
    
    // Display plan information
    console.log(`Plan: ${data.planType}`);
    console.log(`License: ${data.licenseType}`);
    console.log(`Account Type: ${data.accountType}`);
    
    // Display team information (if business account)
    if (data.accountType === 'business') {
      console.log(`Team: ${data.teamName}`);
      console.log(`Licensed Users: ${data.numLicensedUsers}`);
      console.log(`Used Licenses: ${data.numUsedLicenses}`);
    }
  }
};
```

### **Business Intelligence Dashboard**
```javascript
// Quick plan status check
const checkPlanStatus = async () => {
  const response = await fetch('/cloudEagle/dropbox/plan-license');
  const data = await response.json();
  
  // Plan status indicators
  const planStatus = {
    plan: data.planType,
    license: data.licenseType,
    isBusiness: data.accountType === 'business',
    licenseUtilization: data.numLicensedUsers ? 
      (data.numUsedLicenses / data.numLicensedUsers * 100).toFixed(1) + '%' : 'N/A'
  };
  
  return planStatus;
};
```

## Business Use Cases

### **1. Plan Monitoring**
- **Quick Status Check**: Monitor current plan type and license tier
- **Upgrade Planning**: Identify when to upgrade plans
- **Cost Analysis**: Track license utilization and costs

### **2. Team Management**
- **License Utilization**: Monitor how many licenses are being used
- **Capacity Planning**: Plan for team expansion
- **Resource Allocation**: Optimize license distribution

### **3. Reporting & Analytics**
- **Executive Dashboards**: High-level plan and license overview
- **Monthly Reports**: Track plan changes and usage patterns
- **Compliance Monitoring**: Ensure license compliance

## Response Examples by Account Type

### **Business Account Response**
```json
{
  "planType": "Business Plan",
  "licenseType": "Business Starter",
  "accountType": "business",
  "teamName": "Ei",
  "numLicensedUsers": 5,
  "numProvisionedUsers": 1,
  "numUsedLicenses": 1
}
```

### **Pro Account Response**
```json
{
  "planType": "Pro Plan",
  "licenseType": "Professional License",
  "accountType": "pro",
  "teamName": null,
  "numLicensedUsers": null,
  "numProvisionedUsers": null,
  "numUsedLicenses": null
}
```

### **Basic Account Response**
```json
{
  "planType": "Basic Plan",
  "licenseType": "Basic License",
  "accountType": "basic",
  "teamName": null,
  "numLicensedUsers": null,
  "numProvisionedUsers": null,
  "numUsedLicenses": null
}
```

## Implementation Details

### **New DTO Created**
```java
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
```

### **New Service Method**
- **Method**: `getPlanLicenseInfo(String accessToken)`
- **Logic**: Same intelligent token detection as comprehensive endpoint
- **Response**: Simplified DTO with only essential fields

### **New Controller Endpoint**
- **Mapping**: `@GetMapping("/plan-license")`
- **Response**: `ResponseEntity<PlanLicenseDto>`
- **Authentication**: JWT token required

## Benefits of the Simplified Endpoint

### **1. Focused Information**
- **Essential Data**: Only plan and license details
- **Reduced Noise**: No unnecessary account information
- **Clear Purpose**: Specific to plan and license requirements

### **2. Performance Benefits**
- **Smaller Response**: Reduced payload size
- **Faster Processing**: Less data to parse and display
- **Efficient Caching**: Smaller responses cache better

### **3. Frontend Friendly**
- **Simple Integration**: Easy to consume in UI components
- **Reduced Complexity**: Fewer fields to handle
- **Better UX**: Focused information for users

## Testing the New Endpoint

### **Test Command**
```bash
curl --location 'http://localhost:8080/cloudEagle/dropbox/plan-license' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN'
```

### **Expected Response**
Based on your working comprehensive endpoint, you should get:
```json
{
  "planType": "Business Plan",
  "licenseType": "Business Starter",
  "accountType": "business",
  "teamName": "Ei",
  "numLicensedUsers": 5,
  "numProvisionedUsers": 1,
  "numUsedLicenses": 1
}
```

## Summary

The new simplified endpoint `/cloudEagle/dropbox/plan-license` provides:

✅ **Focused Information**: Only plan and license details
✅ **Essential Data**: 7 key fields for business intelligence
✅ **Same Reliability**: Uses the same robust token detection logic
✅ **Better Performance**: Smaller response payload
✅ **Easy Integration**: Simple DTO for frontend consumption

This endpoint perfectly addresses your requirement to "get the plan type or the license assigned to the account" with a clean, focused response that contains only the essential information needed for plan monitoring and license management.
