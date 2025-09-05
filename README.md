# Dropbox OAuth2 Integration

## 🎯 **Project Overview**

This project demonstrates a complete integration of Dropbox Business APIs with OAuth2 authentication, implemented in Spring Boot.

## ✨ **Features Implemented**

- ✅ **OAuth2 Authentication Flow** - Complete Dropbox OAuth2 integration
- ✅ **JWT Token Management** - Secure token handling with embedded Dropbox tokens
- ✅ **Team Information API** - Get team/organization details
- ✅ **Plan & License API** - Simplified endpoint for account plan/license (7 fields only)
- ✅ **Account Details API** - Comprehensive account information (25 fields)
- ✅ **Team Members API** - List organization users with pagination
- ✅ **Sign-in Events API** - Track user sign-in events with detailed location and access information
- ✅ **Refresh Token Support** - Automatic token refresh for both JWT and Dropbox
- ✅ **Smart Token Handling** - Automatic detection of team vs. individual tokens

## 🚀 **Quick Start**

### **Prerequisites**
- Java 17+
- Maven 3.6+
- PostgreSQL database
- Dropbox Business account (free trial)

### **1. Clone the Repository**
```bash
git clone [your-github-repo-url]
cd DropboxOAuth
```

### **2. Configure Database**
```bash
# Create PostgreSQL database
createdb oAuthDB

# Update application.properties with your database credentials
```

### **3. Configure Dropbox OAuth2**
```yaml
# Update src/main/resources/application.yml
dropbox:
  app:
    key: YOUR_DROPBOX_APP_KEY
    secret: YOUR_DROPBOX_APP_SECRET
```

### **4. Run the Application**
```bash
mvn spring-boot:run
```

### **5. Access the Application**
- **OAuth2 Login:** http://localhost:8080{context-path}/login
- **API Base URL:** http://localhost:8080{context-path}

## 🔐 **Authentication Flow**

1. **Visit:** `http://localhost:8080{context-path}/login`
2. **Click:** Dropbox OAuth2 button
3. **Authorize:** On Dropbox's website
4. **Receive:** JWT token with embedded Dropbox access token
5. **Use:** JWT token in Authorization header for API calls

## 📚 **API Endpoints**

### **Authentication**
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/auth/login` | GET | Initiate OAuth2 login |
| `/auth/refresh` | POST | Refresh JWT tokens |

### **Dropbox APIs**
| Endpoint | Method | Purpose | Response Fields |
|----------|--------|---------|-----------------|
| `/dropbox/team-info` | GET | Get team information | Team details |
| `/dropbox/plan-license` | GET | **Plan & license only** | **7 fields** |
| `/dropbox/account-plan-license` | GET | Full account details | 25 fields |
| `/dropbox/team-members` | GET | List team members | Paginated list |
| `/dropbox/sign-in-events` | GET | Get sign-in events | Paginated events |

## 🧪 **Testing with Postman**

### **OAuth2 Configuration**
- **Type:** OAuth 2.0
- **Grant Type:** Authorization Code
- **Callback URL:** `http://localhost:8080{context-path}/login/oauth2/code/dropbox`
- **Auth URL:** `https://www.dropbox.com/oauth2/authorize`
- **Access Token URL:** `https://api.dropboxapi.com/oauth2/token`
- **Client ID:** Your Dropbox App Key
- **Client Secret:** Your Dropbox App Secret

### **API Testing**
```bash
# Example: Get plan and license information
curl --location 'http://localhost:8080{context-path}/dropbox/plan-license' \
--header 'Authorization: Bearer YOUR_JWT_TOKEN'
```

## 📁 **Project Structure**

```
src/main/java/com{context-path}/DropboxOAuth/
├── controller/
│   ├── AuthController.java          # OAuth2 authentication
│   └── DropboxController.java       # Dropbox API endpoints
├── service/
│   └── DropboxApiService.java       # Dropbox API service layer
├── security/
│   ├── OAuth2SuccessHandler.java    # OAuth2 success handling
│   ├── AuthService.java             # JWT token management
│   ├── AuthUtil.java                # JWT utility methods
│   └── WebSecurityConfig.java       # Security configuration
├── dto/                             # Data Transfer Objects
│   ├── PlanLicenseDto.java          # Simplified plan/license (7 fields)
│   ├── AccountPlanLicenseDto.java   # Comprehensive account (25 fields)
│   ├── TeamInfoResponseDto.java     # Team information
│   ├── TeamMembersListResponseDto.java # Team members with pagination
│   └── SignInEventsListResponseDto.java # Sign-in events with pagination
└── entity/                          # Database entities
    └── User.java                    # User management
```

## 🔧 **Key Technical Features**

### **Smart Token Handling**
- Automatic detection of team vs. individual Dropbox tokens
- Proper handling of `Dropbox-API-Select-User` header
- Fallback mechanisms for different token types

### **Pagination Support**
- Cursor-based pagination for team members
- Cursor-based pagination for sign-in events
- Configurable limits and cursor management

### **Error Handling**
- Graceful error handling for API failures
- Comprehensive logging for debugging
- User-friendly error responses

### **Security Features**
- JWT-based authentication
- OAuth2 integration with Dropbox
- Secure token storage and refresh
- Role-based access control

## 📊 **Response Examples**

### **Simplified Plan & License (7 fields)**
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

### **Team Information**
```json
{
  "name": "Ei",
  "teamId": "dbtid:AADh-SthkCRVodiCf7H0Aq_GS-nsoCW0XG4",
  "numLicensedUsers": 5,
  "numProvisionedUsers": 1,
  "numUsedLicenses": 1
}
```

## 🚨 **Troubleshooting**

### **Common Issues**

1. **"No static resource" Error**
   - **Cause:** Accessing endpoints without authentication
   - **Solution:** Complete OAuth2 login flow first

2. **Port 8080 Already in Use**
   - **Solution:** Kill existing process or change port in `application.properties`

3. **Database Connection Issues**
   - **Solution:** Ensure PostgreSQL is running and database exists

4. **OAuth2 Configuration Errors**
   - **Solution:** Verify Dropbox app credentials and redirect URLs


## 📈 **Performance & Scalability**

- **Connection Pooling:** HikariCP for database connections
- **Caching:** Spring Boot caching support
- **Async Processing:** Support for asynchronous operations
- **Rate Limiting:** Configurable API rate limiting
- **Monitoring:** Health checks and metrics endpoints

## 🔒 **Security Considerations**

- **HTTPS Only:** Production deployments should use HTTPS
- **Token Expiration:** Configurable JWT and OAuth2 token expiration
- **Scope Limitation:** Minimal required scopes for production
- **Input Validation:** Comprehensive input validation and sanitization
- **Audit Logging:** Complete audit trail for all operations

## 📚 **Documentation**

- **API Documentation:** Comprehensive endpoint documentation
- **OAuth2 Guide:** Step-by-step authentication setup
- **Testing Guide:** Postman configuration and testing
- **Deployment Guide:** Production deployment instructions


## 🔗 **Links**

- **Dropbox API Documentation:** https://www.dropbox.com/developers/documentation
- **Spring Boot Documentation:** https://spring.io/projects/spring-boot
- **OAuth2 Specification:** https://oauth.net/2/

---

