package com.example.p2p;

import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
class PlatformService {
    private final UserAccountRepository users; private final LoanRepository loans; private final InvestmentRepository investments; private final PasswordEncoder encoder;
    PlatformService(UserAccountRepository users,LoanRepository loans,InvestmentRepository investments,PasswordEncoder encoder){this.users=users;this.loans=loans;this.investments=investments;this.encoder=encoder;}
    @Transactional UserAccount register(RegisterRequest r){if(users.findByEmail(r.email()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");return users.save(new UserAccount(r.name(),r.email().toLowerCase(),encoder.encode(r.password()),r.role(),r.role()==Role.INVESTOR?new BigDecimal("5000.00"):BigDecimal.ZERO));}
    UserAccount authenticate(LoginRequest r){var u=users.findByEmail(r.email().toLowerCase()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password"));if(!encoder.matches(r.password(),u.passwordHash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");return u;}
    List<LoanView> listLoans(){return loans.findAll().stream().map(LoanView::from).toList();}
    @Transactional LoanView createLoan(String email,LoanRequest r){var borrower=users.findByEmail(email).orElseThrow();var loan=loans.save(new Loan(borrower,r.title(),r.description(),r.principal(),r.annualRate(),r.dueDate()));return LoanView.from(loan);}
    @Transactional LoanView invest(String email,Long id,InvestRequest r){
        if(r.amount()==null||r.amount().signum()<=0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Investment must be positive");
        var investor=users.findByEmail(email).orElseThrow();var loan=loans.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Loan not found"));
        if(loan.borrower.id.equals(investor.id))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cannot invest in your own loan");
        if(loan.status!=LoanStatus.OPEN)throw new ResponseStatusException(HttpStatus.CONFLICT,"Loan is not open");
        if(investor.balance.compareTo(r.amount())<0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient account balance");
        if(loan.funded.add(r.amount()).compareTo(loan.principal)>0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Investment exceeds remaining loan amount");
        investor.balance=investor.balance.subtract(r.amount());loan.funded=loan.funded.add(r.amount());if(loan.funded.compareTo(loan.principal)==0)loan.status=LoanStatus.FUNDED;
        try{investments.save(new Investment(investor,loan,r.amount()));users.saveAndFlush(investor);loans.saveAndFlush(loan);}catch(OptimisticLockingFailureException|OptimisticLockException ex){throw new ResponseStatusException(HttpStatus.CONFLICT,"Concurrent update. Please retry.");}
        return LoanView.from(loan);
    }
    AccountView account(String email){return AccountView.from(users.findByEmail(email).orElseThrow());}
    @Transactional int flagOverdue(){var all=loans.findAll();int n=0;for(var loan:all)if(loan.status==LoanStatus.OPEN&&loan.dueDate.isBefore(LocalDate.now())){loan.status=LoanStatus.OVERDUE;n++;}return n;}
}
