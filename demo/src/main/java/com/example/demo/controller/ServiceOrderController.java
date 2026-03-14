package com.example.demo.controller;

import com.example.demo.dto.ServiceOrderRequest;
import com.example.demo.entity.ServiceOrder;
import com.example.demo.service.ServiceOrderService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/service-orders")
public class ServiceOrderController {

    private final ServiceOrderService serviceOrderService;

    public ServiceOrderController(ServiceOrderService serviceOrderService) {
        this.serviceOrderService = serviceOrderService;
    }

    @PostMapping
    public ServiceOrder create(@RequestBody ServiceOrderRequest request) {
        return serviceOrderService.create(request);
    }

    @GetMapping("/{id}")
    public ServiceOrder getById(@PathVariable Long id) {
        ServiceOrder o = serviceOrderService.getById(id);
        if (o == null) throw new IllegalArgumentException("Service order not found: " + id);
        return o;
    }

    @GetMapping
    public List<ServiceOrder> getAll() {
        return serviceOrderService.getAll();
    }

    @PutMapping("/{id}")
    public ServiceOrder update(@PathVariable Long id, @RequestBody ServiceOrderRequest request) {
        ServiceOrder o = serviceOrderService.update(id, request);
        if (o == null) throw new IllegalArgumentException("Service order not found: " + id);
        return o;
    }

    @PutMapping("/{id}/close")
    public ServiceOrder close(@PathVariable Long id) {
        ServiceOrder o = serviceOrderService.close(id);
        if (o == null) throw new IllegalArgumentException("Service order not found: " + id);
        return o;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        serviceOrderService.delete(id);
    }

    @GetMapping("/{id}/total")
    public BigDecimal getOrderTotal(@PathVariable Long id) {
        BigDecimal total = serviceOrderService.calculateOrderTotal(id);
        if (total == null) throw new IllegalArgumentException("Service order not found: " + id);
        return total;
    }

    @PostMapping("/{id}/parts")
    public ServiceOrder addParts(@PathVariable Long id, @RequestBody List<Long> partIds) {
        ServiceOrder o = serviceOrderService.addPartsToOrder(id, partIds);
        if (o == null) throw new IllegalArgumentException("Service order not found: " + id);
        return o;
    }

    @GetMapping("/by-customer/{customerId}")
    public List<ServiceOrder> getByCustomer(@PathVariable Long customerId) {
        return serviceOrderService.getOrdersByCustomer(customerId);
    }

    @GetMapping("/by-mechanic/{mechanicId}/open")
    public List<ServiceOrder> getOpenByMechanic(@PathVariable Long mechanicId) {
        return serviceOrderService.getOpenOrdersByMechanic(mechanicId);
    }
}
