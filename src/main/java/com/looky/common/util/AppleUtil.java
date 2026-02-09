package com.looky.common.util;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleUtil {

    @Value("${apple.team-id}")
    private String teamId;

    @Value("${apple.client-id}")
    private String clientId;

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.private-key-path}")
    private String privateKeyPath;

    public String createClientSecret() {
        Date expirationDate = Date.from(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .header().add("kid", keyId).and()
                .issuer(teamId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expirationDate)
                .audience().add("https://appleid.apple.com").and()
                .subject(clientId)
                .signWith(getPrivateKey(), Jwts.SIG.ES256)
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            String path = privateKeyPath.replace("classpath:", "");
            ClassPathResource resource = new ClassPathResource(path);

            String privateKeyContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            Reader pemReader = new StringReader(privateKeyContent);
            PEMParser pemParser = new PEMParser(pemReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();

            return converter.getPrivateKey(object);
        } catch (Exception e) {
            log.error("Apple Private Key Load Failed: {}", e.getMessage());
            throw new RuntimeException("Apple Login Error: 키 파일을 읽을 수 없습니다.", e);
        }
    }
}