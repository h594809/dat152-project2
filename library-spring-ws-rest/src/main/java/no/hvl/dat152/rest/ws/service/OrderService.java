// OrderService.java
package no.hvl.dat152.rest.ws.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import no.hvl.dat152.rest.ws.exceptions.OrderNotFoundException;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public Order findOrder(Long id) throws OrderNotFoundException {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with id: " + id + " not found in the order list!"));
    }

    public void deleteOrder(Long id) throws OrderNotFoundException {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order with id: " + id + " not found!");
        }

        orderRepository.deleteById(id);
    }


    public List<Order> findAllOrders(Pageable pageable) {
        return pageable == null ? orderRepository.findAll()
                : orderRepository.findAll(pageable).getContent();
    }

    public List<Order> findByExpiryDate(LocalDate expiry, Pageable page) {
        int limit = page.getPageSize();
        int offset = (int) page.getOffset();
        return orderRepository.findOrderByExpiry(expiry, limit, offset);
    }

    public Order updateOrder(Order order, Long id) throws OrderNotFoundException {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order with id: " + id + " not found in the order list!");
        }
        order.setId(id);
        return orderRepository.save(order);
    }
}
