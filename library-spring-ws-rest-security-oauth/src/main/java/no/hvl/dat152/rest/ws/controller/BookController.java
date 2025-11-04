package no.hvl.dat152.rest.ws.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import no.hvl.dat152.rest.ws.exceptions.BookNotFoundException;
import no.hvl.dat152.rest.ws.exceptions.UpdateBookFailedException;
import no.hvl.dat152.rest.ws.model.Author;
import no.hvl.dat152.rest.ws.model.Book;
import no.hvl.dat152.rest.ws.service.BookService;

@RestController
@RequestMapping("/elibrary/api/v1")
public class BookController {

    @Autowired
    private BookService bookService;

    // HENT ALLE BØKER
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/books")
    public ResponseEntity<Object> getAllBooks() {
        List<Book> books = bookService.findAll();
        if (books.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        books.forEach(this::addBookLinks);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    //  HENT EN BOK
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/books/{isbn}")
    public ResponseEntity<Object> getBook(@PathVariable String isbn) throws BookNotFoundException {
        Book book = bookService.findByISBN(isbn);
        addBookLinks(book);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    // OPPRETT NY BOK
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book saved = bookService.saveBook(book);
        addBookLinks(saved);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // HENT ALLE FORFATTERE AV EN BOK
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/books/{isbn}/authors")
    public ResponseEntity<Object> getAuthorsOfBook(@PathVariable String isbn) throws BookNotFoundException {
        Set<Author> authors = bookService.findAuthorsOfBookByISBN(isbn);
        if (authors.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    // OPPDATER EN BOK
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/books/{isbn}")
    public ResponseEntity<Object> updateBook(@PathVariable String isbn, @RequestBody Book book)
            throws BookNotFoundException, UpdateBookFailedException {
        Book updated = bookService.updateBook(book, isbn);
        addBookLinks(updated);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    // SLETT EN BOK
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/books/{isbn}")
    public ResponseEntity<Object> deleteBook(@PathVariable String isbn) throws BookNotFoundException {
        bookService.deleteByISBN(isbn);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // HATEOAS-lenker
    private void addBookLinks(Book book) {
        try {
            book.add(linkTo(methodOn(BookController.class).getBook(book.getIsbn())).withSelfRel());
            book.add(linkTo(methodOn(BookController.class).getAllBooks()).withRel("books"));
            book.add(linkTo(methodOn(BookController.class).getAuthorsOfBook(book.getIsbn())).withRel("authors"));
        } catch (BookNotFoundException e) {
            // Ignorer
        }
    }

}
