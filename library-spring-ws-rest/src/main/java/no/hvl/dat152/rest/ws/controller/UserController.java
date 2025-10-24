// UserController.java
package no.hvl.dat152.rest.ws.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import no.hvl.dat152.rest.ws.exceptions.OrderNotFoundException;
import no.hvl.dat152.rest.ws.exceptions.UserNotFoundException;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.model.User;
import no.hvl.dat152.rest.ws.service.UserService;

@RestController
@RequestMapping("/elibrary/api/v1")
public class UserController {

    @Autowired private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<Object> getUsers() {
        List<User> users = userService.findAllUsers();
        if (users.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        users.forEach(this::addUserLinks);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> getUser(@PathVariable Long id)
            throws UserNotFoundException {
        User user = userService.findUser(id);
        addUserLinks(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.saveUser(user);
        addUserLinks(created);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user)
            throws UserNotFoundException {
        User updated = userService.updateUser(user, id);
        addUserLinks(updated);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id)
            throws UserNotFoundException {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/users/{id}/orders")
    public ResponseEntity<Object> getUserOrders(@PathVariable Long id)
            throws UserNotFoundException {
        Set<Order> orders = userService.getUserOrders(id);
        if (orders.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        orders.forEach(o -> addOrderLinks(o, id));
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/users/{uid}/orders/{oid}")
    public ResponseEntity<Object> getUserOrder(@PathVariable Long uid, @PathVariable Long oid)
            throws UserNotFoundException, OrderNotFoundException {
        Order order = userService.getUserOrder(uid, oid);
        addOrderLinks(order, uid);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PostMapping("/users/{uid}/orders")
    public ResponseEntity<Object> createUserOrder(@PathVariable Long uid, @RequestBody Order order)
            throws UserNotFoundException {
        User user = userService.createOrdersForUser(uid, order);
        user.getOrders().forEach(o -> addOrderLinks(o, uid));
        return new ResponseEntity<>(user.getOrders(), HttpStatus.CREATED);
    }

    @DeleteMapping("/users/{uid}/orders/{oid}")
    public ResponseEntity<Object> deleteUserOrder(@PathVariable Long uid, @PathVariable Long oid)
            throws UserNotFoundException, OrderNotFoundException {
        userService.deleteOrderForUser(uid, oid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void addUserLinks(User user) {
        try {
            user.add(linkTo(methodOn(UserController.class).getUser(user.getUserid())).withSelfRel());
            user.add(linkTo(methodOn(UserController.class).getUserOrders(user.getUserid())).withRel("orders"));
            user.add(linkTo(methodOn(UserController.class).updateUser(user.getUserid(), null)).withRel("update"));
            user.add(linkTo(methodOn(UserController.class).deleteUser(user.getUserid())).withRel("delete"));
            user.add(linkTo(methodOn(UserController.class).createUserOrder(user.getUserid(), null)).withRel("create-order"));
        } catch (Exception ignore) {}
    }

    private void addOrderLinks(Order order, Long userId) {
        try {
            order.add(linkTo(methodOn(UserController.class).getUserOrder(userId, order.getId())).withSelfRel());
            order.add(linkTo(methodOn(UserController.class).getUserOrders(userId)).withRel("user-orders"));
            order.add(linkTo(methodOn(UserController.class).deleteUserOrder(userId, order.getId())).withRel("delete"));
            order.add(linkTo(methodOn(UserController.class).getUser(userId)).withRel("owner"));
        } catch (Exception ignore) {}
    }
}
