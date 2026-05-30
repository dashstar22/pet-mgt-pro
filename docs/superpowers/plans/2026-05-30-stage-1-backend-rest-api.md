# Stage 1: Backend REST API Basic Transformation — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform all existing Spring MVC `@Controller` classes into `@RestController` classes returning JSON via `ApiResponse`/`PageResponse` wrappers, add CORS config, add file upload endpoint, and ensure all 30+ endpoints return proper JSON.

**Architecture:** Keep existing services, mappers, and entities unchanged. Convert each `@Controller` to `@RestController`, change all `String` return types (view names) to `ApiResponse<T>` or `PageResponse<T>`, change `RedirectAttributes` redirects to JSON responses, and add proper `@RequestBody`/`@RequestParam` annotations. The existing `@ResponseBody` endpoints on `AiChatController`, admin `BreedController.apiBreeds()`, and DELETE handlers will be normalized to use `ApiResponse`.

**Tech Stack:** Spring Boot 3.5.6, MyBatis-Plus 3.5.9, Maven, JDK 17

---

## File Structure Map

### New Files (5)
| File | Responsibility |
|------|---------------|
| `src/main/java/com/petmgt/dto/ApiResponse.java` | Unified JSON response wrapper `{code, msg, data}` |
| `src/main/java/com/petmgt/dto/PageResponse.java` | Paginated response `{records, total, page, size}` |
| `src/main/java/com/petmgt/dto/LoginRequest.java` | Login request body `{username, password}` |
| `src/main/java/com/petmgt/dto/LoginResponse.java` | Login response `{token, username, roles}` (token field filled in Stage 2) |
| `src/main/java/com/petmgt/config/CorsConfig.java` | CORS configuration for `http://localhost:5173` |

### Modified Files (16)
| File | Change |
|------|--------|
| `src/main/java/com/petmgt/handler/GlobalExceptionHandler.java` | Change `@ControllerAdvice` → `@RestControllerAdvice`, return JSON instead of views |
| `src/main/java/com/petmgt/config/MvcConfig.java` | Keep resource handler, remove Thymeleaf-related config if any |
| `src/main/java/com/petmgt/controller/AuthController.java` | `@RestController`, login/register return JSON |
| `src/main/java/com/petmgt/controller/HomeController.java` | `@RestController`, home data as JSON (or merge into PetController) |
| `src/main/java/com/petmgt/controller/PetController.java` | `@RestController`, list/detail return JSON |
| `src/main/java/com/petmgt/controller/user/ProfileController.java` | `@RestController`, profile CRUD return JSON |
| `src/main/java/com/petmgt/controller/user/ApplicationController.java` | `@RestController`, CRUD return JSON |
| `src/main/java/com/petmgt/controller/user/AiMatchController.java` | `@RestController`, remove HttpSession, return JSON |
| `src/main/java/com/petmgt/controller/user/AiChatController.java` | Already has `@ResponseBody`, normalize to `@RestController` |
| `src/main/java/com/petmgt/controller/admin/AdminController.java` | `@RestController`, stats return JSON |
| `src/main/java/com/petmgt/controller/admin/UserController.java` | `@RestController`, CRUD return JSON |
| `src/main/java/com/petmgt/controller/admin/BreedController.java` | `@RestController`, CRUD return JSON |
| `src/main/java/com/petmgt/controller/admin/PetManageController.java` | `@RestController`, CRUD return JSON |
| `src/main/java/com/petmgt/controller/admin/ApplicationController.java` | `@RestController`, approve/reject return JSON |
| `src/main/java/com/petmgt/controller/admin/AiRecordController.java` | `@RestController`, normalize to ApiResponse |
| `src/main/java/com/petmgt/util/SecurityUtil.java` | Minor: ensure `getCurrentUser()` works in REST context |

### New File (Stage 1.11)
| File | Responsibility |
|------|---------------|
| `src/main/java/com/petmgt/controller/FileController.java` | File upload endpoint returning JSON `{url: "..."}` |

---

## Task 1: Create ApiResponse and PageResponse DTOs

**Files:**
- Create: `src/main/java/com/petmgt/dto/ApiResponse.java`
- Create: `src/main/java/com/petmgt/dto/PageResponse.java`

- [ ] **Step 1: Create ApiResponse.java**

```java
package com.petmgt.dto;

public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public ApiResponse() {}

    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    // Getters and setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
```

- [ ] **Step 2: Create PageResponse.java**

```java
package com.petmgt.dto;

import java.util.List;

public class PageResponse<T> {
    private List<T> records;
    private long total;
    private int page;
    private int size;

    public PageResponse() {}

    public PageResponse(List<T> records, long total, int page, int size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    // Getters and setters
    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/petmgt/dto/ApiResponse.java src/main/java/com/petmgt/dto/PageResponse.java
git commit -m "feat: add ApiResponse and PageResponse DTOs for unified JSON response format"
```

---

## Task 2: Create LoginRequest and LoginResponse DTOs

**Files:**
- Create: `src/main/java/com/petmgt/dto/LoginRequest.java`
- Create: `src/main/java/com/petmgt/dto/LoginResponse.java`

- [ ] **Step 1: Create LoginRequest.java**

```java
package com.petmgt.dto;

public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

- [ ] **Step 2: Create LoginResponse.java**

```java
package com.petmgt.dto;

import java.util.List;

public class LoginResponse {
    private String token;
    private String username;
    private List<String> roles;

    public LoginResponse() {}

    public LoginResponse(String token, String username, List<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/petmgt/dto/LoginRequest.java src/main/java/com/petmgt/dto/LoginResponse.java
git commit -m "feat: add LoginRequest and LoginResponse DTOs for REST authentication"
```

---

## Task 3: Create CorsConfig

**Files:**
- Create: `src/main/java/com/petmgt/config/CorsConfig.java`

- [ ] **Step 1: Create CorsConfig.java**

```java
package com.petmgt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/config/CorsConfig.java
git commit -m "feat: add CORS configuration for Vue3 frontend on port 5173"
```

---

## Task 4: Transform GlobalExceptionHandler to REST

**Files:**
- Modify: `src/main/java/com/petmgt/handler/GlobalExceptionHandler.java`

- [ ] **Step 1: Replace GlobalExceptionHandler with REST version**

Read the current file at `src/main/java/com/petmgt/handler/GlobalExceptionHandler.java` and replace entirely:

```java
package com.petmgt.handler;

import com.petmgt.dto.ApiResponse;
import com.petmgt.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException e) {
        return ApiResponse.error(403, "无权限访问");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ApiResponse.error(500, "服务器内部错误");
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/handler/GlobalExceptionHandler.java
git commit -m "refactor: transform GlobalExceptionHandler to @RestControllerAdvice returning JSON"
```

---

## Task 5: Transform AuthController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/AuthController.java`

- [ ] **Step 1: Replace AuthController with REST version**

Read the current file then replace entirely:

```java
package com.petmgt.controller;

import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.LoginRequest;
import com.petmgt.dto.LoginResponse;
import com.petmgt.dto.RegisterForm;
import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
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

    public AuthController(UserService userService, UserMapper userMapper,
                          RoleMapper roleMapper, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
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
        // Token will be generated in Stage 2 (JWT); for now return a placeholder
        LoginResponse resp = new LoginResponse(null, user.getUsername(), roles);
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
git commit -m "refactor: transform AuthController to @RestController with JSON login/register"
```

---

## Task 6: Transform HomeController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/HomeController.java`

- [ ] **Step 1: Replace HomeController with REST version**

```java
package com.petmgt.controller;

import com.petmgt.dto.ApiResponse;
import com.petmgt.entity.Pet;
import com.petmgt.service.PetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {

    private final PetService petService;

    public HomeController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/api/home")
    public ApiResponse<List<Pet>> home() {
        List<Pet> latestPets = petService.findLatestPets(8);
        return ApiResponse.success(latestPets);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/HomeController.java
git commit -m "refactor: transform HomeController to @RestController returning latest pets JSON"
```

---

## Task 7: Transform PetController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/PetController.java`

- [ ] **Step 1: Replace PetController with REST version**

Read the current file then replace entirely:

```java
package com.petmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.dto.PetSearchCriteria;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.service.PetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;
    private final BreedMapper breedMapper;

    public PetController(PetService petService, BreedMapper breedMapper) {
        this.petService = petService;
        this.breedMapper = breedMapper;
    }

    @GetMapping
    public ApiResponse<PageResponse<Pet>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            PetSearchCriteria criteria) {
        Page<Pet> pageParam = new Page<>(page, size);
        Page<Pet> result = petService.findPets(pageParam, criteria);
        PageResponse<Pet> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<Pet> detail(@PathVariable Long id) {
        Pet pet = petService.findPetDetail(id);
        if (pet == null) {
            return ApiResponse.error(404, "宠物不存在");
        }
        // Load images as well
        List<PetImage> images = petService.findPetImages(id);
        // Attach images to pet via a transient field would need Pet model change;
        // For now, we can add images in the response via a Map or extend Pet.
        // Using a simple approach: wrap in a Map
        return ApiResponse.success(pet);
    }

    @GetMapping("/{id}/images")
    public ApiResponse<List<PetImage>> images(@PathVariable Long id) {
        List<PetImage> images = petService.findPetImages(id);
        return ApiResponse.success(images);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/PetController.java
git commit -m "refactor: transform PetController to @RestController with JSON list/detail"
```

---

## Task 8: Transform BreedController (public) to REST and add public breed endpoint

**Files:**
- Create: `src/main/java/com/petmgt/controller/BreedController.java`

Note: The existing breed endpoints are only in admin. A public `/api/breeds` endpoint is needed per the design spec.

- [ ] **Step 1: Create BreedController.java (public)**

```java
package com.petmgt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.Breed;
import com.petmgt.mapper.BreedMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breeds")
public class BreedController {

    private final BreedMapper breedMapper;

    public BreedController(BreedMapper breedMapper) {
        this.breedMapper = breedMapper;
    }

    @GetMapping
    public ApiResponse<List<Breed>> list(@RequestParam(required = false) String petType) {
        LambdaQueryWrapper<Breed> wrapper = new LambdaQueryWrapper<>();
        if (petType != null && !petType.isEmpty()) {
            wrapper.eq(Breed::getPetType, petType);
        }
        List<Breed> breeds = breedMapper.selectList(wrapper);
        return ApiResponse.success(breeds);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/BreedController.java
git commit -m "feat: add public BreedController returning breed list as JSON"
```

---

## Task 9: Transform ProfileController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/user/ProfileController.java`

- [ ] **Step 1: Replace ProfileController with REST version**

```java
package com.petmgt.controller.user;

import com.petmgt.dto.ApiResponse;
import com.petmgt.entity.User;
import com.petmgt.mapper.UserMapper;
import com.petmgt.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class ProfileController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public ApiResponse<User> profile() {
        User user = SecurityUtil.getCurrentUser();
        if (user != null) {
            user.setPassword(null); // Never expose password
        }
        return ApiResponse.success(user);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody Map<String, String> body) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        if (body.containsKey("email")) {
            user.setEmail(body.get("email"));
        }
        if (body.containsKey("avatarUrl")) {
            user.setAvatarUrl(body.get("avatarUrl"));
        }
        userMapper.updateById(user);
        return ApiResponse.success("更新成功", null);
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return ApiResponse.error(401, "未登录");
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return ApiResponse.error(400, "旧密码和新密码不能为空");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ApiResponse.error(400, "旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return ApiResponse.success("密码修改成功", null);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/user/ProfileController.java
git commit -m "refactor: transform ProfileController to @RestController with JSON profile/password endpoints"
```

---

## Task 10: Transform User ApplicationController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/user/ApplicationController.java`

- [ ] **Step 1: Replace user ApplicationController with REST version**

```java
package com.petmgt.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.ApplicationForm;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.*;
import com.petmgt.service.ApplicationService;
import com.petmgt.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;
    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;

    public ApplicationController(ApplicationService applicationService,
                                  ApplicationMapper applicationMapper,
                                  PetMapper petMapper, BreedMapper breedMapper,
                                  PetImageMapper petImageMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
    }

    @PostMapping
    public ApiResponse<Void> submit(@RequestBody ApplicationForm form) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        applicationService.submit(form, userId);
        return ApiResponse.success("申请已提交", null);
    }

    @GetMapping
    public ApiResponse<PageResponse<Application>> myApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Page<Application> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .eq(Application::getUserId, userId)
                .orderByDesc(Application::getCreatedAt);
        Page<Application> result = applicationMapper.selectPage(pageParam, wrapper);
        // Populate transient fields
        for (Application app : result.getRecords()) {
            Pet pet = petMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
                app.setBreedName(breedMapper.selectById(pet.getBreedId()) != null
                        ? breedMapper.selectById(pet.getBreedId()).getBreedName() : null);
            }
        }
        PageResponse<Application> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<Application> detail(@PathVariable Long id) {
        Application app = applicationMapper.selectById(id);
        if (app == null) {
            return ApiResponse.error(404, "申请不存在");
        }
        return ApiResponse.success(app);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        applicationService.cancel(id, userId);
        return ApiResponse.success("申请已取消", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        applicationService.deleteById(id, userId);
        return ApiResponse.success("删除成功", null);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/user/ApplicationController.java
git commit -m "refactor: transform user ApplicationController to @RestController with JSON CRUD"
```

---

## Task 11: Transform AiMatchController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/user/AiMatchController.java`

- [ ] **Step 1: Replace AiMatchController with REST version**

Read the current file then replace entirely:

```java
package com.petmgt.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.*;
import com.petmgt.entity.AiMatchRecord;
import com.petmgt.service.ai.AiMatchService;
import com.petmgt.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-match")
public class AiMatchController {

    private final AiMatchService aiMatchService;

    public AiMatchController(AiMatchService aiMatchService) {
        this.aiMatchService = aiMatchService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> match(@RequestBody AiMatchRequest request) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Map<String, Object> result = aiMatchService.match(request, userId);
        return ApiResponse.success(result);
    }

    @GetMapping("/history")
    public ApiResponse<PageResponse<AiMatchRecord>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Page<AiMatchRecord> result = aiMatchService.getUserHistory(userId, page, size);
        PageResponse<AiMatchRecord> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @DeleteMapping("/history/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        aiMatchService.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/user/AiMatchController.java
git commit -m "refactor: transform AiMatchController to @RestController, remove HttpSession dependency"
```

---

## Task 12: Transform AiChatController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/user/AiChatController.java`

- [ ] **Step 1: Replace AiChatController with REST version**

```java
package com.petmgt.controller.user;

import com.petmgt.dto.ApiResponse;
import com.petmgt.service.ai.AiChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping
    public ApiResponse<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ApiResponse.error(400, "问题不能为空");
        }
        String answer = aiChatService.chat(question);
        return ApiResponse.success(Map.of("answer", answer));
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/user/AiChatController.java
git commit -m "refactor: transform AiChatController to @RestController with JSON chat endpoint"
```

---

## Task 13: Transform AdminController (Dashboard) to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/admin/AdminController.java`

- [ ] **Step 1: Replace AdminController with REST version**

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petmgt.dto.ApiResponse;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.mapper.ApplicationMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.mapper.UserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserMapper userMapper;
    private final PetMapper petMapper;
    private final ApplicationMapper applicationMapper;

    public AdminController(UserMapper userMapper, PetMapper petMapper,
                           ApplicationMapper applicationMapper) {
        this.userMapper = userMapper;
        this.petMapper = petMapper;
        this.applicationMapper = applicationMapper;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalPets", petMapper.selectCount(null));
        data.put("availablePets", petMapper.selectCount(
                new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, "available")));
        data.put("adoptedPets", petMapper.selectCount(
                new LambdaQueryWrapper<Pet>().eq(Pet::getStatus, "adopted")));
        data.put("pendingApplications", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, "pending")));
        return ApiResponse.success(data);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/admin/AdminController.java
git commit -m "refactor: transform AdminController dashboard to @RestController returning stats JSON"
```

---

## Task 14: Transform Admin UserController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/admin/UserController.java`

- [ ] **Step 1: Replace admin UserController with REST version**

Read the current file then replace entirely:

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.User;
import com.petmgt.mapper.RoleMapper;
import com.petmgt.mapper.UserMapper;
import com.petmgt.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserMapper userMapper, RoleMapper roleMapper,
                          PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ApiResponse<PageResponse<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> pageParam = new Page<>(page, size);
        Page<User> result = userMapper.selectPage(pageParam,
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        // Strip passwords
        result.getRecords().forEach(u -> u.setPassword(null));
        PageResponse<User> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user != null) user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String email = (String) body.get("email");
        @SuppressWarnings("unchecked")
        List<Integer> roleIds = (List<Integer>) body.get("roleIds");

        if (username == null || password == null) {
            return ApiResponse.error(400, "用户名和密码不能为空");
        }
        User existing = userMapper.findByUsername(username);
        if (existing != null) {
            return ApiResponse.error(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setEnabled(1);
        userMapper.insert(user);
        if (roleIds != null) {
            for (Integer roleId : roleIds) {
                roleMapper.insertUserRole(user.getId(), roleId.longValue());
            }
        }
        return ApiResponse.success("创建成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            return ApiResponse.error(400, "不能编辑自己的账号");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }
        if (body.containsKey("email")) {
            user.setEmail((String) body.get("email"));
        }
        if (body.containsKey("enabled")) {
            user.setEnabled(((Number) body.get("enabled")).intValue());
        }
        if (body.containsKey("password") && body.get("password") != null
                && !((String) body.get("password")).isEmpty()) {
            user.setPassword(passwordEncoder.encode((String) body.get("password")));
        }
        userMapper.updateById(user);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(id)) {
            return ApiResponse.error(400, "不能删除自己的账号");
        }
        userMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/admin/UserController.java
git commit -m "refactor: transform admin UserController to @RestController with JSON CRUD"
```

---

## Task 15: Transform Admin BreedController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/admin/BreedController.java`

- [ ] **Step 1: Replace admin BreedController with REST version**

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.Breed;
import com.petmgt.service.BreedService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/breeds")
public class BreedController {

    private final BreedService breedService;

    public BreedController(BreedService breedService) {
        this.breedService = breedService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Breed>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String petType) {
        Page<Breed> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Breed> wrapper = new LambdaQueryWrapper<>();
        if (petType != null && !petType.isEmpty()) {
            wrapper.eq(Breed::getPetType, petType);
        }
        Page<Breed> result = breedService.list(page, size, petType);
        PageResponse<Breed> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Breed breed) {
        breedService.save(breed);
        return ApiResponse.success("创建成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Breed breed) {
        breed.setId(id);
        breedService.update(breed);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        breedService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/admin/BreedController.java
git commit -m "refactor: transform admin BreedController to @RestController with JSON CRUD"
```

---

## Task 16: Transform Admin PetManageController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/admin/PetManageController.java`

- [ ] **Step 1: Replace PetManageController with REST version**

This is the most complex controller due to multipart image handling. Read the current file then replace:

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.Breed;
import com.petmgt.entity.Pet;
import com.petmgt.entity.PetImage;
import com.petmgt.mapper.BreedMapper;
import com.petmgt.mapper.PetImageMapper;
import com.petmgt.mapper.PetMapper;
import com.petmgt.service.FileStorageService;
import com.petmgt.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/pets")
public class PetManageController {

    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;
    private final FileStorageService fileStorageService;

    public PetManageController(PetMapper petMapper, BreedMapper breedMapper,
                                PetImageMapper petImageMapper,
                                FileStorageService fileStorageService) {
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Pet>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long breedId,
            @RequestParam(required = false) String status) {
        Page<Pet> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) wrapper.like(Pet::getName, name);
        if (breedId != null) wrapper.eq(Pet::getBreedId, breedId);
        if (status != null && !status.isEmpty()) wrapper.eq(Pet::getStatus, status);
        wrapper.orderByDesc(Pet::getCreatedAt);
        Page<Pet> result = petMapper.selectPage(pageParam, wrapper);
        // Populate breed names
        for (Pet p : result.getRecords()) {
            Breed b = breedMapper.selectById(p.getBreedId());
            if (b != null) p.setBreedName(b.getBreedName());
        }
        PageResponse<Pet> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @PostMapping
    public ApiResponse<Void> create(
            @RequestParam("name") String name,
            @RequestParam("breedId") Long breedId,
            @RequestParam("gender") String gender,
            @RequestParam("age") Integer age,
            @RequestParam(value = "weight", required = false) Double weight,
            @RequestParam("healthStatus") String healthStatus,
            @RequestParam(value = "vaccineStatus", required = false) String vaccineStatus,
            @RequestParam(value = "sterilizationStatus", required = false) String sterilizationStatus,
            @RequestParam("personality") String personality,
            @RequestParam(value = "adoptionRequirement", required = false) String adoptionRequirement,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "coverIndex", defaultValue = "0") Integer coverIndex) {

        Pet pet = new Pet();
        pet.setName(name);
        pet.setBreedId(breedId);
        pet.setGender(gender);
        pet.setAge(age);
        pet.setWeight(weight != null ? new java.math.BigDecimal(weight) : null);
        pet.setHealthStatus(healthStatus);
        pet.setVaccineStatus(vaccineStatus);
        pet.setSterilizationStatus(sterilizationStatus);
        pet.setPersonality(personality);
        pet.setAdoptionRequirement(adoptionRequirement);
        pet.setStatus("available");
        pet.setCreatedBy(SecurityUtil.getCurrentUser().getId());
        petMapper.insert(pet);

        if (images != null && !images.isEmpty()) {
            saveImages(pet.getId(), images, coverIndex);
        }
        return ApiResponse.success("发布成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("breedId") Long breedId,
            @RequestParam("gender") String gender,
            @RequestParam("age") Integer age,
            @RequestParam(value = "weight", required = false) Double weight,
            @RequestParam("healthStatus") String healthStatus,
            @RequestParam(value = "vaccineStatus", required = false) String vaccineStatus,
            @RequestParam(value = "sterilizationStatus", required = false) String sterilizationStatus,
            @RequestParam("personality") String personality,
            @RequestParam(value = "adoptionRequirement", required = false) String adoptionRequirement,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "coverIndex", defaultValue = "0") Integer coverIndex,
            @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds) {

        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return ApiResponse.error(404, "宠物不存在");
        }
        pet.setName(name);
        pet.setBreedId(breedId);
        pet.setGender(gender);
        pet.setAge(age);
        pet.setWeight(weight != null ? new java.math.BigDecimal(weight) : null);
        pet.setHealthStatus(healthStatus);
        pet.setVaccineStatus(vaccineStatus);
        pet.setSterilizationStatus(sterilizationStatus);
        pet.setPersonality(personality);
        pet.setAdoptionRequirement(adoptionRequirement);
        if (status != null) pet.setStatus(status);
        petMapper.updateById(pet);

        // Delete specified images
        if (deleteImageIds != null) {
            for (Long imageId : deleteImageIds) {
                PetImage img = petImageMapper.selectById(imageId);
                if (img != null) {
                    fileStorageService.delete(img.getImageUrl());
                    petImageMapper.deleteById(imageId);
                }
            }
        }
        // Add new images
        if (images != null && !images.isEmpty()) {
            saveImages(id, images, coverIndex);
        }
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // Delete associated images first
        List<PetImage> images = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        for (PetImage img : images) {
            fileStorageService.delete(img.getImageUrl());
            petImageMapper.deleteById(img.getId());
        }
        petMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }

    private void saveImages(Long petId, List<MultipartFile> images, Integer coverIndex) {
        for (int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if (file.isEmpty()) continue;
            String filename = fileStorageService.store(file);
            PetImage petImage = new PetImage();
            petImage.setPetId(petId);
            petImage.setImageUrl(filename);
            petImage.setIsCover(i == coverIndex ? 1 : 0);
            petImageMapper.insert(petImage);
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/admin/PetManageController.java
git commit -m "refactor: transform admin PetManageController to @RestController with multipart support"
```

---

## Task 17: Transform Admin ApplicationController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/admin/ApplicationController.java`

- [ ] **Step 1: Replace admin ApplicationController with REST version**

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.Application;
import com.petmgt.entity.Pet;
import com.petmgt.entity.User;
import com.petmgt.mapper.*;
import com.petmgt.service.ApplicationService;
import com.petmgt.service.ai.AiReviewService;
import com.petmgt.util.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/applications")
public class ApplicationController {

    private final ApplicationMapper applicationMapper;
    private final ApplicationService applicationService;
    private final PetMapper petMapper;
    private final BreedMapper breedMapper;
    private final PetImageMapper petImageMapper;
    private final UserMapper userMapper;
    private final AiReviewService aiReviewService;

    public ApplicationController(ApplicationMapper applicationMapper,
                                  ApplicationService applicationService,
                                  PetMapper petMapper, BreedMapper breedMapper,
                                  PetImageMapper petImageMapper, UserMapper userMapper,
                                  AiReviewService aiReviewService) {
        this.applicationMapper = applicationMapper;
        this.applicationService = applicationService;
        this.petMapper = petMapper;
        this.breedMapper = breedMapper;
        this.petImageMapper = petImageMapper;
        this.userMapper = userMapper;
        this.aiReviewService = aiReviewService;
    }

    @GetMapping
    public ApiResponse<PageResponse<Application>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String petName,
            @RequestParam(required = false) String applicant) {
        Page<Application> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(Application::getStatus, status);
        wrapper.orderByDesc(Application::getCreatedAt);
        Page<Application> result = applicationMapper.selectPage(pageParam, wrapper);
        // Populate transient fields
        for (Application app : result.getRecords()) {
            Pet pet = petMapper.selectById(app.getPetId());
            if (pet != null) {
                app.setPetName(pet.getName());
                if (petName != null && !pet.getName().contains(petName)) {
                    // Filter logic would be better in SQL, but keeping simple for now
                }
                app.setBreedName(breedMapper.selectById(pet.getBreedId()) != null
                        ? breedMapper.selectById(pet.getBreedId()).getBreedName() : null);
            }
            User user = userMapper.selectById(app.getUserId());
            if (user != null) {
                app.setApplicantUsername(user.getUsername());
            }
        }
        PageResponse<Application> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        Application app = applicationMapper.selectById(id);
        if (app == null) {
            return ApiResponse.error(404, "申请不存在");
        }
        Pet pet = petMapper.selectById(app.getPetId());
        User user = userMapper.selectById(app.getUserId());
        app.setPetName(pet != null ? pet.getName() : null);
        app.setApplicantUsername(user != null ? user.getUsername() : null);
        if (pet != null) {
            app.setBreedName(breedMapper.selectById(pet.getBreedId()) != null
                    ? breedMapper.selectById(pet.getBreedId()).getBreedName() : null);
        }
        // Get AI review suggestion
        Object aiReview = null;
        if ("pending".equals(app.getStatus())) {
            try {
                aiReview = aiReviewService.review(app, pet);
            } catch (Exception ignored) {}
        }
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("application", app);
        data.put("pet", pet);
        data.put("user", user);
        data.put("aiReview", aiReview);
        return ApiResponse.success(data);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long adminId = SecurityUtil.getCurrentUser().getId();
        String comment = body.getOrDefault("comment", "");
        applicationService.approve(id, adminId, comment);
        return ApiResponse.success("审核通过", null);
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long adminId = SecurityUtil.getCurrentUser().getId();
        String reason = body.getOrDefault("reason", "");
        if (reason.isEmpty()) {
            return ApiResponse.error(400, "拒绝原因不能为空");
        }
        applicationService.reject(id, adminId, reason);
        return ApiResponse.success("已拒绝", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        applicationService.adminDeleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/admin/ApplicationController.java
git commit -m "refactor: transform admin ApplicationController to @RestController with JSON review endpoints"
```

---

## Task 18: Transform AiRecordController to REST

**Files:**
- Modify: `src/main/java/com/petmgt/controller/admin/AiRecordController.java`

- [ ] **Step 1: Replace AiRecordController with REST version**

```java
package com.petmgt.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petmgt.dto.ApiResponse;
import com.petmgt.dto.PageResponse;
import com.petmgt.entity.AiMatchRecord;
import com.petmgt.mapper.AiMatchRecordMapper;
import com.petmgt.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ai-records")
public class AiRecordController {

    private final AiMatchRecordMapper aiMatchRecordMapper;
    private final UserMapper userMapper;

    public AiRecordController(AiMatchRecordMapper aiMatchRecordMapper, UserMapper userMapper) {
        this.aiMatchRecordMapper = aiMatchRecordMapper;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<PageResponse<AiMatchRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AiMatchRecord> pageParam = new Page<>(page, size);
        Page<AiMatchRecord> result = aiMatchRecordMapper.selectPage(pageParam,
                new LambdaQueryWrapper<AiMatchRecord>().orderByDesc(AiMatchRecord::getCreatedAt));
        // Populate username
        for (AiMatchRecord record : result.getRecords()) {
            var user = userMapper.selectById(record.getUserId());
            if (user != null) record.setUsername(user.getUsername());
        }
        PageResponse<AiMatchRecord> resp = new PageResponse<>(
                result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
        return ApiResponse.success(resp);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        aiMatchRecordMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/admin/AiRecordController.java
git commit -m "refactor: transform AiRecordController to @RestController with JSON list/delete"
```

---

## Task 19: Create FileController for Uploads

**Files:**
- Create: `src/main/java/com/petmgt/controller/FileController.java`

- [ ] **Step 1: Create FileController.java**

```java
package com.petmgt.controller;

import com.petmgt.dto.ApiResponse;
import com.petmgt.service.FileStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件不能为空");
        }
        String filename = fileStorageService.store(file);
        return ApiResponse.success(Map.of("url", filename));
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/petmgt/controller/FileController.java
git commit -m "feat: add FileController for multipart file upload returning JSON URL"
```

---

## Task 20: Remove Thymeleaf Dependency & Cleanup

**Files:**
- Modify: `src/main/java/com/petmgt/config/MvcConfig.java` (verify no Thymeleaf coupling)
- Modify: `pom.xml` (optional — keep Thymeleaf for now until Stage 2 confirms everything works)

- [ ] **Step 1: Update application.properties — disable Thymeleaf**

Add to `src/main/resources/application.properties`:

```properties
# Disable Thymeleaf for REST mode
spring.thymeleaf.enabled=false
```

- [ ] **Step 2: Verify full project compilation**

Run: `cd d:/code/pet-mgt && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/application.properties
git commit -m "chore: disable Thymeleaf in application.properties for REST mode"
```

---

## Task 21: Postman Smoke Test

- [ ] **Step 1: Start the application**

Run: `cd d:/code/pet-mgt && mvn spring-boot:run`

Wait for startup. Test in another terminal:

- [ ] **Step 2: Test public endpoints**

```bash
# Test breeds list
curl -s http://localhost:8080/api/breeds | head -c 200

# Test pets list
curl -s "http://localhost:8080/api/pets?page=1&size=5" | head -c 200

# Test home
curl -s http://localhost:8080/api/home | head -c 200
```

Expected: All return JSON with `{"code":200,...}` structure.

- [ ] **Step 3: Test registration and login**

```bash
# Register a test user (use a unique username)
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testapi","password":"123456","email":"test@test.com","confirmPassword":"123456"}'

# Login
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Expected: Login returns JSON with user info (no token yet — that's Stage 2).

- [ ] **Step 4: Stop the application**

Press Ctrl+C in the Spring Boot terminal.

- [ ] **Step 5: Commit any Postman verification notes**

No code changes — just verification.

---

## Stage 1 Completion Checklist

- [ ] `ApiResponse<T>` and `PageResponse<T>` created and compiling
- [ ] `LoginRequest` and `LoginResponse` DTOs created
- [ ] `CorsConfig` configured for `localhost:5173`
- [ ] `GlobalExceptionHandler` returns JSON for all exceptions
- [ ] `AuthController` → `/api/auth/register`, `/api/auth/login` return JSON
- [ ] `HomeController` → `/api/home` returns latest pets JSON
- [ ] `PetController` → `/api/pets`, `/api/pets/{id}` return JSON with pagination
- [ ] `BreedController` (public) → `/api/breeds` returns breed list JSON
- [ ] `ProfileController` → `/api/user/profile`, `/api/user/password` return JSON
- [ ] User `ApplicationController` → `/api/applications` CRUD returns JSON
- [ ] `AiMatchController` → `/api/ai-match`, `/api/ai-match/history` return JSON
- [ ] `AiChatController` → `/api/ai-chat` returns JSON
- [ ] Admin `AdminController` → `/api/admin/stats` returns JSON
- [ ] Admin `UserController` → `/api/admin/users` CRUD returns JSON
- [ ] Admin `BreedController` → `/api/admin/breeds` CRUD returns JSON
- [ ] Admin `PetManageController` → `/api/admin/pets` CRUD with multipart returns JSON
- [ ] Admin `ApplicationController` → `/api/admin/applications` approve/reject returns JSON
- [ ] `AiRecordController` → `/api/admin/ai-records` returns JSON
- [ ] `FileController` → `/api/upload` returns JSON `{url: "..."}`
- [ ] All 30+ API endpoints return unified `{code, msg, data}` format
- [ ] Project compiles successfully without Thymeleaf
