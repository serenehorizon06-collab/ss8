package com.example.library.controller;

import com.example.library.dto.BookCreateDTO;
import com.example.library.dto.BookUpdateStockDTO;
import com.example.library.entity.Book;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Book> createBook(@ModelAttribute BookCreateDTO dto) throws IOException {
        Book book = bookService.createBook(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.findBookById(id);
        return ResponseEntity.ok(book);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id,
                                           @Valid @RequestBody BookUpdateStockDTO dto) {
        Book book = bookService.updateBook(id, dto);
        return ResponseEntity.ok(book);
    }
}
