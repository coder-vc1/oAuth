# Dropbox API Integration Template

## Authentication

In order to get a Client ID and Client Secret, you need to create an OAuth app on Dropbox:
1. Go to https://www.dropbox.com/developers/apps
2. Click "Create app"
3. Choose "Dropbox API" and "Full Dropbox" access
4. Set the OAuth 2 redirect URI

**Authentication type**: OAuth2 (Authorization Code flow)

**Auth URL**: `https://www.dropbox.com/oauth2/authorize`

**Access Token URL**: `https://api.dropboxapi.com/oauth2/token`

**Refresh Token URL**: `https://api.dropboxapi.com/oauth2/token`

**Client ID/App ID**: [Your Dropbox App Key]

**Client Secret/App Secret**: [Your Dropbox App Secret]

**Scopes**: 
- `account_info.read` - Read account information
- `team_info.read` - Read team information
- `team_data.member` - Read team member data
- `team_data.team_space` - Read team space data
- `team_log.read` - Read team activity logs

**Redirect URL**: `http://localhost:8080/cloudEagle/login/oauth2/code/dropbox`

## Token Management

### JWT Token Structure
- **Access Token**: Contains user info + Dropbox access token, valid for 24 hours
- **Refresh Token**: Valid for 7 days, used to refresh expired access tokens

### Token Refresh Endpoints
- **JWT Refresh**: `POST /cloudEagle/auth/refresh`
- **Dropbox Token Refresh**: `POST /cloudEagle/dropbox/refresh-dropbox-token`

## API Endpoints

### 1. To get the name of the team/organization

**API URL**: `https://api.dropboxapi.com/2/team/get_info`

**Parameters**: None (POST request with empty body)

**Scopes**: `team_info.read`

**Request**: 
```bash
curl -X POST https://api.dropboxapi.com/2/team/get_info \
  --header "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  --header "Content-Type: application/json"
```

**Response**:
```json
{
  "name": "Your Team Name",
  "team_id": "dbid:xxxxxxxxxxxxxxxxxxxxx",
  "num_licensed_users": 10,
  "num_provisioned_users": 10,
  "policies": {
    "sharing": {
      "shared_folder_member_policy": {
        ".tag": "anyone"
      },
      "shared_folder_join_policy": {
        ".tag": "from_anyone"
      }
    }
  }
}
```

### 2. To get the plan type or the license assigned to the account

**API URL**: `https://api.dropboxapi.com/2/users/get_current_account`

**Parameters**: None (POST request with empty body)

**Scopes**: `account_info.read`

**Request**: 
```bash
curl -X POST https://api.dropboxapi.com/2/users/get_current_account \
  --header "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  --header "Content-Type: application/json"
```

**Response**:
```json
{
  "account_id": "dbid:xxxxxxxxxxxxxxxxxxxxx",
  "name": {
    "given_name": "John",
    "surname": "Doe",
    "familiar_name": "John",
    "display_name": "John Doe"
  },
  "email": "john.doe@company.com",
  "email_verified": true,
  "disabled": false,
  "country": "US",
  "locale": "en",
  "referral_link": "https://db.tt/xxxxxxxxx",
  "is_paired": false,
  "account_type": {
    ".tag": "business"
  },
  "profile_photo_url": "https://dl-web.dropbox.com/account_photo/get/dbid%3Axxxxxxxxx?vers=xxxxxxxxx&size=128x128",
  "team": {
    "id": "dbid:xxxxxxxxxxxxxxxxxxxxx",
    "name": "Your Team Name"
  }
}
```

### 3. To obtain the list of all users in the organization using this app

**API URL**: `https://api.dropboxapi.com/2/team/members/list`

**Parameters**: 
```json
{
  "limit": 100,
  "include_removed": false
}
```

**Scopes**: `team_data.member`

**Request**: 
```bash
curl -X POST https://api.dropboxapi.com/2/team/members/list \
  --header "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  --header "Content-Type: application/json" \
  --data '{"limit": 100, "include_removed": false}'
```

**Response**:
```json
{
  "members": [
    {
      "profile": {
        "team_member_id": "dbmid:xxxxxxxxxxxxxxxxxxxxx",
        "external_id": "user123",
        "account_id": "dbid:xxxxxxxxxxxxxxxxxxxxx",
        "email": "user1@company.com",
        "email_verified": true,
        "status": {
          ".tag": "active"
        },
        "name": {
          "given_name": "User",
          "surname": "One",
          "familiar_name": "User",
          "display_name": "User One"
        },
        "membership_type": {
          ".tag": "full"
        },
        "joined_on": "2023-01-01T00:00:00Z"
      },
      "role": {
        ".tag": "member_edit"
      }
    }
  ],
  "has_more": false,
  "cursor": "xxxxxxxxxxxxxxxxxxxxx"
}
```

### 4. To fetch sign-in events of all the users

**API URL**: `https://api.dropboxapi.com/2/team_log/get_events`

**Parameters**: 
```json
{
  "limit": 100,
  "event_types": ["login"],
  "start_time": "2023-01-01T00:00:00Z",
  "end_time": "2023-12-31T23:59:59Z"
}
```

**Scopes**: `team_log.read`

**Request**: 
```bash
curl -X POST https://api.dropboxapi.com/2/team_log/get_events \
  --header "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  --header "Content-Type: application/json" \
  --data '{"limit": 100, "event_types": ["login"], "start_time": "2023-01-01T00:00:00Z", "end_time": "2023-12-31T23:59:59Z"}'
```

**Response**:
```json
{
  "events": [
    {
      "timestamp": "2023-12-01T10:00:00Z",
      "event_category": {
        ".tag": "login"
      },
      "event_type": {
        ".tag": "login_success"
      },
      "details": {
        "ip_address": "192.168.1.1",
        "user_agent": "Mozilla/5.0...",
        "country": "US",
        "city": "New York"
      },
      "actor": {
        "actor_type": {
          ".tag": "user"
        },
        "user": {
          "account_id": "dbid:xxxxxxxxxxxxxxxxxxxxx",
          "email": "user@company.com"
        }
      }
    }
  ],
  "has_more": false,
  "cursor": "xxxxxxxxxxxxxxxxxxxxx"
}
```

## Implementation Notes

- All Dropbox API endpoints require a valid OAuth2 access token
- The access token is obtained during the OAuth2 authorization flow
- Both access and refresh tokens are embedded in the JWT token for secure API access
- JWT tokens expire in 24 hours (with Dropbox tokens) or 10 minutes (basic)
- Refresh tokens expire in 7 days
- All endpoints use POST method with JSON content type
- Rate limiting applies to all API calls
- Team APIs require a Dropbox Business account

## Error Handling

Common error responses:
- `401 Unauthorized`: Invalid or expired access token
- `403 Forbidden`: Insufficient permissions for the requested scope
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Dropbox service error

## Testing

Use the provided Java application endpoints:
- `GET /cloudEagle/dropbox/team-info` - Get team information
- `GET /cloudEagle/dropbox/account-info` - Get account information  
- `GET /cloudEagle/dropbox/team-members` - Get team members
- `GET /cloudEagle/dropbox/sign-in-events` - Get sign-in events
- `POST /cloudEagle/dropbox/refresh-dropbox-token` - Refresh Dropbox access token
- `POST /cloudEagle/auth/refresh` - Refresh JWT token

All endpoints require the JWT token in the Authorization header: `Bearer <JWT_TOKEN>`

## Frontend Integration

### Token Storage
```javascript
// Store tokens after OAuth2 login
localStorage.setItem('accessToken', response.jwt);
localStorage.setItem('refreshToken', response.refreshToken);
```

### Automatic Token Refresh
```javascript
// Refresh JWT token when expired
const refreshToken = async () => {
  const response = await fetch('/cloudEagle/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken: localStorage.getItem('refreshToken') })
  });
  
  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('accessToken', data.jwt);
    localStorage.setItem('refreshToken', data.refreshToken);
    return data.jwt;
  }
  
  // Redirect to login if refresh fails
  window.location.href = '/cloudEagle/login';
};
```
