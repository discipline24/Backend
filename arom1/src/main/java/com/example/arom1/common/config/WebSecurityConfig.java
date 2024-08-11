package com.example.arom1.common.config;import com.example.arom1.common.filter.JwtAuthenticationFilter;import com.example.arom1.common.handler.OAuth2LoginSuccessHandler;import com.example.arom1.common.util.jwt.TokenProvider;import com.example.arom1.service.OAuth2Service;import com.example.arom1.service.RefreshTokenService;import lombok.RequiredArgsConstructor;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.security.authentication.AuthenticationManager;import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;import org.springframework.security.config.http.SessionCreationPolicy;import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.security.web.SecurityFilterChain;import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;@EnableWebSecurity@Configuration@RequiredArgsConstructorpublic class WebSecurityConfig {    private final OAuth2Service oAuth2Service;    private final TokenProvider tokenProvider;    private final RefreshTokenService refreshTokenService;    //인증 필요없는 url 작성    private static final String[] AUTH_WHITELIST = {            "/login", "/signup", "/login/**"    };    @Bean    public PasswordEncoder passwordEncoder(){        return new BCryptPasswordEncoder();    }    //Web configure 작성해서 특정 url 필터체인 작동안하게 설정    @Bean    public WebSecurityCustomizer configure() {        return (web) -> web.ignoring()                .requestMatchers(AUTH_WHITELIST);    }    @Bean    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {        return http                .authorizeHttpRequests(auth -> auth                        .requestMatchers(AUTH_WHITELIST).permitAll()                        .anyRequest().authenticated())                .formLogin(AbstractHttpConfigurer::disable)                //폼로그인 사용 X -> 폼로그인은 세션 방식에 적합//                .formLogin(formLogin -> formLogin//                        .loginPage("/login")//                        .permitAll()//                        .loginProcessingUrl("/login_proc")//                        .defaultSuccessUrl("/home")//                )//                .logout(logout -> logout//                        .logoutSuccessUrl("/login")//                        .invalidateHttpSession(true)//                )                //필터 추가 (위치 중요)                //로그인 필터 -> 로그인 서비스 메서드 변경 (이유: 예외 처리 수월 등)                //.addFilterAt(new JwtLoginFilter(authenticationManager(configuration), tokenProvider), UsernamePasswordAuthenticationFilter.class)                .addFilterBefore(new JwtAuthenticationFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class)                //csrf 설정                .csrf(AbstractHttpConfigurer::disable)                .sessionManagement((session) -> session                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))                .oauth2Login(oauth ->                        oauth.userInfoEndpoint(c -> c.userService(oAuth2Service))                                .successHandler(oAuth2LoginSuccessHandler()))                .build();    }    @Bean    public OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler() {        return new OAuth2LoginSuccessHandler(refreshTokenService);    }    @Bean    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {        return configuration.getAuthenticationManager();    }}