package com.cloudEagle.DropboxOAuth.service;

import com.cloudEagle.DropboxOAuth.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DropboxApiService {

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ResponseEntity<TeamInfoResponseDto> getTeamInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>("null", headers);

    ResponseEntity<String> response = restTemplate.exchange(
        "https://api.dropboxapi.com/2/team/get_info",
        HttpMethod.POST,
        entity,
        String.class
    );

    try {
      JsonNode jsonNode = objectMapper.readTree(response.getBody());
      TeamInfoResponseDto teamInfo = new TeamInfoResponseDto(
          jsonNode.get("name").asText(),
          jsonNode.get("team_id").asText()
      );
      return ResponseEntity.ok(teamInfo);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new TeamInfoResponseDto("Error parsing response", ""));
    }
  }

  public ResponseEntity<AccountInfoResponseDto> getAccountInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>("null", headers);

    ResponseEntity<String> response = restTemplate.exchange(
        "https://api.dropboxapi.com/2/users/get_current_account",
        HttpMethod.POST,
        entity,
        String.class
    );

    try {
      JsonNode jsonNode = objectMapper.readTree(response.getBody());
      AccountInfoResponseDto accountInfo = new AccountInfoResponseDto(
          jsonNode.get("account_id").asText(),
          jsonNode.get("email").asText(),
          jsonNode.get("name").get("display_name").asText(),
          jsonNode.get("account_type").get(".tag").asText(),
          jsonNode.has("team") ? jsonNode.get("team").get("id").asText() : null,
          jsonNode.has("team") ? jsonNode.get("team").get("name").asText() : null,
          jsonNode.get("country").asText(),
          jsonNode.get("locale").asText(),
          jsonNode.get("email_verified").asBoolean(),
          jsonNode.get("disabled").asBoolean()
      );
      return ResponseEntity.ok(accountInfo);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new AccountInfoResponseDto());
    }
  }

  public ResponseEntity<AccountPlanLicenseDto> getAccountPlanLicenseInfo(String accessToken) {
    try {
      // First, try to get team information to determine if this is a team token
      JsonNode teamNode = null;
      String accountType = null;
      JsonNode accountNode = null;
      boolean isTeamToken = false;

      try {
        // Try to get team info first - if this succeeds, it's a team token
        // Team API requires an empty JSON body
        ResponseEntity<String> teamResponse = restTemplate.exchange(
            "https://api.dropboxapi.com/2/team/get_info",
            HttpMethod.POST,
            new HttpEntity<>("null", createHeaders(accessToken)),
            String.class
        );
        teamNode = objectMapper.readTree(teamResponse.getBody());
        accountType = "business";
        isTeamToken = true;

        System.out.println("Successfully detected team token, getting team member info...");

        // For team tokens, we need to get a specific team member's info
        // Let's get the first team member to represent the team
        ResponseEntity<String> membersResponse = restTemplate.exchange(
            "https://api.dropboxapi.com/2/team/members/list",
            HttpMethod.POST,
            new HttpEntity<>("{\"limit\":1,\"include_removed\":false}", createHeaders(accessToken)),
            String.class
        );

        JsonNode membersNode = objectMapper.readTree(membersResponse.getBody());
        if (membersNode.has("members") && membersNode.get("members").size() > 0) {
          JsonNode firstMember = membersNode.get("members").get(0);
          String memberId = firstMember.get("profile").get("team_member_id").asText();

          System.out.println("Selected team member: " + memberId);

          // Now get the specific member's account info using the Dropbox-API-Select-User header
          HttpHeaders memberHeaders = createHeaders(accessToken);
          memberHeaders.set("Dropbox-API-Select-User", memberId);

          ResponseEntity<String> memberResponse = restTemplate.exchange(
              "https://api.dropboxapi.com/2/users/get_current_account",
              HttpMethod.POST,
              new HttpEntity<>("null", memberHeaders),
              String.class
          );
          accountNode = objectMapper.readTree(memberResponse.getBody());
          System.out.println("Successfully got team member account info");
        } else {
          System.out.println("No team members found, using team info only");
        }

      } catch (Exception e) {
        // Team API failed, this might be an individual token
        System.out.println(
            "Team API failed, checking if this is an individual token: " + e.getMessage());

        // Only try individual account approach if we haven't confirmed it's a team token
        if (!isTeamToken) {
          try {
            ResponseEntity<String> accountResponse = restTemplate.exchange(
                "https://api.dropboxapi.com/2/users/get_current_account",
                HttpMethod.POST,
                new HttpEntity<>("null", createHeaders(accessToken)),
                String.class
            );
            accountNode = objectMapper.readTree(accountResponse.getBody());
            accountType = accountNode.get("account_type").get(".tag").asText();
            System.out.println("Successfully got individual account info, type: " + accountType);
          } catch (Exception e2) {
            // If individual API also fails with team token error, we know it's a team token
            if (e2.getMessage().contains("entire Dropbox Business team")) {
              System.out.println("Confirmed this is a team token, but team APIs are failing");
              // Try to get at least some basic team info
              try {
                ResponseEntity<String> basicTeamResponse = restTemplate.exchange(
                    "https://api.dropboxapi.com/2/team/get_info",
                    HttpMethod.POST,
                    new HttpEntity<>("null", createHeaders(accessToken)),
                    String.class
                );
                teamNode = objectMapper.readTree(basicTeamResponse.getBody());
                accountType = "business";
                System.out.println("Successfully got basic team info");
              } catch (Exception e3) {
                System.err.println("All team APIs failed: " + e3.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AccountPlanLicenseDto());
              }
            } else {
              System.err.println(
                  "Both team and individual account APIs failed: " + e2.getMessage());
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(new AccountPlanLicenseDto());
            }
          }
        } else {
          // We confirmed it's a team token but failed to get member info
          System.err.println(
              "Confirmed team token but failed to get member info: " + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new AccountPlanLicenseDto());
        }
      }

      // Determine plan type based on account type and team info
      String planType = determinePlanType(accountNode, teamNode);
      String licenseType = determineLicenseType(accountNode, teamNode);

      AccountPlanLicenseDto planLicenseInfo = new AccountPlanLicenseDto(
          accountNode != null ? accountNode.get("account_id").asText() : null,
          accountNode != null ? accountNode.get("email").asText() : null,
          accountNode != null ? accountNode.get("name").get("display_name").asText() : null,
          accountType,
          planType,
          licenseType,
          accountNode != null && accountNode.has("team") ? accountNode.get("team").get("id")
              .asText() : null,
          accountNode != null && accountNode.has("team") ? accountNode.get("team").get("name")
              .asText() : null,
          teamNode != null && teamNode.has("num_licensed_users") ? teamNode.get(
              "num_licensed_users").asInt() : null,
          teamNode != null && teamNode.has("num_provisioned_users") ? teamNode.get(
              "num_provisioned_users").asInt() : null,
          teamNode != null && teamNode.has("num_used_licenses") ? teamNode.get("num_used_licenses")
              .asInt() : null,
          accountNode != null ? accountNode.get("country").asText() : null,
          accountNode != null ? accountNode.get("locale").asText() : null,
          accountNode != null ? accountNode.get("email_verified").asBoolean() : false,
          accountNode != null ? accountNode.get("disabled").asBoolean() : false,
          accountNode != null && accountNode.has("referral_link") ? accountNode.get("referral_link")
              .asText() : null,
          accountNode != null && accountNode.has("profile_photo_url") ? accountNode.get(
              "profile_photo_url").asText() : null,
          teamNode != null && teamNode.has("policies") && teamNode.get("policies").has("emm_state")
              ?
              teamNode.get("policies").get("emm_state").get(".tag").asText() : null,
          teamNode != null && teamNode.has("policies") && teamNode.get("policies")
              .has("office_addin") ?
              teamNode.get("policies").get("office_addin").get(".tag").asText() : null,
          teamNode != null && teamNode.has("policies") && teamNode.get("policies")
              .has("suggest_members_policy") ?
              teamNode.get("policies").get("suggest_members_policy").get(".tag").asText() : null,
          teamNode != null && teamNode.has("policies") && teamNode.get("policies")
              .has("top_level_content_policy") ?
              teamNode.get("policies").get("top_level_content_policy").get(".tag").asText() : null
      );

      return ResponseEntity.ok(planLicenseInfo);
    } catch (Exception e) {
      System.err.println("Error getting account plan and license info: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new AccountPlanLicenseDto());
    }
  }

  public ResponseEntity<PlanLicenseDto> getPlanLicenseInfo(String accessToken) {
    try {
      // First, try to get team information to determine if this is a team token
      JsonNode teamNode = null;
      String accountType = null;
      JsonNode accountNode = null;
      boolean isTeamToken = false;

      try {
        // Try to get team info first - if this succeeds, it's a team token
        // Team API requires an empty JSON body
        ResponseEntity<String> teamResponse = restTemplate.exchange(
            "https://api.dropboxapi.com/2/team/get_info",
            HttpMethod.POST,
            new HttpEntity<>("null", createHeaders(accessToken)),
            String.class
        );
        teamNode = objectMapper.readTree(teamResponse.getBody());
        accountType = "business";
        isTeamToken = true;

        System.out.println("Successfully detected team token, getting team member info...");

        // For team tokens, we need to get a specific team member's info
        // Let's get the first team member to represent the team
        ResponseEntity<String> membersResponse = restTemplate.exchange(
            "https://api.dropboxapi.com/2/team/members/list",
            HttpMethod.POST,
            new HttpEntity<>("{\"limit\":1,\"include_removed\":false}", createHeaders(accessToken)),
            String.class
        );

        JsonNode membersNode = objectMapper.readTree(membersResponse.getBody());
        if (membersNode.has("members") && membersNode.get("members").size() > 0) {
          JsonNode firstMember = membersNode.get("members").get(0);
          String memberId = firstMember.get("profile").get("team_member_id").asText();

          System.out.println("Selected team member: " + memberId);

          // Now get the specific member's account info using the Dropbox-API-Select-User header
          HttpHeaders memberHeaders = createHeaders(accessToken);
          memberHeaders.set("Dropbox-API-Select-User", memberId);

          ResponseEntity<String> memberResponse = restTemplate.exchange(
              "https://api.dropboxapi.com/2/users/get_current_account",
              HttpMethod.POST,
              new HttpEntity<>("null", memberHeaders),
              String.class
          );
          accountNode = objectMapper.readTree(memberResponse.getBody());
          System.out.println("Successfully got team member account info");
        } else {
          System.out.println("No team members found, using team info only");
        }

      } catch (Exception e) {
        // Team API failed, this might be an individual token
        System.out.println(
            "Team API failed, checking if this is an individual token: " + e.getMessage());

        // Only try individual account approach if we haven't confirmed it's a team token
        if (!isTeamToken) {
          try {
            ResponseEntity<String> accountResponse = restTemplate.exchange(
                "https://api.dropboxapi.com/2/users/get_current_account",
                HttpMethod.POST,
                new HttpEntity<>("null", createHeaders(accessToken)),
                String.class
            );
            accountNode = objectMapper.readTree(accountResponse.getBody());
            accountType = accountNode.get("account_type").get(".tag").asText();
            System.out.println("Successfully got individual account info, type: " + accountType);
          } catch (Exception e2) {
            // If individual API also fails with team token error, we know it's a team token
            if (e2.getMessage().contains("entire Dropbox Business team")) {
              System.out.println("Confirmed this is a team token, but team APIs are failing");
              // Try to get at least some basic team info
              try {
                ResponseEntity<String> basicTeamResponse = restTemplate.exchange(
                    "https://api.dropboxapi.com/2/team/get_info",
                    HttpMethod.POST,
                    new HttpEntity<>("null", createHeaders(accessToken)),
                    String.class
                );
                teamNode = objectMapper.readTree(basicTeamResponse.getBody());
                accountType = "business";
                System.out.println("Successfully got basic team info");
              } catch (Exception e3) {
                System.err.println("All team APIs failed: " + e3.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PlanLicenseDto());
              }
            } else {
              System.err.println(
                  "Both team and individual account APIs failed: " + e2.getMessage());
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(new PlanLicenseDto());
            }
          }
        } else {
          // We confirmed it's a team token but failed to get member info
          System.err.println(
              "Confirmed team token but failed to get member info: " + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new PlanLicenseDto());
        }
      }

      // Determine plan type based on account type and team info
      String planType = determinePlanType(accountNode, teamNode);
      String licenseType = determineLicenseType(accountNode, teamNode);

      // Create simplified response with only plan and license information
      PlanLicenseDto planLicenseInfo = new PlanLicenseDto(
          planType,
          licenseType,
          accountType,
          teamNode != null && teamNode.has("name") ? teamNode.get("name").asText() : null,
          teamNode != null && teamNode.has("num_licensed_users") ? teamNode.get(
              "num_licensed_users").asInt() : null,
          teamNode != null && teamNode.has("num_provisioned_users") ? teamNode.get(
              "num_provisioned_users").asInt() : null,
          teamNode != null && teamNode.has("num_used_licenses") ? teamNode.get("num_used_licenses")
              .asInt() : null
      );

      return ResponseEntity.ok(planLicenseInfo);
    } catch (Exception e) {
      System.err.println("Error getting plan and license info: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new PlanLicenseDto());
    }
  }

  private HttpHeaders createHeaders(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private String determinePlanType(JsonNode accountNode, JsonNode teamNode) {
    String accountType = accountNode.get("account_type").get(".tag").asText();

    switch (accountType) {
      case "business":
        return "Business Plan";
      case "basic":
        return "Basic Plan";
      case "pro":
        return "Pro Plan";
      case "plus":
        return "Plus Plan";
      default:
        return "Unknown Plan";
    }
  }

  private String determineLicenseType(JsonNode accountNode, JsonNode teamNode) {
    String accountType = accountNode.get("account_type").get(".tag").asText();

    if ("business".equals(accountType)) {
      if (teamNode != null && teamNode.has("num_licensed_users")) {
        int licensedUsers = teamNode.get("num_licensed_users").asInt();
        if (licensedUsers <= 5) {
          return "Business Starter";
        } else if (licensedUsers <= 20) {
          return "Business Standard";
        } else {
          return "Business Advanced";
        }
      }
      return "Business License";
    } else if ("pro".equals(accountType)) {
      return "Professional License";
    } else if ("plus".equals(accountType)) {
      return "Plus License";
    } else {
      return "Basic License";
    }
  }

  public ResponseEntity<TeamMembersListResponseDto> getTeamMembers(String accessToken,
      Integer limit, String cursor) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Build request body with pagination parameters
    String body;
    if (cursor != null && !cursor.isBlank()) {
      body = String.format("{\"limit\":%d,\"include_removed\":false,\"cursor\":\"%s\"}", limit,
          cursor);
    } else {
      body = String.format("{\"limit\":%d,\"include_removed\":false}", limit);
    }

    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        "https://api.dropboxapi.com/2/team/members/list",
        HttpMethod.POST,
        entity,
        String.class
    );

    try {
      JsonNode jsonNode = objectMapper.readTree(response.getBody());
      List<TeamMemberResponseDto> members = new ArrayList<>();

      JsonNode membersNode = jsonNode.get("members");
      for (JsonNode memberNode : membersNode) {
        JsonNode profile = memberNode.get("profile");
        JsonNode role = memberNode.get("role");

        TeamMemberResponseDto member = new TeamMemberResponseDto(
            profile.get("team_member_id").asText(),
            profile.get("account_id").asText(),
            profile.get("email").asText(),
            profile.get("name").get("display_name").asText(),
            profile.get("status").get(".tag").asText(),
            profile.get("membership_type").get(".tag").asText(),
            role.get(".tag").asText(),
            profile.get("joined_on").asText()
        );
        members.add(member);
      }

      TeamMembersListResponseDto result = new TeamMembersListResponseDto(
          members,
          jsonNode.get("has_more").asBoolean(),
          jsonNode.has("cursor") ? jsonNode.get("cursor").asText() : null
      );

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new TeamMembersListResponseDto());
    }
  }

  public ResponseEntity<TeamMembersListResponseDto> getTeamMembers(String accessToken) {
    return getTeamMembers(accessToken, 100, null);
  }

  public ResponseEntity<SignInEventsListResponseDto> getSignInEvents(
      String accessToken, Integer limit, String cursor) {

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    String body;
    if (cursor != null && !cursor.isBlank()) {
      body = String.format("{\"limit\":%d,\"category\":\"logins\",\"cursor\":\"%s\"}", limit, cursor);
    } else {
      body = String.format("{\"limit\":%d,\"category\":\"logins\"}", limit);
    }

    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    ResponseEntity<String> response = restTemplate.exchange(
        "https://api.dropboxapi.com/2/team_log/get_events",
        HttpMethod.POST,
        entity,
        String.class
    );

    System.out.println("--->Sign-in events response: " + response.getBody());

    try {
      JsonNode jsonNode = objectMapper.readTree(response.getBody());
      List<SignInEventResponseDto> events = new ArrayList<>();

      JsonNode eventsNode = jsonNode.get("events");
      if (eventsNode != null && eventsNode.isArray()) {
        for (JsonNode eventNode : eventsNode) {

          // Extract actor info
          String accountId = null;
          String email = null;

          if (eventNode.has("actor")) {
            JsonNode actorNode = eventNode.get("actor");

            if (actorNode.has("user")) { // some events
              JsonNode userNode = actorNode.get("user");
              accountId = userNode.has("account_id") ? userNode.get("account_id").asText() : null;
              email = userNode.has("email") ? userNode.get("email").asText() : null;

            } else if (actorNode.has("admin")) { // your case
              JsonNode adminNode = actorNode.get("admin");
              accountId = adminNode.has("account_id") ? adminNode.get("account_id").asText() : null;
              email = adminNode.has("email") ? adminNode.get("email").asText() : null;
            }
          }

          // Extract geo location
          String ipAddress = null;
          String country = null;
          String city = null;
          if (eventNode.has("origin") && eventNode.get("origin").has("geo_location")) {
            JsonNode geoNode = eventNode.get("origin").get("geo_location");
            ipAddress = geoNode.has("ip_address") ? geoNode.get("ip_address").asText() : null;
            country = geoNode.has("country") ? geoNode.get("country").asText() : null;
            city = geoNode.has("city") ? geoNode.get("city").asText() : null;
          }

          // Extract access method (user agent)
          String userAgent = null;
          if (eventNode.has("origin") && eventNode.get("origin").has("access_method")) {
            JsonNode accessNode = eventNode.get("origin").get("access_method");
            if (accessNode.has("end_user") && accessNode.get("end_user").has(".tag")) {
              userAgent = accessNode.get("end_user").get(".tag").asText();
            }
          }

          // Build DTO
          SignInEventResponseDto event = new SignInEventResponseDto(
              eventNode.get("timestamp").asText(),
              eventNode.has("event_category") ? eventNode.get("event_category").get(".tag").asText() : null,
              eventNode.has("event_type") ? eventNode.get("event_type").get(".tag").asText() : null,
              ipAddress,
              userAgent,
              country,
              city,
              accountId,
              email
          );
          events.add(event);
        }
      }

      SignInEventsListResponseDto result = new SignInEventsListResponseDto(
          events,
          jsonNode.has("has_more") && jsonNode.get("has_more").asBoolean(),
          jsonNode.has("cursor") ? jsonNode.get("cursor").asText() : null
      );

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new SignInEventsListResponseDto());
    }
  }


  public ResponseEntity<SignInEventsListResponseDto> getSignInEvents(String accessToken) {
    return getSignInEvents(accessToken, 10, null);
  }


  public ResponseEntity<String> refreshAccessToken(String refreshToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    String body = String.format("grant_type=refresh_token&refresh_token=%s", refreshToken);
    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    return restTemplate.exchange(
        "https://api.dropboxapi.com/oauth2/token",
        HttpMethod.POST,
        entity,
        String.class
    );
  }
}

