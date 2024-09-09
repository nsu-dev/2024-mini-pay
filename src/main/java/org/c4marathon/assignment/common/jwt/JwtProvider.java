package org.c4marathon.assignment.common.jwt;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.common.exception.runtime.CustomJwtException;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.exception.UserErrorCode;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {

	private static final String ROLE_KEY = "ROLE";
	private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 5L;
	private final UserRepository userRepository;
	@Value("${jwt.key}")
	private String key;
	private SecretKey secretKey;

	@PostConstruct
	private void initSecretKey() {
		this.secretKey = Keys.hmacShaKeyFor(key.getBytes());
	}

	public String createToken(User user, Date now) {
		Date expiredTime = createExpiredDateWithTokenType(now, TOKEN_EXPIRE_TIME);
		String authorities = user.getRole().getRole();

		return Jwts.builder()
			.subject(user.getEmail())
			.issuer(user.getName())
			.claim(ROLE_KEY, authorities)
			.issuedAt(now)
			.expiration(expiredTime)
			.signWith(secretKey, Jwts.SIG.HS512)
			.compact();
	}

	private Date createExpiredDateWithTokenType(Date now, long tokenExpireTime) {
		return new Date(now.getTime() + tokenExpireTime);
	}

	public boolean validateToken(String token, Date date) {
		if (!StringUtils.hasText(token)) {
			return false;
		}

		Claims claims = parseToken(token);
		return claims.getExpiration().after(date);
	}

	private Claims parseToken(String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build()
				.parseSignedClaims(token).getPayload();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		} catch (MalformedJwtException e) {
			throw new CustomJwtException(JwtErrorCode.MALFORMED_TOKEN);
		} catch (JwtException e) {
			throw new CustomJwtException(JwtErrorCode.INVALID_TOKEN);
		} catch (Exception e) {
			throw new CustomJwtException(JwtErrorCode.INVALID_TOKEN);
		}
	}

	public Authentication getAuthentication(String token) {
		Claims claims = parseToken(token);
		List<SimpleGrantedAuthority> authorities = getAuthorities(claims);

		String email = claims.getSubject();
		User principal = userRepository.findByEmail(email)
			.orElseThrow(() -> new BaseException(UserErrorCode.NOT_FOUND_USER));

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
		return Collections.singletonList(new SimpleGrantedAuthority(
			claims.get(ROLE_KEY).toString()
		));
	}
}
