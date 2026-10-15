package com.example.p2p;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface UserAccountRepository extends JpaRepository<UserAccount,Long> { Optional<UserAccount> findByEmail(String email); }
interface LoanRepository extends JpaRepository<Loan,Long> {}
interface InvestmentRepository extends JpaRepository<Investment,Long> {}
