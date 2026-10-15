package com.example.p2p;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

enum Role { BORROWER, INVESTOR }
enum LoanStatus { OPEN, FUNDED, OVERDUE }

@Entity
class UserAccount {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false) String name;
    @Column(nullable=false,unique=true) String email;
    @Column(nullable=false) String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false) Role role;
    @Column(nullable=false,precision=19,scale=2) BigDecimal balance=BigDecimal.ZERO;
    @Version Long version;
    protected UserAccount() {}
    UserAccount(String name,String email,String passwordHash,Role role,BigDecimal balance){this.name=name;this.email=email;this.passwordHash=passwordHash;this.role=role;this.balance=balance;}
}

@Entity
class Loan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) UserAccount borrower;
    @Column(nullable=false) String title;
    @Column(nullable=false,length=1000) String description;
    @Column(nullable=false,precision=19,scale=2) BigDecimal principal;
    @Column(nullable=false,precision=19,scale=2) BigDecimal funded=BigDecimal.ZERO;
    @Column(nullable=false,precision=5,scale=2) BigDecimal annualRate;
    @Column(nullable=false) LocalDate dueDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false) LoanStatus status=LoanStatus.OPEN;
    @Version Long version;
    protected Loan() {}
    Loan(UserAccount borrower,String title,String description,BigDecimal principal,BigDecimal annualRate,LocalDate dueDate){this.borrower=borrower;this.title=title;this.description=description;this.principal=principal;this.annualRate=annualRate;this.dueDate=dueDate;}
}

@Entity
class Investment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) UserAccount investor;
    @ManyToOne(optional=false) Loan loan;
    @Column(nullable=false,precision=19,scale=2) BigDecimal amount;
    @Column(nullable=false) LocalDate createdOn=LocalDate.now();
    protected Investment() {}
    Investment(UserAccount investor,Loan loan,BigDecimal amount){this.investor=investor;this.loan=loan;this.amount=amount;}
}
