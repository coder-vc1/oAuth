package com.cloudEagle.DropboxOAuth.security;
import com.cloudEagle.DropboxOAuth.dto.LoginResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String registrationId = token.getAuthorizedClientRegistrationId();

        // Get the OAuth2 access token
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
            registrationId,
            token.getName()
        );
        String dropboxAccessToken = client != null ? client.getAccessToken().getTokenValue() : null;
        String dropboxRefreshToken = client != null && client.getRefreshToken() != null ? 
            client.getRefreshToken().getTokenValue() : null;
        
        // Store the dropbox tokens in the request for the auth service to use
        if (dropboxAccessToken != null) {
            request.setAttribute("dropboxAccessToken", dropboxAccessToken);
        }
        if (dropboxRefreshToken != null) {
            request.setAttribute("dropboxRefreshToken", dropboxRefreshToken);
        }

        ResponseEntity<LoginResponseDto> loginResponse = authService.handleOAuth2LoginRequest(oAuth2User,
                registrationId, dropboxAccessToken, dropboxRefreshToken);

        response.setStatus(loginResponse.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(loginResponse.getBody()));
    }
}
