package com.example.library;

import com.example.library.entity.Book;
import com.example.library.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class LibraryApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookRepository bookRepository;

	@BeforeEach
	void setUp() throws IOException {
		bookRepository.deleteAll();
		deleteUploads();
	}

	@AfterEach
	void tearDown() throws IOException {
		deleteUploads();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createBookWithImageUploadStoresCoverUrl() throws Exception {
		MockMultipartFile coverImage = new MockMultipartFile(
				"coverImage",
				"cover.jpg",
				MediaType.IMAGE_JPEG_VALUE,
				"cover".getBytes()
		);

		mockMvc.perform(multipart("/api/books")
						.file(coverImage)
						.param("title", "Clean Code")
						.param("author", "Robert C. Martin")
						.param("stock", "5"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Clean Code"))
				.andExpect(jsonPath("$.coverUrl").value(startsWith("uploads/")));

		Book book = bookRepository.findAll().getFirst();
		assertTrue(Files.exists(Paths.get(book.getCoverUrl())));
	}

	@Test
	void getBookByIdReturnsBook() throws Exception {
		Book book = saveBook(7);

		mockMvc.perform(get("/api/books/{id}", book.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(book.getId()))
				.andExpect(jsonPath("$.stock").value(7));
	}

	@Test
	void getBookByIdWithWrongIdReturnsNotFoundError() throws Exception {
		mockMvc.perform(get("/api/books/{id}", 10L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Book not found with id: 10"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void updateBookWithNegativeStockReturnsBadRequestError() throws Exception {
		Book book = saveBook(3);

		mockMvc.perform(patch("/api/books/update/{id}", book.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"stock\":-1}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("stock: must be greater than or equal to 0"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void updateBookWithValidStockUpdatesBook() throws Exception {
		Book book = saveBook(3);

		mockMvc.perform(patch("/api/books/update/{id}", book.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"stock\":12}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stock").value(12));
	}

	private Book saveBook(Integer stock) {
		Book book = new Book();
		book.setTitle("Clean Code");
		book.setAuthor("Robert C. Martin");
		book.setStock(stock);
		book.setCoverUrl("uploads/cover.jpg");
		return bookRepository.save(book);
	}

	private void deleteUploads() throws IOException {
		Path uploads = Paths.get("uploads");
		if (!Files.exists(uploads)) {
			return;
		}

		try (var paths = Files.walk(uploads)) {
			paths.sorted((left, right) -> right.compareTo(left))
					.forEach(path -> {
						try {
							Files.deleteIfExists(path);
						} catch (IOException ex) {
							throw new IllegalStateException(ex);
						}
					});
		}
	}
}
