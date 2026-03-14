package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_orders")
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mechanic_id", nullable = false)
    private Mechanic mechanic;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderPart> orderParts = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal laborCost = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean requiredTasksCompleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.OPEN;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @JsonIgnore
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @JsonIgnore
    public Mechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(Mechanic mechanic) {
        this.mechanic = mechanic;
    }

    @JsonIgnore
    public List<OrderPart> getOrderParts() {
        return orderParts;
    }

    public void setOrderParts(List<OrderPart> orderParts) {
        this.orderParts = orderParts;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }

    public boolean isRequiredTasksCompleted() {
        return requiredTasksCompleted;
    }

    public void setRequiredTasksCompleted(boolean requiredTasksCompleted) {
        this.requiredTasksCompleted = requiredTasksCompleted;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Long getVehicleId() {
        return vehicle != null ? vehicle.getId() : null;
    }

    public Long getCustomerId() {
        return customer != null ? customer.getId() : null;
    }

    public Long getMechanicId() {
        return mechanic != null ? mechanic.getId() : null;
    }

    public List<Long> getPartIds() {
        List<Long> ids = new ArrayList<>();
        for (OrderPart op : orderParts) {
            for (int i = 0; i < op.getQuantity(); i++) {
                ids.add(op.getPart().getId());
            }
        }
        return ids;
    }
}
