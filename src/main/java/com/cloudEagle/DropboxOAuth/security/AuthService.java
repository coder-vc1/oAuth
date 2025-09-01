package com.cloudEagle.DropboxOAuth.security;
import com.cloudEagle.DropboxOAuth.dto.LoginRequestDto;
import com.cloudEagle.DropboxOAuth.dto.LoginResponseDto;
import com.cloudEagle.DropboxOAuth.dto.SignUpRequestDto;
import com.cloudEagle.DropboxOAuth.dto.SignupResponseDto;
import com.cloudEagle.DropboxOAuth.entity.User;
import com.cloudEagle.DropboxOAuth.entity.type.AuthProviderType;
import com.cloudEagle.DropboxOAuth.entity.type.RoleType;
import com.cloudEagle.DropboxOAuth.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final AuthUtil authUtil;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public LoginResponseDto login(LoginRequestDto loginRequestDto) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(),
            loginRequestDto.getPassword())
    );

    User user = (User) authentication.getPrincipal();

    String token = authUtil.generateAccessToken(user);
    String refreshToken = authUtil.generateRefreshToken(user);

    return new LoginResponseDto(token, refreshToken, user.getId());
  }

  public User signUpInternal(SignUpRequestDto signupRequestDto, AuthProviderType authProviderType,
      String providerId) {
    User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElse(null);

    if (user != null) {
      throw new IllegalArgumentException("User already exists");
    }

    user = User.builder()
        .username(signupRequestDto.getUsername())
        .providerId(providerId)
        .providerType(authProviderType)
        .roles(signupRequestDto.getRoles())
        .build();

    if (authProviderType == AuthProviderType.EMAIL) {
      user.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
    }

    user = userRepository.save(user);

    return user;
  }

  // login controller
  public SignupResponseDto signup(SignUpRequestDto signupRequestDto) {
    User user = signUpInternal(signupRequestDto, AuthProviderType.EMAIL, null);
    return new SignupResponseDto(user.getId(), user.getUsername());
  }

  @Transactional
  public ResponseEntity<LoginResponseDto> handleOAuth2LoginRequest(OAuth2User oAuth2User,
      String registrationId, String dropboxAccessToken, String dropboxRefreshToken) {
    AuthProviderType providerType = authUtil.getProviderTypeFromRegistrationId(registrationId);
    String providerId = authUtil.determineProviderIdFromOAuth2User(oAuth2User, registrationId);

    User user = userRepository.findByProviderIdAndProviderType(providerId, providerType)
        .orElse(null);
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");

    User emailUser = userRepository.findByUsername(email).orElse(null);

    if (user == null && emailUser == null) {
      // signup flow:
      String username = authUtil.determineUsernameFromOAuth2User(oAuth2User, registrationId,
          providerId);
      user = signUpInternal(
          new SignUpRequestDto(username, null, name, Set.of(RoleType.USER)),
          providerType, providerId);
    } else if (user != null) {
      if (email != null && !email.isBlank() && !email.equals(user.getUsername())) {
        user.setUsername(email);
        userRepository.save(user);
      }
    } else {
      throw new BadCredentialsException(
          "This email is already registered with provider " + emailUser.getProviderType());
    }

    // Generate JWT tokens with Dropbox tokens if available
    String accessToken;
    String refreshToken;
    
    if (dropboxAccessToken != null && !dropboxAccessToken.isBlank()) {
      // Always include Dropbox access token in both tokens if available
      if (dropboxRefreshToken != null && !dropboxRefreshToken.isBlank()) {
        // Both tokens available
        accessToken = authUtil.generateAccessTokenWithDropboxTokens(user, dropboxAccessToken, dropboxRefreshToken);
        refreshToken = authUtil.generateRefreshTokenWithDropboxTokens(user, dropboxAccessToken, dropboxRefreshToken);
      } else {
        // Only access token available
        accessToken = authUtil.generateAccessTokenWithDropboxToken(user, dropboxAccessToken);
        refreshToken = authUtil.generateRefreshTokenWithDropboxToken(user, dropboxAccessToken);
      }
    } else {
      // No Dropbox tokens available
      accessToken = authUtil.generateAccessToken(user);
      refreshToken = authUtil.generateRefreshToken(user);
    }

    LoginResponseDto loginResponseDto = new LoginResponseDto(accessToken, refreshToken, user.getId());
    return ResponseEntity.ok(loginResponseDto);
  }

  public LoginResponseDto refreshToken(String refreshToken) {
    try {
      // Validate the refresh token
      if (!authUtil.isRefreshToken(refreshToken)) {
        throw new BadCredentialsException("Invalid refresh token");
      }

      // Extract user information from refresh token
      String username = authUtil.getUsernameFromToken(refreshToken);
      User user = userRepository.findByUsername(username).orElse(null);
      
      if (user == null) {
        throw new BadCredentialsException("User not found");
      }

      // Extract Dropbox tokens from the old refresh token if they exist
      String dropboxAccessToken = authUtil.getDropboxAccessTokenFromToken(refreshToken);
      String dropboxRefreshToken = authUtil.getDropboxRefreshTokenFromToken(refreshToken);

      // Generate new access and refresh tokens
      String newAccessToken;
      String newRefreshToken;
      
      if (dropboxAccessToken != null && !dropboxAccessToken.isBlank()) {
        // Dropbox access token is available
        if (dropboxRefreshToken != null && !dropboxRefreshToken.isBlank()) {
          // Both tokens available
          newAccessToken = authUtil.generateAccessTokenWithDropboxTokens(user, dropboxAccessToken, dropboxRefreshToken);
          newRefreshToken = authUtil.generateRefreshTokenWithDropboxTokens(user, dropboxAccessToken, dropboxRefreshToken);
        } else {
          // Only access token available
          newAccessToken = authUtil.generateAccessTokenWithDropboxToken(user, dropboxAccessToken);
          newRefreshToken = authUtil.generateRefreshTokenWithDropboxToken(user, dropboxAccessToken);
        }
      } else {
        // No Dropbox tokens available
        newAccessToken = authUtil.generateAccessToken(user);
        newRefreshToken = authUtil.generateRefreshToken(user);
      }

      return new LoginResponseDto(newAccessToken, newRefreshToken, user.getId());
    } catch (Exception e) {
      throw new BadCredentialsException("Invalid refresh token: " + e.getMessage());
    }
  }
}


















