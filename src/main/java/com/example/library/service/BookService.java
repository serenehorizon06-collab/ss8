package com.example.library.service;

import com.example.library.dto.BookCreateDTO;
import com.example.library.dto.BookUpdateStockDTO;
import com.example.library.entity.Book;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class BookService {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(BookCreateDTO dto) throws IOException {
        MultipartFile coverImage = dto.getCoverImage();
        String coverUrl = saveCoverImage(coverImage);

        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setStock(dto.getStock());
        book.setCoverUrl(coverUrl);

        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookUpdateStockDTO dto) {
        Book book = findBookById(id);

        book.setStock(dto.getStock());

        return bookRepository.save(book);
    }

    public Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    private String saveCoverImage(MultipartFile coverImage) throws IOException {
        Files.createDirectories(UPLOAD_DIR);

        String originalFilename = coverImage.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String filename;
        Path destination;

        do {
            filename = UUID.randomUUID() + extension;
            destination = UPLOAD_DIR.resolve(filename).normalize();
        } while (Files.exists(destination));

        try (InputStream inputStream = coverImage.getInputStream()) {
            Files.copy(inputStream, destination);
        }

        return UPLOAD_DIR.resolve(filename).toString().replace("\\", "/");
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }

        String safeFilename = Paths.get(filename).getFileName().toString();
        int dotIndex = safeFilename.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }

        return safeFilename.substring(dotIndex);
    }
}
