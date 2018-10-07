package com.pavelsklenar.rest.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pavelsklenar.rest.pojo.Customer;

@RestController
@RequestMapping("/customer")

public class CustomerController {

    @GetMapping("/{id}")
    @Secured("ROLE_USER")
    public Customer GetCustomer(@PathVariable Long id) {
        return new Customer(id, "" + id.hashCode());
    }
}
