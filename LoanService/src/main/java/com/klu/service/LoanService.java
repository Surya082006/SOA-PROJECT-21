package com.klu.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.klu.entity.Loan;
import com.klu.repository.LoanRepository;

@Service
public class LoanService {

    private final LoanRepository repository;
    private final RestTemplate restTemplate;

    public LoanService(
            LoanRepository repository,
            RestTemplate restTemplate) {

        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // ==========================================
    // CREATE LOAN / BORROW BOOK
    // ==========================================

    public Loan createLoan(Loan loan) {

        String url = "http://BOOKSERVICE/books/"
                + loan.getBookId()
                + "/borrow";

        try {

            // Ask BookService to borrow the book
            restTemplate.put(url, null);

        } catch (HttpClientErrorException.Conflict ex) {

            // No copies available
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No copies available"
            );

        } catch (HttpClientErrorException.NotFound ex) {

            // Book does not exist
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book not found"
            );
        }

        // Book successfully borrowed
        loan.setStatus("BORROWED");

        // Save loan
        return repository.save(loan);
    }

    // ==========================================
    // GET ALL LOANS
    // ==========================================

    public List<Loan> getAllLoans() {

        return repository.findAll();
    }

    // ==========================================
    // GET LOAN BY ID
    // ==========================================

    public Loan getLoanById(Long id) {

        return repository.findById(id)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Loan not found"
                    )
                );
    }

    // ==========================================
    // GET LOANS BY USER
    // ==========================================

    public List<Loan> getLoansByUser(Long userId) {

        return repository.findByUserId(userId);
    }

    // ==========================================
    // GET LOANS BY BOOK
    // ==========================================

    public List<Loan> getLoansByBook(Long bookId) {

        return repository.findByBookId(bookId);
    }

    // ==========================================
    // RETURN BOOK
    // ==========================================

    public Loan returnBook(Long id) {

        Loan loan = getLoanById(id);

        // Check if already returned
        if ("RETURNED".equals(loan.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Book already returned"
            );
        }

        String url = "http://BOOKSERVICE/books/"
                + loan.getBookId()
                + "/return";

        try {

            // Tell BookService to increase available copies
            restTemplate.put(url, null);

        } catch (HttpClientErrorException.NotFound ex) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book not found"
            );
        }

        // Change loan status
        loan.setStatus("RETURNED");

        return repository.save(loan);
    }

    // ==========================================
    // DELETE LOAN
    // ==========================================

    public void deleteLoan(Long id) {

        if (!repository.existsById(id)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Loan not found"
            );
        }

        repository.deleteById(id);
    }
}