package com.example.bankcards.controller;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.service.UserService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@RestController
@Profile("dev")
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserService userService;

    @Value("${application.private-key-path}")
    private String privateKeyPath;

    @PostMapping("/token")
    public String testToken(@RequestParam Long userId) {

        return userService
            .findById(userId)
            .map(this::generateJwtToken)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private String generateJwtToken(final User user) {

        try (final InputStream inputStream = new FileInputStream(ResourceUtils.getFile(privateKeyPath))) {

            final String key = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(getKeySpec(key));
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);

            return Jwts
                .builder()
                .subject(user.getId().toString())
                .claim("iat", new Date())
                .claim("exp", new Date(System.currentTimeMillis() + 3600000)) // 1 час
                .signWith(privateKey)
                .compact();

        } catch (final Exception e) {
            throw new RuntimeException("Не удалось создать токен доступа", e);
        }
    }

    private byte[] getKeySpec(String keyValue) {
        keyValue = keyValue.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
        return Base64.getMimeDecoder().decode(keyValue);
    }
}
