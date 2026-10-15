package com.example.p2p;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api")
class ApiController {
    private final PlatformService service; private final JwtService jwt;
    ApiController(PlatformService service,JwtService jwt){this.service=service;this.jwt=jwt;}
    @PostMapping("/auth/register") @ResponseStatus(HttpStatus.CREATED) AuthResponse register(@Valid @RequestBody RegisterRequest r){var u=service.register(r);return new AuthResponse(jwt.issue(u),u.name,u.email,u.role.name());}
    @PostMapping("/auth/login") AuthResponse login(@Valid @RequestBody LoginRequest r){var u=service.authenticate(r);return new AuthResponse(jwt.issue(u),u.name,u.email,u.role.name());}
    @GetMapping("/loans") java.util.List<LoanView> loans(){return service.listLoans();}
    @PostMapping("/loans") @ResponseStatus(HttpStatus.CREATED) LoanView create(Authentication auth,@Valid @RequestBody LoanRequest r){return service.createLoan(auth.getName(),r);}
    @PostMapping("/loans/{id}/invest") LoanView invest(Authentication auth,@PathVariable Long id,@Valid @RequestBody InvestRequest r){return service.invest(auth.getName(),id,r);}
    @GetMapping("/accounts/me") AccountView account(Authentication auth){return service.account(auth.getName());}
    @GetMapping("/health") Map<String,String> health(){return Map.of("status","ok");}
}

@org.springframework.web.bind.annotation.RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(OptimisticLockingFailureException.class) @ResponseStatus(HttpStatus.CONFLICT) Map<String,String> conflict(){return Map.of("error","Concurrent account update. Please retry.");}
}
