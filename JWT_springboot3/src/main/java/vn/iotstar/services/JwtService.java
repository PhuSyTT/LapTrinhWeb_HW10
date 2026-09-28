package vn.iotstar.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, JWTClaimsSet::getSubject);
    }

    public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
        final JWTClaimsSet claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        try {
            Date now = new Date(System.currentTimeMillis());
            Date expiryDate = new Date(System.currentTimeMillis() + expiration);

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(userDetails.getUsername())
                    .issueTime(now)
                    .expirationTime(expiryDate);

            if (extraClaims != null) {
                extraClaims.forEach(claimsBuilder::claim);
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();

            // Create SignedJWT with JWSHeader (HS256)
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256)
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claimsSet
            );

            // Sign using HMAC signer from Nimbus
            JWSSigner signer = new MACSigner(getSignInKey());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error signing JWT token with Nimbus: " + e.getMessage(), e);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, JWTClaimsSet::getExpirationTime);
    }

    private JWTClaimsSet extractAllClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(getSignInKey());
            if (!signedJWT.verify(verifier)) {
                throw new RuntimeException("JWT signature is invalid");
            }
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid JWT format: " + e.getMessage(), e);
        } catch (JOSEException e) {
            throw new RuntimeException("Error verifying JWT signature: " + e.getMessage(), e);
        }
    }

    private byte[] getSignInKey() {
        String cleanKey = secretKey.trim();
        // If 64 hex characters (256 bits), decode as hex
        if (cleanKey.matches("^[0-9a-fA-F]{64}$")) {
            byte[] keyBytes = new byte[32];
            for (int i = 0; i < 32; i++) {
                keyBytes[i] = (byte) Integer.parseInt(cleanKey.substring(i * 2, i * 2 + 2), 16);
            }
            return keyBytes;
        }
        // If Base64 string >= 32 bytes
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(cleanKey);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
        }
        // Fallback to UTF-8 bytes
        byte[] utf8Bytes = cleanKey.getBytes(StandardCharsets.UTF_8);
        if (utf8Bytes.length >= 32) {
            return utf8Bytes;
        }
        // If shorter than 32 bytes, hash with SHA-256
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(utf8Bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
