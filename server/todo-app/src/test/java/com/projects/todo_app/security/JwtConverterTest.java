package com.projects.todo_app.security;

import com.projects.todo_app.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

// Plain unit test — JwtConverter has no dependencies, so no Spring context or database is needed.
class JwtConverterTest {

    private static final String SECRET = "test-jwt-secret-key-with-32-char";
    private static final String OTHER_SECRET = "another-secret-key-also-32-chars";
    private static final String EMAIL = "testOne@email.com";

    private final JwtConverter converter = new JwtConverter(SECRET);
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    @Test
    void shouldRoundTripUser() {
        String token = converter.getTokenFromUser(new User(EMAIL));

        User user = converter.getUserFromToken("Bearer " + token);
        assertNotNull(user);
        assertEquals(EMAIL, user.getEmail());
    }

    @Test
    void shouldExpireInTwentyFourHours() {
        String token = converter.getTokenFromUser(new User(EMAIL));

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        long expectedMillis = System.currentTimeMillis() + 24L * 60 * 60 * 1000;
        long actualMillis = claims.getExpiration().getTime();
        // allow a minute of slack for test execution time
        assertTrue(Math.abs(expectedMillis - actualMillis) < 60_000);
    }

    @Test
    void shouldNotAcceptNullToken() {
        assertNull(converter.getUserFromToken(null));
    }

    @Test
    void shouldNotAcceptTokenWithoutBearerPrefix() {
        String token = converter.getTokenFromUser(new User(EMAIL));
        assertNull(converter.getUserFromToken(token));
    }

    @Test
    void shouldNotAcceptTokenSignedWithDifferentSecret() {
        String forged = new JwtConverter(OTHER_SECRET).getTokenFromUser(new User(EMAIL));
        assertNull(converter.getUserFromToken("Bearer " + forged));
    }

    @Test
    void shouldNotAcceptTamperedToken() {
        // a JWT is header.payload.signature — swap in another user's payload but keep the original signature
        String[] original = converter.getTokenFromUser(new User(EMAIL)).split("\\.");
        String[] other = converter.getTokenFromUser(new User("testTwo@email.com")).split("\\.");
        String tampered = original[0] + "." + other[1] + "." + original[2];

        assertNull(converter.getUserFromToken("Bearer " + tampered));
    }

    @Test
    void shouldNotAcceptExpiredToken() {
        String expired = Jwts.builder()
                .setIssuer("bug-todo")
                .setSubject(EMAIL)
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key)
                .compact();

        assertNull(converter.getUserFromToken("Bearer " + expired));
    }

    @Test
    void shouldNotAcceptWrongIssuer() {
        String wrongIssuer = Jwts.builder()
                .setIssuer("someone-else")
                .setSubject(EMAIL)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();

        assertNull(converter.getUserFromToken("Bearer " + wrongIssuer));
    }

    @Test
    void shouldNotAcceptGarbage() {
        assertNull(converter.getUserFromToken("Bearer not.a.jwt"));
    }
}
