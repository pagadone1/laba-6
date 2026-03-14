package com.example.demo.service;

import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Customer create(Customer customer) {
        if (customer.getEmail() != null && repository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Клиент с email " + customer.getEmail() + " уже существует");
        }
        return repository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Customer> getAll() {
        return repository.findAll();
    }

    @Transactional
    public Customer update(Long id, Customer customer) {
        Customer existing = repository.findById(id).orElse(null);
        if (existing == null) return null;
        customer.setId(id);
        return repository.save(customer);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
