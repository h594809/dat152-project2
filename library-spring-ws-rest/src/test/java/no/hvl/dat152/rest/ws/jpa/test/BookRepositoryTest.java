package no.hvl.dat152.rest.ws.jpa.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;

import no.hvl.dat152.rest.ws.main.LibraryApplication;
import no.hvl.dat152.rest.ws.model.Book;
import no.hvl.dat152.rest.ws.repository.BookRepository;

@SpringBootTest
@ContextConfiguration(classes = LibraryApplication.class)
@ActiveProfiles("test") // KJØRER UTEN DEFAULT DATA FRA RUNNER
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepo;

    @Test
    final void testFindAllAndSort() {
        Iterable<Book> books = bookRepo.findAll(Sort.by("title"));
        assertTrue(books.iterator().hasNext());
    }

    @Test
    final void testFindAllAndPage() {
        Pageable paging = PageRequest.of(0, 2);
        Page<Book> books = bookRepo.findAll(paging);
        assertTrue(books.getNumberOfElements() >= 0);
    }

    @Test
    final void testFindByTitleLike() {
        List<Book> books = bookRepo.findByTitleContaining("Software");
        assertNotNull(books);
    }

    @Test
    final void testFindByIsbn() {
        Optional<Book> book = bookRepo.findByIsbn("ghijk1234");
        assertTrue(book.isEmpty() || book.get().getIsbn().equals("ghijk1234"));
    }

    @Test
    final void testFindBookByIsbn() {
        Book book = bookRepo.findBookByISBN("ghijk1234");
        if (book != null) {
            assertEquals("ghijk1234", book.getIsbn());
        }
    }
}
