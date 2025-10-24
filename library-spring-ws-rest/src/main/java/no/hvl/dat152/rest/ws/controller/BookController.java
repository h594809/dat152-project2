// BookController.java
package no.hvl.dat152.rest.ws.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import no.hvl.dat152.rest.ws.exceptions.BookNotFoundException;
import no.hvl.dat152.rest.ws.exceptions.UpdateBookFailedException;
import no.hvl.dat152.rest.ws.model.Author;
import no.hvl.dat152.rest.ws.model.Book;
import no.hvl.dat152.rest.ws.service.BookService;

@RestController
@RequestMapping("/elibrary/api/v1")
public class BookController {

    @Autowired private BookService bookService;

    @GetMapping("/books")
    public ResponseEntity<Object> getAllBooks() {
        List<Book> books = bookService.findAll();
        if (books.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @GetMapping("/books/{isbn}")
    public ResponseEntity<Object> getBook(@PathVariable String isbn) throws BookNotFoundException {
        Book book = bookService.findByISBN(isbn);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        return new ResponseEntity<>(bookService.saveBook(book), HttpStatus.CREATED);
    }

    @GetMapping("/books/{isbn}/authors")
    public ResponseEntity<Object> getAuthorsOfBookByISBN(@PathVariable String isbn) throws BookNotFoundException {
        Set<Author> authors = bookService.findAuthorsOfBookByISBN(isbn);
        if (authors.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    @PutMapping("/books/{isbn}")
    public ResponseEntity<Object> updateBookByISBN(@PathVariable String isbn, @RequestBody Book book)
            throws BookNotFoundException, UpdateBookFailedException {
        return new ResponseEntity<>(bookService.updateBook(book, isbn), HttpStatus.OK);
    }

    @DeleteMapping("/books/{isbn}")
    public ResponseEntity<Object> deleteBookByISBN(@PathVariable String isbn) throws BookNotFoundException {
        bookService.deleteByISBN(isbn);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
