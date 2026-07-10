# security — 安全实现规范（JWT + RBAC）

## 目标

定义 EvoLib 项目中 JWT 认证 + RBAC 角色授权的全链路实现标准，覆盖后端（Filter / SecurityConfig / 注解）和前端（Pinia / Axios / 路由守卫）。

---

## 一、后端安全实现

### 1.1 核心类清单

| 类 | 位置 | 职责 |
| --- | --- | --- |
| `JwtTokenProvider` | `com.evolib.security` | 生成 JWT / 解析 JWT / 校验 JWT 有效性 |
| `JwtAuthenticationFilter` | `com.evolib.security` | 拦截请求，从 `Authorization` 头提取 token，注入 SecurityContext |
| `UserPrincipal` | `com.evolib.security` | 安全上下文主体（含 readerId + role + 权限列表） |
| `SecurityConfig` | `com.evolib.config` | Spring Security 配置：路径白名单/角色限制/CORS/无状态会话 |

### 1.2 JwtTokenProvider 实现要点

```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secret;                    // 签名密钥，从环境变量 JWT_SECRET 注入，禁止硬编码
    
    @Value("${jwt.expiration}")
    private long expiration;                  // 有效期，application.yml 中配置 28800000ms = 8h
    
    // 生成 JWT —
    //   把 readerId 和 role 写入 token 的 claims（自定义载荷），
    //   这样后续 Filter 解析 token 时就能拿到角色信息，无需再查数据库
    public String generateToken(String readerId, String role) {
        return Jwts.builder()
            .setSubject(readerId)
            .claim("role", role)                  // ★ 关键：角色写入 token，后续直接读取
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
            .compact();
    }
    
    // 从 token 中解析 readerId
    public String getReaderId(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secret.getBytes())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
    
    // 校验 token 是否有效（未过期 + 签名正确）
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secret.getBytes()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;  // 签名错误 / 过期 / 格式不对 → 统一返回 false
        }
    }
}
```

### 1.3 JwtAuthenticationFilter 实现要点

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                     HttpServletResponse response,
                                     FilterChain chain) {
        // 1. 从请求头取出 "Authorization: Bearer xxx"
        String token = extractToken(request);
        
        // 2. 校验 token 有效性 → 构造 UserPrincipal → 注入 SecurityContext
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String readerId = jwtTokenProvider.getReaderId(token);
            String role = jwtTokenProvider.getRole(token);
            
            UserPrincipal principal = new UserPrincipal(readerId, role);
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(auth);
            // ★ 此后，Controller 中可以通过 @PreAuthorize("hasRole('ROLE_CIRCULATION')") 做角色校验
        }
        
        chain.doFilter(request, response);
    }
    
    // 从请求头提取 Bearer token
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);  // 跳过 "Bearer " 前缀
        }
        return null;
    }
}
```

### 1.4 SecurityConfig 配置要点

```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)  // ★ 开启 @PreAuthorize 注解支持
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    private final JwtAuthenticationFilter jwtFilter;
    
    @Override
    protected void configure(HttpSecurity http) {
        http
            .csrf().disable()                      // 前后端分离，禁用 CSRF
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 无状态，不创建 session
            .and()
            .authorizeRequests()
                // 公开路径 — 任何人可访问
                .antMatchers("/api/v1/auth/login").permitAll()
                .antMatchers("/static/**", "/index.html", "/favicon.ico").permitAll()
                
                // 读者 — 图书检索（ROLE_READER 及以上均可）
                .antMatchers(HttpMethod.GET, "/api/v1/books/**").hasAnyRole("READER", "CIRCULATION", "ADMIN")
                
                // 馆员 — 借还书 + 读者管理
                .antMatchers("/api/v1/borrow-records/**").hasRole("CIRCULATION")
                .antMatchers(HttpMethod.POST, "/api/v1/readers").hasRole("CIRCULATION")
                .antMatchers(HttpMethod.PUT, "/api/v1/readers/*/phone").hasRole("CIRCULATION")
                .antMatchers(HttpMethod.GET, "/api/v1/readers/*/borrows").hasRole("CIRCULATION")
                
                // 管理员 — 图书上架/下架/重置密码
                .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // 兜底：其他所有请求需登录
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
```

### 1.5 Controller 层权限注解

```java
// 方式一：类级别 + 方法级别
@RestController
@RequestMapping("/api/v1/borrow-records")
@PreAuthorize("hasRole('ROLE_CIRCULATION')")          // ★ 整个 Controller 需要馆员角色
public class BorrowController {
    
    @PostMapping
    public Result<BorrowResponse> borrow(@RequestBody BorrowRequest request) {
        // 继承类级别的 @PreAuthorize
    }
}

// 方式二：方法级别覆盖
@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_READER', 'ROLE_CIRCULATION', 'ROLE_ADMIN')")
    public Result<PageResult<BookDTO>> search(...) {  // 读者也可以查
    }
    
    @GetMapping("/{isbn}")
    @PreAuthorize("hasAnyRole('ROLE_READER', 'ROLE_CIRCULATION', 'ROLE_ADMIN')")
    public Result<BookDTO> detail(...) {}
}
```

---

## 二、前端安全实现

### 2.1 Pinia 认证 Store

```javascript
// stores/auth.js — 见 fe-standards.md 第五节完整模板
async function login(readerId, password) {
    const resp = await api.post('/auth/login', { readerId, password });
    if (resp.code === 0) {
        token.value = resp.data.token;            // 保存 JWT
        role.value  = resp.data.role;             // 保存角色
        localStorage.setItem('evolib_token', token.value);  // 持久化 → 刷新页面不丢
        localStorage.setItem('evolib_role', role.value);
        router.push(homePath.value);              // 跳转角色对应首页
    }
}
```

### 2.2 Axios 拦截器

```javascript
// utils/api.js — 见 fe-standards.md 第六节完整模板

// 请求拦截 — 每次发请求自动带上 JWT
http.interceptors.request.use(config => {
    const auth = useAuthStore();
    if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`;
    return config;
});

// 响应拦截 — token 过期 → 自动登出
http.interceptors.response.use(
    resp => resp.data,
    error => {
        if (error.response?.status === 401) useAuthStore().logout();
        if (error.response?.status === 403) ElMessage.error('无权限访问');
        return Promise.reject(error);
    }
);
```

### 2.3 路由守卫

```javascript
// router/index.js — 见 fe-standards.md 第四节完整模板

router.beforeEach((to, from, next) => {
    if (to.meta.role === false) return next();     // 公开页面
    if (!authStore.isLoggedIn) return next('/login'); // 未登录
    if (to.meta.role && !authStore.hasRole(...)) ...  // 角色不符
});
```

---

## 三、前端安全规则

1. **JWT secret 只能存在于后端**，前端只持有 token 字符串，不持有 secret
2. **token 只能通过登录接口获取**，前端不允许硬编码或伪造 token
3. **token 过期（401）→ 立即清空 localStorage 并跳转登录页**
4. **无权限（403）→ 提示「无权限访问」**，不跳转，让用户知道自己越权了
5. **所有密码必须 BCrypt 加密后存储**，数据库 `password_hash` 字段不可逆
