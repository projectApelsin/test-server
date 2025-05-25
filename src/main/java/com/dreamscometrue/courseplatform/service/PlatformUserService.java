package com.dreamscometrue.courseplatform.service;

import com.dreamscometrue.courseplatform.model.PlatformUser;
import com.dreamscometrue.courseplatform.model.repository.PlatformUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class PlatformUserService implements UserDetailsService {

    private final PlatformUserRepository platformUserRepository;

    private final PasswordEncoder passwordEncoder;

    public PlatformUserService(PlatformUserRepository platformUserRepository, PasswordEncoder passwordEncoder) {
        this.platformUserRepository = platformUserRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public void register(String username, String password) {
        PlatformUser platformUser = new PlatformUser();
        platformUser.setUsername(username);
        platformUser.setPassword(passwordEncoder.encode(password));
        platformUserRepository.save(platformUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PlatformUser user = platformUserRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
