package com.example.library.service;

import com.example.library.dto.BorrowCreateDTO;
import com.example.library.entity.Borrow;
import com.example.library.repository.BorrowRepository;
import org.springframework.stereotype.Service;

@Service
public class BorrowService {

    private final BorrowRepository borrowRepository;

    public BorrowService(BorrowRepository borrowRepository) {
        this.borrowRepository = borrowRepository;
    }

    public Borrow createBorrow(BorrowCreateDTO dto) {
        Borrow borrow = new Borrow();
        borrow.setUsername(dto.getUsername());
        borrow.setBookId(dto.getBookId());

        return borrowRepository.save(borrow);
    }
}
