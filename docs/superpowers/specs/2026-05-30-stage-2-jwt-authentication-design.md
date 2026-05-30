# Stage 2: JWT 认证改造 — 设计规格

**日期**: 2026-05-30
**状态**: 已批准
**目标**: 实现 JWT 无状态认证，替换现有 Spring Security Session/Form 登录

---

## 1. 背景

### 1.1 当前状态（Stage 1 完成后）

- 所有 Controller 已改为 `@RestController`，返回 JSON
- `AuthController` 手动验证用户名密码，但返回 `token=null`（占位）
- `SecurityConfig` 仍使用旧式 `formLogin()` + `logout()` + Session 认证
- `SecurityUtil` 通过 `SecurityContextHolder.getContext().getAuthentication()` 获取用户名后查 DB
- 无 JWT 依赖，`security/` 包不存在

### 1.2 改造目标

- 添加 jjwt 依赖，实现 JWT Token 生成/验证/解析
- 创建 `JwtAuthenticationFilter`（OncePerRequestFilter），从 Authorization Header 提取并验证 Token
- 改造 `SecurityConfig`：移除 formLogin/logout，启用 STATELESS 会话，注册 JWT Filter
- 改造 `AuthController.login()`：登录成功后返回含真实 JWT Token 的响应
- 改造 `SecurityUtil`：从 JWT Authentication 中解析当前用户，减少 DB 查询
- 401/403 返回 JSON（而非重定向或 HTML）

---

## 2. 设计决策

| 决策项 | 选择 | 原因 |
|--------|------|------|
| JWT 方案 | 手动 JWT Filter + Spring Security 无状态 | 标准实践，课程项目适配度最佳，不引入 OAuth2 复杂性 |
| Token 有效期 | 2 小时 | 安全与便利性平衡 |
| Token 刷新 | 不实现 | 课程项目够用，过期后前端跳转登录页 |
| 旧 formLogin | 完全移除 | 与前后端分离、全 REST API 目标一致 |
| jjwt 版本 | 0.12.x | 最新稳定版，支持 Builder API，Java 17 兼容 |

---

## 3. 架构设计

### 3.1 认证流程

```
POST /api/auth/login
  → AuthController (验证用户名/密码)
    → JwtTokenProvider.generateToken(userId, username, roles)
      → 返回 { token, username, roles }

后续请求:
  → JwtAuthenticationFilter (OncePerRequestFilter)
    → 从 Authorization: Bearer <token> 提取
      → JwtTokenProvider.validateToken(token)
        → JwtTokenProvider.parseToken(token) → userId, username, roles
          → 构建 UsernamePasswordAuthenticationToken
            → 设置 SecurityContextHolder
              → Controller 通过 SecurityUtil.getCurrentUser() 获取

401 响应:
  → JwtAuthenticationEntryPoint → JSON { code: 401, msg: "未登录或 Token 已过期" }

403 响应:
  → JwtAccessDeniedHandler → JSON { code: 403, msg: "无权限访问" }
```

### 3.2 组件依赖关系

```
JwtTokenProvider (生成/验证/解析)
  ├── 依赖 JwtConfig (secret, expiration)
  ├── 被 AuthController 使用 (生成 Token)
  └── 被 JwtAuthenticationFilter 使用 (验证/解析)

JwtAuthenticationFilter (OncePerRequestFilter)
  ├── 依赖 JwtTokenProvider
  ├── 依赖 UserMapper (可选，加载完整 User 时)
  └── 被 SecurityConfig 注册到过滤器链

SecurityUtil
  └── 从 SecurityContextHolder 获取 Authentication
      └── 解析 userId → 查 DB (回退方案)

SecurityConfig
  ├── 注入 JwtTokenProvider, JwtAuthenticationEntryPoint, JwtAccessDeniedHandler
  ├── 构建 SecurityFilterChain: STATELESS + JWT Filter + 权限规则 + 异常处理
  └── 提供 PasswordEncoder Bean
```

---

## 4. 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|----------|------|
| 修改 | `pom.xml` | 添加 `jjwt-api`, `jjwt-impl`, `jjwt-jackson` 依赖 |
| 新增 | `src/main/java/com/petmgt/config/JwtConfig.java` | JWT 配置属性 (secret, expiration=7200000ms) |
| 新增 | `src/main/java/com/petmgt/security/JwtTokenProvider.java` | Token 生成/验证/解析核心类 |
| 新增 | `src/main/java/com/petmgt/security/JwtAuthenticationFilter.java` | OncePerRequestFilter，从 Header 提取 Token |
| 新增 | `src/main/java/com/petmgt/security/JwtAuthenticationEntryPoint.java` | 401 JSON 响应 |
| 新增 | `src/main/java/com/petmgt/security/JwtAccessDeniedHandler.java` | 403 JSON 响应 |
| 修改 | `src/main/java/com/petmgt/config/SecurityConfig.java` | 移除 formLogin/logout，启用 STATELESS，注册 JWT Filter |
| 修改 | `src/main/java/com/petmgt/controller/AuthController.java` | 注入 JwtTokenProvider，登录成功返回 JWT |
| 修改 | `src/main/java/com/petmgt/util/SecurityUtil.java` | 从 JWT Authentication 直接获取 userId |

---

## 5. 详细设计

### 5.1 pom.xml — 新增依赖

```xml
<!-- JWT -->
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

### 5.2 JwtConfig

```java
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret = "pet-mgt-jwt-secret-key-2026-this-is-a-long-enough-key-for-hs256";
    private long expiration = 7200000; // 2 hours
    // getters/setters
}
```

在 `PetMgtApplication` 或独立 `@Configuration` 启用 `@EnableConfigurationProperties(JwtConfig.class)`。

### 5.3 JwtTokenProvider

核心方法：

| 方法 | 签名 | 说明 |
|------|------|------|
| `generateToken` | `String generateToken(Long userId, String username, List<String> roles)` | 生成 JWT，包含 sub, userId, roles, iat, exp |
| `validateToken` | `boolean validateToken(String token)` | 验证签名和过期时间 |
| `getUserId` | `Long getUserId(String token)` | 从 Token 解析 userId |
| `getUsername` | `String getUsername(String token)` | 从 Token 解析 username |
| `getRoles` | `List<String> getRoles(String token)` | 从 Token 解析 roles |

使用 jjwt 0.12.x Builder API：
```java
Jwts.builder()
    .subject(username)
    .claim("userId", userId)
    .claim("roles", roles)
    .issuedAt(new Date())
    .expiration(new Date(System.currentTimeMillis() + expiration))
    .signWith(key)
    .compact();
```

密钥使用 `Keys.hmacShaKeyFor(secret.getBytes())`。

### 5.4 JwtAuthenticationFilter

```
extend OncePerRequestFilter
  doFilterInternal:
    1. 从 request.getHeader("Authorization") 提取 Token
    2. 若 Header 为空或不以 "Bearer " 开头 → 放行 (交给后续过滤器)
    3. 去掉 "Bearer " 前缀 → token
    4. jwtTokenProvider.validateToken(token) → 无效则放行
    5. 解析 userId, username, roles
    6. 构建 UserDetails: setUsername + setAuthorities(roles)
    7. 构建 UsernamePasswordAuthenticationToken
    8. 设置 SecurityContextHolder.getContext().setAuthentication(...)
    9. filterChain.doFilter(request, response)
```

注意：不在此过滤器中查 DB（避免每次请求都查库）。JWT 本身包含足够角色信息。

### 5.5 JwtAuthenticationEntryPoint

```java
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token已过期\",\"data\":null}");
    }
}
```

### 5.6 JwtAccessDeniedHandler

```java
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(403);
        response.getWriter().write("{\"code\":403,\"msg\":\"无权限访问\",\"data\":null}");
    }
}
```

### 5.7 SecurityConfig 改造

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // 公开端点
            .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/pets/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/breeds").permitAll()
            .requestMatchers("/api/home").permitAll()
            .requestMatchers("/uploads/**").permitAll()
            .requestMatchers("/error").permitAll()
            // 管理后台 → 管理员
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            // 其他 API → 需要登录
            .requestMatchers("/api/**").authenticated()
            // 非 API 路径 (静态资源等)
            .anyRequest().permitAll()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

要点：
- 移除 `.formLogin()` 和 `.logout()`
- 添加 `.csrf().disable()`（REST API 不需要 CSRF）
- 添加 `.sessionManagement().sessionCreationPolicy(STATELESS)`
- 添加 `.exceptionHandling()` 配置 JSON 响应
- 添加 JWT Filter 到过滤器链

### 5.8 AuthController 改造

注入 `JwtTokenProvider`，修改 `login()` 方法：

```java
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
```

### 5.9 SecurityUtil 改造

优先从 JWT Authentication 解析 userId，回退到旧逻辑：

```java
public static User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) return null;
    // 优先从 JWT Authentication 获取
    if (auth instanceof UsernamePasswordAuthenticationToken && auth.getPrincipal() instanceof Long) {
        Long userId = (Long) auth.getPrincipal();
        return userMapper.selectById(userId);
    }
    // 回退：用户名查 DB
    String username = auth.getName();
    if ("anonymousUser".equals(username)) return null;
    return userMapper.findByUsername(username);
}
```

**重要设计决定**：JWT Filter 构建的 `UsernamePasswordAuthenticationToken` 中，将 `userId` 作为 principal（而非 username 字符串）。这样 `SecurityUtil` 可以直接通过 `auth.getPrincipal()` 获取 userId 进行 DB 查询，避免模糊的用户名匹配。

---

## 6. 安全性验证

| 场景 | 预期结果 |
|------|----------|
| 无 Token 访问 `/api/user/profile` | 401 `{"code":401,"msg":"未登录或Token已过期"}` |
| 过期 Token 访问受保护端点 | 401（过期 Token 视为无效） |
| 伪造/篡改 Token | 401（签名验证失败） |
| 普通用户 Token 访问 `/api/admin/stats` | 403 `{"code":403,"msg":"无权限访问"}` |
| 管理员 Token 访问 `/api/admin/stats` | 200 正常返回数据 |
| 公开端点无 Token 访问 | 200（permitAll） |
| 登录成功返回 Token | Token 结构含 sub, userId, roles, iat, exp |

---

## 7. JWT Filter 与 @PreAuthorize 兼容性

由于 JwtAuthenticationFilter 在 `UsernamePasswordAuthenticationFilter` 之前设置 SecurityContext（包含 roles），Spring Security 的方法级权限注解（`@PreAuthorize`, `hasRole` 等）均可正常工作。

**注意事项**：
- JWT Filter 不查 DB，角色信息直接来自 Token
- Token 签发后角色变更不会生效（如需立即生效，需重新登录获取新 Token）
- 这在课程项目中是可接受的权衡

---

*文档版本：v1.0 | 日期：2026-05-30*
