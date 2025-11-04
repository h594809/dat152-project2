// UserService.java
package no.hvl.dat152.rest.ws.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import no.hvl.dat152.rest.ws.exceptions.OrderNotFoundException;
import no.hvl.dat152.rest.ws.exceptions.UserNotFoundException;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.model.User;
import no.hvl.dat152.rest.ws.repository.OrderRepository;
import no.hvl.dat152.rest.ws.repository.UserRepository;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;

    public List<User> findAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public User findUser(Long userid) throws UserNotFoundException {
        return userRepository.findById(userid)
                .orElseThrow(() -> new UserNotFoundException("User with id: " + userid + " not found"));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) throws UserNotFoundException {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id: " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    public User updateUser(User user, Long id) throws UserNotFoundException {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id: " + id + " not found");
        }
        user.setUserid(id);
        return userRepository.save(user);
    }

    public Set<Order> getUserOrders(Long userid) throws UserNotFoundException {
        return findUser(userid).getOrders();
    }

    public Order getUserOrder(Long userid, Long oid) throws UserNotFoundException, OrderNotFoundException {
        User user = findUser(userid);
        Long ownerId = orderRepository.findUserID(oid);
        if (ownerId == null || !ownerId.equals(userid)) {
            throw new OrderNotFoundException("Order with id: " + oid + " not found for user with id: " + userid);
        }
        return user.getOrders()
                .stream()
                .filter(o -> o.getId().equals(oid))
                .findFirst()
                .orElseThrow(() -> new OrderNotFoundException("Order with id: " + oid + " not found for user with id: " + userid));
    }

    public void deleteOrderForUser(Long userid, Long oid) throws UserNotFoundException, OrderNotFoundException {
        User user = findUser(userid);
        Order toRemove = getUserOrder(userid, oid);
        user.removeOrder(toRemove);
        userRepository.save(user);
    }

    public User createOrdersForUser(Long userid, Order order) throws UserNotFoundException {
        User user = findUser(userid);
        user.addOrder(order);
        return userRepository.save(user);
    }
}
