package no.hvl.dat152.rest.ws.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import no.hvl.dat152.rest.ws.exceptions.OrderNotFoundException;
import no.hvl.dat152.rest.ws.exceptions.UserNotFoundException;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.model.User;
import no.hvl.dat152.rest.ws.service.UserService;

@RestController
@RequestMapping("/elibrary/api/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userService.findAllUsers();
        if (users.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        users.forEach(this::addUserLinks);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or @userService.findUserEmailById(#id) == authentication.principal.claims['email']")
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) throws UserNotFoundException {
        User user = userService.findUser(id);
        addUserLinks(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.saveUser(user);
        addUserLinks(created);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ADMIN') or @userService.findUserEmailById(#id) == authentication.principal.claims['email']")
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user)
            throws UserNotFoundException {
        User updated = userService.updateUser(user, id);
        addUserLinks(updated);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or @userService.findUserEmailById(#id) == authentication.principal.claims['email']")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws UserNotFoundException {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or @userService.findUserEmailById(#id) == authentication.principal.claims['email']")
    @GetMapping("/users/{id}/orders")
    public ResponseEntity<Set<Order>> getUserOrders(@PathVariable Long id) throws UserNotFoundException {
        Set<Order> orders = userService.getUserOrders(id);
        if (orders.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        orders.forEach(o -> addOrderLinks(o, id));
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or @userService.findUserEmailById(#uid) == authentication.principal.claims['email']")
    @GetMapping("/users/{uid}/orders/{oid}")
    public ResponseEntity<Order> getUserOrder(@PathVariable Long uid, @PathVariable Long oid)
            throws UserNotFoundException, OrderNotFoundException {
        Order order = userService.getUserOrder(uid, oid);
        addOrderLinks(order, uid);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN') or @userService.findUserEmailById(#id) == authentication.principal.claims['email']")
    @PostMapping("/users/{id}/orders")
    public ResponseEntity<List<Order>> createUserOrder(@PathVariable Long id, @RequestBody Order order)
            throws UserNotFoundException {

        System.out.println("Creating order for user " + id + " with ISBN: " + order.getIsbn());

        List<Order> createdOrders = userService.createOrdersForUser(id, order);

        System.out.println("Controller returning " + createdOrders.size() + " orders:");
        createdOrders.forEach(o -> {
            System.out.println(" - Order: " + o.getIsbn());
            addOrderLinks(o, id);
        });

        return new ResponseEntity<>(createdOrders, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ADMIN') or @userService.findUserEmailById(#uid) == authentication.principal.claims['email']")
    @DeleteMapping("/users/{uid}/orders/{oid}")
    public ResponseEntity<Void> deleteUserOrder(@PathVariable Long uid, @PathVariable Long oid) {

        userService.deleteOrderForUser(uid, oid);
        return ResponseEntity.ok().build();
    }

    private void addUserLinks(User user) {
        try {
            user.add(linkTo(methodOn(UserController.class).getUser(user.getUserid())).withSelfRel());
            user.add(linkTo(methodOn(UserController.class).getUserOrders(user.getUserid())).withRel("orders"));
        } catch (Exception ignore) {}
    }

    private void addOrderLinks(Order order, Long userId) {
        try {
            order.add(linkTo(methodOn(UserController.class).getUserOrder(userId, order.getId())).withSelfRel());
            order.add(linkTo(methodOn(UserController.class).getUserOrders(userId)).withRel("user-orders"));
        } catch (Exception ignore) {}
    }
}