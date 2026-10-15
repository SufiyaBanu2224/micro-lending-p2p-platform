package com.example.p2p;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

record RegisterRequest(@NotBlank String name,@Email @NotBlank String email,@Size(min=8) String password,@NotNull Role role) {}
record LoginRequest(@Email @NotBlank String email,@NotBlank String password) {}
record AuthResponse(String token,String name,String email,String role) {}
record LoanRequest(@NotBlank String title,@NotBlank String description,@DecimalMin("1.00") BigDecimal principal,@DecimalMin("0.00") @DecimalMax("100.00") BigDecimal annualRate,@Future LocalDate dueDate) {}
record InvestRequest(@DecimalMin("1.00") BigDecimal amount) {}
record LoanView(Long id,String title,String description,BigDecimal principal,BigDecimal funded,BigDecimal annualRate,LocalDate dueDate,String status,String borrower) {
    static LoanView from(Loan l){return new LoanView(l.id,l.title,l.description,l.principal,l.funded,l.annualRate,l.dueDate,l.status.name(),l.borrower.name);}
}
record AccountView(String name,String email,String role,BigDecimal balance) { static AccountView from(UserAccount a){return new AccountView(a.name,a.email,a.role.name(),a.balance);} }
