package com.cloudEagle.DropboxOAuth.repository;

import com.cloudEagle.DropboxOAuth.entity.User;
import com.cloudEagle.DropboxOAuth.entity.type.AuthProviderType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProviderType providerType);
}