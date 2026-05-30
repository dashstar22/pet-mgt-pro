# Stage 2: JWT Authentication Transformation — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement JWT stateless authentication to replace Spring Security Session/Form login across the pet adoption platform.

**Architecture:** Manual JWT Filter (OncePerRequestFilter) integrated with Spring Security stateless session. JwtTokenProvider handles token generation/validation/parsing using jjwt 0.12.6. JwtAuthenticationFilter extracts Bearer token from Authorization header, validates it, and sets SecurityContext. AuthController returns real JWT on login. SecurityUtil adapts to parse userId from JWT Authentication principal.

**Tech Stack:** Spring Boot 3.5.6, Spring Security 6.x, jjwt 0.12.6, MyBatis-Plus 3.5.9, JDK 17

---

## File Structure Map

### New Files (6)
| File | Responsibility |
|------|---------------|
| `src/main/java/com/petmgt/config/JwtConfig.java` | `@ConfigurationProperties` for `jwt.secret` and `jwt.expiration` |
| `src/main/java/com/petmgt/security/JwtTokenProvider.java` | Generate, validate, and parse JWT tokens (HS256) |
| `src/main/java/com/petmgt/security/JwtAuthenticationFilter.java` | `OncePerRequestFilter` — extract Bearer token, validate, set SecurityContext |
| `src/main/java/com/petmgt/security/JwtAuthenticationEntryPoint.java` | Returns 401 JSON when unauthenticated |
| `src/main/java/com/petmgt/security/JwtAccessDeniedHandler.java` | Returns 403 JSON when access denied |
| `src/main/java/com/petmgt/config/JwtPropertiesConfig.java` | Enable `@ConfigurationProperties` for `JwtConfig` |

### Modified Files (5)
| File | Change |
|------|--------|
| `pom.xml` | Add jjwt-api, jjwt-impl, jjwt-jackson 0.12.6 dependencies |
| `src/main/resources/application.properties` | Add `jwt.secret` and `jwt.expiration` properties |
| `src/main/java/com/petmgt/config/SecurityConfig.java` | Remove formLogin/logout, add STATELESS session, JWT filter, exception handlers, update URL rules |
| `src/main/java/com/petmgt/controller/AuthController.java` | Inject JwtTokenProvider, return real JWT on login |
| `src/main/java/com/petmgt/util/SecurityUtil.java` | Parse userId from JWT principal, fallback to username lookup |

---

## Task 1: Add jjwt Dependencies to pom.xml

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add jjwt dependencies**

Add the following inside the `<dependencies>` block, after the `<!-- Lombok -->` dependency block:

```xml
        <!-- JWT (jjwt 0.12.x) -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.6</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.6</version>
            <scope>runtime</scope>
        </dependency>
```

- [ ] **Step 2: Verify Maven resolves new dependencies**

Run: `cd d:/code/pet-mgt && mvn dependency:resolve -q`
Expected: BUILD SUCCESS (no errors about missing jjwt artifacts)

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "feat(jwt): add jjwt 0.12.6 dependencies"
```

---

## Task 2: Create JwtConfig and Enable Configuration Properties

**Files:**
- Create: `src/main/java/com/petmgt/config/JwtConfig.java`
- Create: `src/main/java/com/petmgt/config/JwtPropertiesConfig.java`
- Modify: `src/main/resources/application.properties`

- [ ] **Step 1: Create JwtConfig.java**

```java
package com.petmgt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret = "pet-mgt-jwt-secret-key-2026-this-is-a-long-enough-key-for-hs256";
    private long expiration = 7200000; // 2 hours in milliseconds

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}
```

- [ ] **Step 2: Create JwtPropertiesConfig.java to enable the configuration properties**

```java
package com.petmgt.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtConfig.class)
public class JwtPropertiesConfig {
}
```

- [ ] **Step 3: Add JWT properties to application.properties**

Add the following to the end of `src/main/resources/application.properties`:

```properties
# JWT Configuration
jwt.secret=pet-mgt-jwt-secret-key-2026-this-is-a-long-enough-key-for-hs256
jwt.expiration=7200000
```

- [ ] **Step 4: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/petmgt/config/JwtConfig.java src/main/java/com/petmgt/config/JwtPropertiesConfig.java src/main/resources/application.properties
git commit -m "feat(jwt): add JwtConfig with secret and expiration properties"
```

---

## Task 3: Create JwtTokenProvider

**Files:**
- Create: `src/main/java/com/petmgt/security/JwtTokenProvider.java`

- [ ] **Step 1: Create the security package directory and JwtTokenProvider.java**

Create the directory: `src/main/java/com/petmgt/security/` (if it doesn't exist).

Then create the file:

```java
package com.petmgt.security;

import com.petmgt.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private final long expiration;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expiration = jwtConfig.getExpiration();
    }

    /**
     * Generate a JWT token for the given user.
     */
    public String generateToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Validate a JWT token. Returns true if the token is valid and not expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Get username (subject) from token.
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Get userId from token.
     */
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * Get roles from token.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return parseClaims(token).get("roles", List.class);
    }

    /**
     * Parse claims from token. Assumes token has already been validated.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/security/JwtTokenProvider.java
git commit -m "feat(jwt): add JwtTokenProvider for token generation, validation, and parsing"
```

---

## Task 4: Create JwtAuthenticationEntryPoint and JwtAccessDeniedHandler

**Files:**
- Create: `src/main/java/com/petmgt/security/JwtAuthenticationEntryPoint.java`
- Create: `src/main/java/com/petmgt/security/JwtAccessDeniedHandler.java`

- [ ] **Step 1: Create JwtAuthenticationEntryPoint.java**

```java
package com.petmgt.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token已过期\",\"data\":null}");
    }
}
```

- [ ] **Step 2: Create JwtAccessDeniedHandler.java**

```java
package com.petmgt.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"code\":403,\"msg\":\"无权限访问\",\"data\":null}");
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/petmgt/security/JwtAuthenticationEntryPoint.java src/main/java/com/petmgt/security/JwtAccessDeniedHandler.java
git commit -m "feat(jwt): add JwtAuthenticationEntryPoint (401) and JwtAccessDeniedHandler (403) returning JSON"
```

---

## Task 5: Create JwtAuthenticationFilter

**Files:**
- Create: `src/main/java/com/petmgt/security/JwtAuthenticationFilter.java`

- [ ] **Step 1: Create JwtAuthenticationFilter.java**

```java
package com.petmgt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserId(token);
            String username = jwtTokenProvider.getUsername(token);
            List<String> roles = jwtTokenProvider.getRoles(token);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority(
                            role.startsWith("ROLE_") ? role : "ROLE_" + role))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            // Store username in details for logging/debugging purposes
            authentication.setDetails(username);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract Bearer token from Authorization header.
     * Returns null if no valid Authorization header is found.
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/security/JwtAuthenticationFilter.java
git commit -m "feat(jwt): add JwtAuthenticationFilter to extract and validate Bearer token from requests"
```

---

## Task 6: Transform SecurityConfig to JWT Stateless Mode

**Files:**
- Modify: `src/main/java/com/petmgt/config/SecurityConfig.java`

- [ ] **Step 1: Replace SecurityConfig.java entirely**

```java
package com.petmgt.config;

import com.petmgt.security.JwtAccessDeniedHandler;
import com.petmgt.security.JwtAuthenticationEntryPoint;
import com.petmgt.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API
            .csrf(csrf -> csrf.disable())
            // Stateless session — no HTTP sessions created or used
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                // Public read-only endpoints
                .requestMatchers(HttpMethod.GET, "/api/pets/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/breeds").permitAll()
                .requestMatchers("/api/home").permitAll()
                // Static resources and uploads
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/error").permitAll()
                // Admin endpoints require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                // Non-API paths (catch-all)
                .anyRequest().permitAll()
            )
            // Exception handling — return JSON instead of redirect
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            // Register JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/config/SecurityConfig.java
git commit -m "refactor(jwt): transform SecurityConfig to JWT stateless mode, remove formLogin/logout"
```

---

## Task 7: Transform AuthController to Return Real JWT

**Files:**
- Modify: `src/main/java/com/petmgt/controller/AuthController.java`

- [ ] **Step 1: Replace AuthController.java to inject JwtTokenProvider and return real JWT**

```java
package com.petmgt.controller;

import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.LoginRequest;
import com.petmgt.dto.LoginResponse;
import com.petmgt.dto.RegisterForm;
import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
import com.petmgt.security.JwtTokenProvider;
import com.petmgt.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, UserMapper userMapper,
                          RoleMapper roleMapper, PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterForm form) {
        userService.register(form);
        return ApiResponse.success("注册成功，请登录", null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error(401, "用户名或密码错误");
        }
        if (user.getEnabled() != null && user.getEnabled() == 0) {
            return ApiResponse.error(403, "账号已被禁用");
        }
        List<String> roles = roleMapper.findRoleNamesByUserId(user.getId());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);
        LoginResponse resp = new LoginResponse(token, user.getUsername(), roles);
        return ApiResponse.success(resp);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/AuthController.java
git commit -m "feat(jwt): inject JwtTokenProvider in AuthController, return real JWT on login"
```

---

## Task 8: Transform SecurityUtil for JWT Compatibility

**Files:**
- Modify: `src/main/java/com/petmgt/util/SecurityUtil.java`

- [ ] **Step 1: Replace SecurityUtil.java to handle JWT principal (userId as Long)**

```java
package com.petmgt.util;

import com.petmgt.entity.User;
import com.petmgt.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private static UserMapper userMapper;

    public SecurityUtil(UserMapper userMapper) {
        SecurityUtil.userMapper = userMapper;
    }

    /**
     * Get the currently authenticated user.
     * With JWT auth, the principal is the userId (Long).
     * Falls back to username-based lookup for non-JWT Authentication types.
     */
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        // JWT auth: principal is userId (Long)
        if (principal instanceof Long userId) {
            return userMapper.selectById(userId);
        }

        // Fallback: principal might be a String (username) or UserDetails
        String username = auth.getName();
        if ("anonymousUser".equals(username)) {
            return null;
        }
        return userMapper.findByUsername(username);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/util/SecurityUtil.java
git commit -m "refactor(jwt): adapt SecurityUtil to parse userId from JWT authentication principal"
```

---

## Task 9: Full Project Compilation Verification

- [ ] **Step 1: Clean compile the entire project**

Run: `cd d:/code/pet-mgt && mvn clean compile -q`
Expected: BUILD SUCCESS with no errors or warnings

- [ ] **Step 2: Verify the security package files are all present**

Run: `ls src/main/java/com/petmgt/security/`
Expected output:
```
JwtAccessDeniedHandler.java
JwtAuthenticationEntryPoint.java
JwtAuthenticationFilter.java
JwtTokenProvider.java
```

- [ ] **Step 3: Commit (if any final adjustments needed)**

No commit needed if compilation passes cleanly.

---

## Task 10: Postman Smoke Test — API Authentication Verification

- [ ] **Step 1: Start the Spring Boot application**

Run: `cd d:/code/pet-mgt && mvn spring-boot:run`
Wait for "Started PetMgtApplication" message.

- [ ] **Step 2: Test public endpoint — GET /api/breeds (no token needed)**

Run in another terminal:
```bash
curl -s http://localhost:8080/api/breeds
```
Expected: Returns JSON with `"code":200` and breed list in `data`.

- [ ] **Step 3: Test public endpoint — GET /api/pets (no token needed)**

```bash
curl -s "http://localhost:8080/api/pets?page=1&size=5"
```
Expected: Returns JSON with `"code":200` and paginated pet list in `data`.

- [ ] **Step 4: Test login — POST /api/auth/login (obtain JWT token)**

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
Expected: Returns JSON with `"code":200` and `data.token` containing a JWT string (starts with `eyJ`). Save this token for subsequent tests.

- [ ] **Step 5: Test authenticated endpoint with valid token — GET /api/user/profile**

```bash
TOKEN="<paste-token-from-step-4>"
curl -s http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer $TOKEN"
```
Expected: Returns JSON with `"code":200` and user profile in `data`.

- [ ] **Step 6: Test 401 — access authenticated endpoint without token**

```bash
curl -s http://localhost:8080/api/user/profile
```
Expected: Returns `{"code":401,"msg":"未登录或Token已过期","data":null}`

- [ ] **Step 7: Test 401 with invalid/expired token**

```bash
curl -s http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer invalidtoken123"
```
Expected: Returns `{"code":401,"msg":"未登录或Token已过期","data":null}`

- [ ] **Step 8: Test admin endpoint with admin token — GET /api/admin/stats**

```bash
TOKEN="<paste-admin-token>"
curl -s http://localhost:8080/api/admin/stats \
  -H "Authorization: Bearer $TOKEN"
```
Expected: Returns JSON with `"code":200` and stats data (totalUsers, totalPets, etc.).

- [ ] **Step 9: Test login with invalid credentials**

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}'
```
Expected: Returns `{"code":401,"msg":"用户名或密码错误","data":null}`

- [ ] **Step 10: Test registration**

```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testjwtuser","password":"123456","email":"testjwt@test.com","confirmPassword":"123456"}'
```
Expected: Returns `"code":200` with success message.

- [ ] **Step 11: Test login with newly registered user and access authenticated endpoint**

```bash
# Login
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testjwtuser","password":"123456"}'

# Use the token to access authenticated endpoint
curl -s http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer <token-from-above>"
```
Expected: Both return 200. Second call returns the new user's profile.

- [ ] **Step 12: Stop the application**

Press Ctrl+C in the Spring Boot terminal.

- [ ] **Step 13: Commit test verification notes**

```bash
git add -A && git commit -m "test(jwt): complete Postman smoke test — all public, authenticated, and admin endpoints verified"
```

---

## Stage 2 Completion Checklist

- [ ] `pom.xml` includes jjwt-api, jjwt-impl, jjwt-jackson 0.12.6
- [ ] `JwtConfig` with `@ConfigurationProperties(prefix = "jwt")` for secret and expiration
- [ ] `JwtPropertiesConfig` enables configuration properties binding
- [ ] `application.properties` has `jwt.secret` and `jwt.expiration`
- [ ] `JwtTokenProvider` generates, validates, and parses JWT tokens (HS256)
- [ ] `JwtAuthenticationFilter` extracts Bearer token, validates, sets SecurityContext with userId as principal
- [ ] `JwtAuthenticationEntryPoint` returns 401 JSON
- [ ] `JwtAccessDeniedHandler` returns 403 JSON
- [ ] `SecurityConfig` uses STATELESS sessions, no formLogin/logout, JWT filter registered
- [ ] `SecurityConfig` URL rules: public GET endpoints open, admin needs ROLE_ADMIN, other API needs authentication
- [ ] `AuthController.login()` returns real JWT token in LoginResponse
- [ ] `SecurityUtil.getCurrentUser()` parses userId from JWT principal (Long), falls back to username lookup
- [ ] All 11 Postman smoke tests pass
- [ ] Project compiles successfully with `mvn clean compile`

---

*Plan version: v1.0 | Date: 2026-05-30 | Design spec: [2026-05-30-stage-2-jwt-authentication-design.md](../specs/2026-05-30-stage-2-jwt-authentication-design.md)*
