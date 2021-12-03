package com.aiskov.aws.products.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        return new UserDetailsImpl(this.userRepository.getById(name));
    }

    @Data
    public static class UserDetailsImpl implements UserDetails {
        private final String password;
        private final String username;

        public UserDetailsImpl(User user) {
            this.username = user.getUsername();
            this.password = user.getPassword();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(Authority.USER);
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        public enum Authority implements GrantedAuthority {
            USER;

            @Override
            public String getAuthority() {
                return this.name();
            }
        }
    }
}
