package com.example.demo.service;

import com.example.demo.dto.ServiceOrderRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.ServiceOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServiceOrderService {

    private final ServiceOrderRepository repository;
    private final VehicleService vehicleService;
    private final CustomerService customerService;
    private final MechanicService mechanicService;
    private final PartService partService;

    public ServiceOrderService(ServiceOrderRepository repository, VehicleService vehicleService,
                              CustomerService customerService, MechanicService mechanicService,
                              PartService partService) {
        this.repository = repository;
        this.vehicleService = vehicleService;
        this.customerService = customerService;
        this.mechanicService = mechanicService;
        this.partService = partService;
    }

    @Transactional
    public ServiceOrder create(ServiceOrderRequest request) {
        validateReferences(request);
        ServiceOrder order = new ServiceOrder();
        order.setVehicle(vehicleService.getById(request.getVehicleId()));
        order.setCustomer(customerService.getById(request.getCustomerId()));
        order.setMechanic(mechanicService.getById(request.getMechanicId()));
        order.setLaborCost(request.getLaborCost() != null ? request.getLaborCost() : BigDecimal.ZERO);
        order.setRequiredTasksCompleted(request.isRequiredTasksCompleted());
        order.setStatus(OrderStatus.OPEN);
        order = repository.save(order);
        if (request.getPartIds() != null && !request.getPartIds().isEmpty()) {
            addPartsToOrder(order.getId(), request.getPartIds());
            order = repository.findByIdWithParts(order.getId());
        }
        return order;
    }

    private void validateReferences(ServiceOrderRequest request) {
        validateCoreReferences(request);
        if (request.getPartIds() != null) {
            for (Long partId : request.getPartIds()) {
                if (partService.getById(partId) == null) {
                    throw new IllegalArgumentException("Part not found: " + partId);
                }
            }
        }
    }

    private void validateCoreReferences(ServiceOrderRequest request) {
        if (request.getVehicleId() == null || vehicleService.getById(request.getVehicleId()) == null) {
            throw new IllegalArgumentException("Vehicle not found: " + request.getVehicleId());
        }
        if (request.getCustomerId() == null || customerService.getById(request.getCustomerId()) == null) {
            throw new IllegalArgumentException("Customer not found: " + request.getCustomerId());
        }
        if (request.getMechanicId() == null || mechanicService.getById(request.getMechanicId()) == null) {
            throw new IllegalArgumentException("Mechanic not found: " + request.getMechanicId());
        }
    }

    @Transactional(readOnly = true)
    public ServiceOrder getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public ServiceOrder getByIdWithParts(Long id) {
        return repository.findByIdWithParts(id);
    }

    @Transactional(readOnly = true)
    public List<ServiceOrder> getAll() {
        return repository.findAll();
    }

    @Transactional
    public ServiceOrder update(Long id, ServiceOrderRequest request) {
        ServiceOrder existing = repository.findById(id).orElse(null);
        if (existing == null) return null;
        if (existing.getStatus() == OrderStatus.CLOSED) {
            throw new IllegalArgumentException("Cannot update a closed order");
        }
        validateCoreReferences(request);
        existing.setVehicle(vehicleService.getById(request.getVehicleId()));
        existing.setCustomer(customerService.getById(request.getCustomerId()));
        existing.setMechanic(mechanicService.getById(request.getMechanicId()));
        existing.setLaborCost(request.getLaborCost() != null ? request.getLaborCost() : BigDecimal.ZERO);
        existing.setRequiredTasksCompleted(request.isRequiredTasksCompleted());
        return repository.save(existing);
    }

    @Transactional
    public ServiceOrder close(Long id) {
        ServiceOrder existing = repository.findByIdWithParts(id);
        if (existing == null) return null;
        if (existing.getStatus() == OrderStatus.CLOSED) {
            throw new IllegalArgumentException("Order is already closed");
        }
        if (!existing.isRequiredTasksCompleted()) {
            throw new IllegalArgumentException("Cannot close order: required tasks are not completed");
        }
        existing.setStatus(OrderStatus.CLOSED);
        return repository.save(existing);
    }

    /** Business op 1: Close order - already above */

    /** Business op 2: Calculate order total (parts + labor) */
    @Transactional(readOnly = true)
    public BigDecimal calculateOrderTotal(Long id) {
        ServiceOrder order = repository.findByIdWithParts(id);
        if (order == null) return null;
        BigDecimal partsTotal = order.getOrderParts().stream()
                .map(op -> op.getPart().getPrice().multiply(BigDecimal.valueOf(op.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return partsTotal.add(order.getLaborCost() != null ? order.getLaborCost() : BigDecimal.ZERO);
    }

    /** Business op 3: Add parts to order and decrease stock (single transaction) */
    @Transactional
    public ServiceOrder addPartsToOrder(Long orderId, List<Long> partIds) {
        ServiceOrder order = repository.findByIdWithParts(orderId);
        if (order == null) return null;
        if (order.getStatus() == OrderStatus.CLOSED) {
            throw new IllegalArgumentException("Cannot modify closed order");
        }
        Map<Long, Long> partQuantity = partIds.stream().collect(Collectors.groupingBy(p -> p, Collectors.counting()));
        for (Map.Entry<Long, Long> e : partQuantity.entrySet()) {
            Part part = partService.getById(e.getKey());
            if (part == null) throw new IllegalArgumentException("Part not found: " + e.getKey());
            int qty = e.getValue().intValue();
            if (part.getQuantity() < qty) {
                throw new IllegalArgumentException("Insufficient quantity for part " + part.getName() + ": need " + qty + ", have " + part.getQuantity());
            }
            part.setQuantity(part.getQuantity() - qty);
            partService.update(part.getId(), part);
            Optional<OrderPart> existingOp = order.getOrderParts().stream()
                    .filter(op -> op.getPart().getId().equals(part.getId()))
                    .findFirst();
            if (existingOp.isPresent()) {
                existingOp.get().setQuantity(existingOp.get().getQuantity() + qty);
            } else {
                OrderPart op = new OrderPart();
                op.setOrder(order);
                op.setPart(part);
                op.setQuantity(qty);
                order.getOrderParts().add(op);
            }
        }
        return repository.save(order);
    }

    /** Business op 4: Get customer service history */
    @Transactional(readOnly = true)
    public List<ServiceOrder> getOrdersByCustomer(Long customerId) {
        return repository.findByCustomer_IdOrderByIdDesc(customerId);
    }

    /** Business op 5: Get mechanic's open orders (workload) */
    @Transactional(readOnly = true)
    public List<ServiceOrder> getOpenOrdersByMechanic(Long mechanicId) {
        return repository.findByMechanic_IdAndStatus(mechanicId, OrderStatus.OPEN);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
