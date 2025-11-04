package no.hvl.dat152.rest.ws.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import no.hvl.dat152.rest.ws.exceptions.AuthorNotFoundException;
import no.hvl.dat152.rest.ws.model.Author;
import no.hvl.dat152.rest.ws.model.Book;
import no.hvl.dat152.rest.ws.service.AuthorService;

@RestController
@RequestMapping("/elibrary/api/v1")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    // HENT ALLE FORFATTERE
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/authors")
    public ResponseEntity<Object> getAllAuthors() {
        List<Author> authors = authorService.findAll();
        if (authors.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        authors.forEach(this::addAuthorLinks);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    // HENT EN FORFATTER
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/authors/{id}")
    public ResponseEntity<Author> getAuthor(@PathVariable int id) throws AuthorNotFoundException {
        Author author = authorService.findById(id);
        addAuthorLinks(author);
        return new ResponseEntity<>(author, HttpStatus.OK);
    }

    // HENT BØKER AV EN FORFATTER
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @GetMapping("/authors/{id}/books")
    public ResponseEntity<Object> getBooksByAuthorId(@PathVariable int id) throws AuthorNotFoundException {
        Set<Book> books = authorService.findBooksByAuthorId(id);
        if (books.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    // OPPRETT FORFATTER
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/authors")
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
        Author newAuthor = authorService.saveAuthor(author);
        addAuthorLinks(newAuthor);
        return new ResponseEntity<>(newAuthor, HttpStatus.CREATED);
    }

    // OPPDATER FORFATTER
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/authors/{id}")
    public ResponseEntity<Author> updateAuthor(@PathVariable int id, @RequestBody Author author)
            throws AuthorNotFoundException {
        authorService.findById(id);
        author.setAuthorId(id);
        Author updatedAuthor = authorService.updateAuthor(author);
        addAuthorLinks(updatedAuthor);
        return new ResponseEntity<>(updatedAuthor, HttpStatus.OK);
    }

    // HATEOAS
    private void addAuthorLinks(Author author) {
        try {
            author.add(linkTo(methodOn(AuthorController.class).getAuthor(author.getAuthorId())).withSelfRel());
            author.add(linkTo(methodOn(AuthorController.class).getAllAuthors()).withRel("authors"));
            author.add(linkTo(methodOn(AuthorController.class).getBooksByAuthorId(author.getAuthorId())).withRel("books"));
        } catch (AuthorNotFoundException e) {
            // Ignorer
        }
    }

}
