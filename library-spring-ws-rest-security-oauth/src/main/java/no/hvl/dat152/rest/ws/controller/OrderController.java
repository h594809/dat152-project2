package no.hvl.dat152.rest.ws.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import no.hvl.dat152.rest.ws.exceptions.OrderNotFoundException;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.service.OrderService;

@RestController
@RequestMapping("/elibrary/api/v1")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/orders")
    public ResponseEntity<Object> getAllBorrowOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String expiry) {

        Pageable pageable = PageRequest.of(page, size);
        List<Order> orders = (expiry == null)
                ? orderService.findAllOrders(pageable)
                : orderService.findByExpiryDate(LocalDate.parse(expiry), pageable);

        if (orders.isEmpty()) return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        for (Order o : orders) addOrderLinks(o);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/orders/{id}")
    public ResponseEntity<Object> getBorrowOrder(@PathVariable Long id) throws OrderNotFoundException {
        Order order = orderService.findOrder(id);
        addOrderLinks(order);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/orders/{id}")
    public ResponseEntity<Object> updateOrder(@PathVariable Long id, @RequestBody Order order)
            throws OrderNotFoundException {
        Order updated = orderService.updateOrder(order, id);
        addOrderLinks(updated);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Object> deleteBookOrder(@PathVariable Long id) throws OrderNotFoundException {
        orderService.deleteOrder(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void addOrderLinks(Order order) {
        try {
            order.add(linkTo(methodOn(OrderController.class).getBorrowOrder(order.getId())).withSelfRel());
            order.add(linkTo(methodOn(OrderController.class).getAllBorrowOrders(0, 10, null)).withRel("all-orders"));
        } catch (Exception ignore) {}
    }
}