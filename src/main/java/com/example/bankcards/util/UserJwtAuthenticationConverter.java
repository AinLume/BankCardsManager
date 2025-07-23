package com.example.bankcards.util;

import com.example.bankcards.entity.User;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * @author komarov
 * @date 22.07.2025
 */
@Component
@RequiredArgsConstructor
public class UserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {

        final long userId = Long.parseLong(jwt.getSubject());

        final User user = userService.findById(userId).get();

        return new UsernamePasswordAuthenticationToken(user, jwt, user.getAuthorities());
    }
}
