package com.example.p2p;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
class JwtService {
    private final byte[] key; private final long expiration;
    JwtService(@Value("${app.jwt.secret}") String secret,@Value("${app.jwt.expiration-ms}") long expiration){this.key=secret.getBytes(StandardCharsets.UTF_8);this.expiration=expiration;}
    String issue(UserAccount account){return Jwts.builder().subject(account.email).claim("role",account.role.name()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiration)).signWith(Keys.hmacShaKeyFor(key)).compact();}
    String email(String token){return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(key)).build().parseSignedClaims(token).getPayload().getSubject();}
}

@Component
class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt; private final UserAccountRepository users;
    JwtFilter(JwtService jwt,UserAccountRepository users){this.jwt=jwt;this.users=users;}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
        String h=req.getHeader("Authorization");
        if(h!=null&&h.startsWith("Bearer "))try{String email=jwt.email(h.substring(7));var user=users.findByEmail(email).orElse(null);if(user!=null){var auth=new UsernamePasswordAuthenticationToken(email,null,java.util.List.of(new SimpleGrantedAuthority("ROLE_"+user.role.name())));org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);}}catch(Exception ignored){org.springframework.security.core.context.SecurityContextHolder.clearContext();}
        chain.doFilter(req,res);
    }
}

@Configuration
class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean SecurityFilterChain filterChain(HttpSecurity http,JwtFilter jwt)throws Exception{
        return http.csrf(c->c.ignoringRequestMatchers("/h2-console/**").disable()).headers(h->h.frameOptions(f->f.sameOrigin()))
          .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->res.sendError(HttpStatus.UNAUTHORIZED.value())))
          .authorizeHttpRequests(a->a.requestMatchers("/api/auth/**","/api/health","/h2-console/**").permitAll().requestMatchers(org.springframework.http.HttpMethod.GET,"/api/loans").permitAll().requestMatchers(HttpMethodFor.POST,"/api/loans/*/invest").hasRole("INVESTOR").requestMatchers(HttpMethodFor.POST,"/api/loans").hasRole("BORROWER").anyRequest().authenticated())
          .addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
    }
    private static final org.springframework.http.HttpMethod HttpMethodFor=org.springframework.http.HttpMethod.POST;
}
