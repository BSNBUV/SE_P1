package com.astra.command.auth;

import com.astra.command.common.Role;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final String secret;

    public TokenService(@Value("${astra.jwt.secret}") String secret) {
        this.secret = secret;
    }

    public String issue(String username, Role role) {
        long exp = Instant.now().plusSeconds(8 * 60 * 60).getEpochSecond();
        String body = username + "|" + role + "|" + exp;
        return Base64.getUrlEncoder().withoutPadding().encodeToString((body + "|" + sign(body)).getBytes(StandardCharsets.UTF_8));
    }

    public TokenPrincipal verify(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|");
            if (parts.length != 4) {
                return null;
            }
            String body = parts[0] + "|" + parts[1] + "|" + parts[2];
            if (!sign(body).equals(parts[3]) || Long.parseLong(parts[2]) < Instant.now().getEpochSecond()) {
                return null;
            }
            return new TokenPrincipal(parts[0], Role.valueOf(parts[1]));
        } catch (Exception ex) {
            return null;
        }
    }

    private String sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign auth token", ex);
        }
    }

    public record TokenPrincipal(String username, Role role) {
    }
}
