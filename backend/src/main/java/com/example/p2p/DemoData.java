package com.example.p2p;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
class DemoData {
    @Bean CommandLineRunner seed(UserAccountRepository users,LoanRepository loans,PasswordEncoder encoder){return args->{
        if(users.count()>0)return;
        var borrower=users.save(new UserAccount("Asha Rai","borrower@demo.com",encoder.encode("Borrower123!"),Role.BORROWER,BigDecimal.ZERO));
        users.save(new UserAccount("Sam Investor","investor@demo.com",encoder.encode("Investor123!"),Role.INVESTOR,new BigDecimal("5000.00")));
        loans.save(new Loan(borrower,"Expand neighborhood bakery","Purchase a second commercial oven and hire two part-time staff.",new BigDecimal("8000.00"),new BigDecimal("8.50"),LocalDate.now().plusMonths(9)));
        loans.save(new Loan(borrower,"Solar panels for workshop","Install rooftop solar to reduce operating costs.",new BigDecimal("5000.00"),new BigDecimal("7.25"),LocalDate.now().plusMonths(12)));
    };}
}
