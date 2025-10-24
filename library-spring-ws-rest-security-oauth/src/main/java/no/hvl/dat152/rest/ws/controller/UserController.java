package no.hvl.dat152.rest.ws.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import no.hvl.dat152.rest.ws.exceptions.*;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.model.User;
import no.hvl.dat152.rest.ws.service.UserService;

@RestController
@RequestMapping("/elibrary/api/v1")
public class UserController {

    @Autowired
    private UserService userService;

    // === HENT ALLE BRUKERE (ADMIN) ===
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.findAllUsers();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        users.forEach(this::addUserLinks);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // === HENT ÉN BRUKER ===
    @PreAuthorize("hasAuthority('ADMIN') or authentication.token.claims['email'] == @userService.findUserEmailById(#id)")
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id,
                                        @AuthenticationPrincipal JwtAuthenticationToken principal)
            throws UserNotFoundException {
        User user = userService.findUser(id);
        addUserLinks(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // === OPPRETT BRUKER (kun ADMIN) ===
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.saveUser(user);
        addUserLinks(created);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // === OPPDATER BRUKER ===
    @PreAuthorize("hasAuthority('ADMIN') or authentication.token.claims['email'] == @userService.findUserEmailById(#id)")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody User user,
                                           @AuthenticationPrincipal JwtAuthenticationToken principal)
            throws UserNotFoundException {
        User updated = userService.updateUser(user, id);
        addUserLinks(updated);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    // === SLETT BRUKER ===
    @PreAuthorize("hasAuthority('ADMIN') or authentication.token.claims['email'] == @userService.findUserEmailById(#id)")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal JwtAuthenticationToken principal)
            throws UserNotFoundException {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // === HENT ALLE ORDRE FOR EN BRUKER ===
    @PreAuthorize("hasAuthority('ADMIN') or authentication.token.claims['email'] == @userService.findUserEmailById(#id)")
    @GetMapping("/users/{id}/orders")
    public ResponseEntity<Set<Order>> getUserOrders(@PathVariable Long id,
                                                    @AuthenticationPrincipal JwtAuthenticationToken principal)
            throws UserNotFoundException {
        Set<Order> orders = userService.getUserOrders(id);
        if (orders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        orders.forEach(o -> addOrderLinks(o, id));
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // === HENT EN SPESIFIKK ORDRE ===
    @PreAuthorize("hasAuthority('ADMIN') or authentication.token.claims['email'] == @userService.findUserEmailById(#id)")
    @GetMapping("/users/{id}/orders/{oid}")
    public ResponseEntity<Order> getUserOrder(@PathVariable Long id,
                                              @PathVariable Long oid,
                                              @AuthenticationPrincipal JwtAuthenticationToken principal)
            throws UserNotFoundException, OrderNotFoundException {
        Order order = userService.getUserOrder(id, oid);
        addOrderLinks(order, id);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    // === OPPRETT NY ORDRE ===
    @PreAuthorize("hasAuthority('ADMIN') or authentication.token.claims['email'] == @userService.findUserEmailById(#id)")
    @PostMapping("/users/{id}/orders")
    public ResponseEntity<Set<Order>> createUserOrder(@PathVariable Long id,
                                                      @RequestBody Order order,
                                                      @AuthenticationPrincipal JwtAuthenticationToken principal)
            throws UserNotFoundException {
        User user = userService.createOrdersForUser(id, order);
        user.getOrders().forEach(o -> addOrderLinks(o, id));
        return new ResponseEntity<>(user.getOrders(), HttpStatus.CREATED);
    }

    // === SLETT EN ORDRE ===
    @PreAuthorize("hasAuthority('ADMIN') or authentication.token.claims['email'] == @userService.findUserEmailById(#id)")
    @DeleteMapping("/users/{id}/orders/{oid}")
    public ResponseEntity<Void> deleteUserOrder(@PathVariable Long id,
                                                @PathVariable Long oid,
                                                @AuthenticationPrincipal JwtAuthenticationToken principal)
            throws UserNotFoundException, OrderNotFoundException, UnauthorizedOrderActionException {
        userService.deleteOrderForUser(id, oid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // === HATEOAS ===
    private void addUserLinks(User user) {
        try {
            user.add(linkTo(methodOn(UserController.class)
                    .getUser(user.getUserid(), null))
                    .withSelfRel());

            user.add(linkTo(methodOn(UserController.class)
                    .getUserOrders(user.getUserid(), null))
                    .withRel("orders"));

            user.add(linkTo(methodOn(UserController.class)
                    .updateUser(user.getUserid(), null, null))
                    .withRel("update"));

            user.add(linkTo(methodOn(UserController.class)
                    .deleteUser(user.getUserid(), null))
                    .withRel("delete"));

            user.add(linkTo(methodOn(UserController.class)
                    .createUserOrder(user.getUserid(), null, null))
                    .withRel("create-order"));
        } catch (Exception e) {
            // Disse exceptionene kastes aldri under HATEOAS, ignorer
        }
    }

    private void addOrderLinks(Order order, Long userId) {
        try {
            order.add(linkTo(methodOn(UserController.class)
                    .getUserOrder(userId, order.getId(), null))
                    .withSelfRel());

            order.add(linkTo(methodOn(UserController.class)
                    .getUserOrders(userId, null))
                    .withRel("user-orders"));

            order.add(linkTo(methodOn(UserController.class)
                    .deleteUserOrder(userId, order.getId(), null))
                    .withRel("delete"));

            order.add(linkTo(methodOn(UserController.class)
                    .getUser(userId, null))
                    .withRel("owner"));
        } catch (Exception e) {
            // Ignorer kompilasjonskrav
        }
    }
}