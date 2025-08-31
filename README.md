# Dropbox OAuth Integration Application

This Spring Boot application demonstrates OAuth2 integration with Dropbox API, including authentication and data fetching capabilities.

## Features

- **OAuth2 Authentication**: Secure login via Dropbox OAuth2
- **JWT Token Management**: Secure token-based authentication
- **Dropbox API Integration**: Access to team, account, and user data
- **RESTful Endpoints**: Clean API design for external consumption

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Dropbox Developer Account
- Dropbox Business Account (for team APIs)

## Setup Instructions

### 1. Create Dropbox OAuth App

1. Go to [Dropbox Developers Console](https://www.dropbox.com/developers/apps)
2. Click "Create app"
3. Choose "Dropbox API" and "Full Dropbox" access
4. Set the OAuth 2 redirect URI to: `http://localhost:8080/cloudEagle/login/oauth2/code/dropbox`
5. Note down your **App Key** and **App Secret**

### 2. Configure Application Properties

Update `src/main/resources/application.yml` with your Dropbox app credentials:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          dropbox:
            client-id: YOUR_APP_KEY
            client-secret: YOUR_APP_SECRET
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/cloudEagle/login/oauth2/code/dropbox
            scope: account_info.read,team_info.read,team_data.member,team_log.read
        provider:
          dropbox:
            authorization-uri: https://www.dropbox.com/oauth2/authorize
            token-uri: https://api.dropboxapi.com/oauth2/token
            user-info-uri: https://api.dropboxapi.com/2/users/get_current_account
            user-name-attribute: account_id

jwt:
  secretKey: your-super-secret-jwt-key-here-make-it-long-and-random
```

### 3. Build and Run

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication Flow

1. **OAuth2 Login**: `GET /cloudEagle/login/oauth2/code/dropbox`
2. **Login Page**: `GET /cloudEagle/login`

### Dropbox API Endpoints

All endpoints require JWT token in Authorization header: `Bearer <JWT_TOKEN>`

- **Team Information**: `GET /cloudEagle/dropbox/team-info`
- **Account Information**: `GET /cloudEagle/dropbox/account-info`
- **Team Members**: `GET /cloudEagle/dropbox/team-members`
- **Sign-in Events**: `GET /cloudEagle/dropbox/sign-in-events`

## Usage Examples

### 1. OAuth2 Login

1. Navigate to `http://localhost:8080/cloudEagle/login`
2. Click "Login with Dropbox"
3. Complete OAuth2 authorization
4. Receive JWT token in response

### 2. API Calls

```bash
# Get team information
curl -X GET http://localhost:8080/cloudEagle/dropbox/team-info \
  --header "Authorization: Bearer YOUR_JWT_TOKEN"

# Get account information
curl -X GET http://localhost:8080/cloudEagle/dropbox/account-info \
  --header "Authorization: Bearer YOUR_JWT_TOKEN"

# Get team members
curl -X GET http://localhost:8080/cloudEagle/dropbox/team-members \
  --header "Authorization: Bearer YOUR_JWT_TOKEN"

# Get sign-in events
curl -X GET http://localhost:8080/cloudEagle/dropbox/sign-in-events \
  --header "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Demo Program

The `DropboxApiDemo.java` class demonstrates how to use the Dropbox API directly:

1. Replace `YOUR_DROPBOX_ACCESS_TOKEN_HERE` with your actual access token
2. Run the main method to see API responses
3. The program fetches team info, account info, team members, and sign-in events

## Security Features

- **JWT-based Authentication**: Secure token management
- **OAuth2 Integration**: Industry-standard OAuth2 flow
- **Session Management**: Secure session handling
- **Error Handling**: Comprehensive error responses

## Troubleshooting

### Common Issues

1. **"Invalid redirect URI"**: Ensure redirect URI matches exactly in Dropbox app settings
2. **"Insufficient permissions"**: Check that your Dropbox app has the required scopes
3. **"Team APIs not available"**: Ensure you have a Dropbox Business account
4. **"JWT token expired"**: Re-authenticate via OAuth2 flow

### Debug Mode

Enable debug logging in `application.yml`:

```yaml
logging:
  level:
    com.cloudEagle.DropboxOAuth: DEBUG
    org.springframework.security: DEBUG
```

## API Documentation

For detailed API information, see `DROPBOX_API_TEMPLATE.md` which includes:
- Complete API endpoint details
- Request/response examples
- Required scopes and permissions
- Error handling information

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review the API documentation
3. Check Dropbox API status at [Dropbox Status Page](https://status.dropbox.com/)
4. Open an issue in the repository
