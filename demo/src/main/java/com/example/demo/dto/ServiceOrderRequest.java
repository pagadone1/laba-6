package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class ServiceOrderRequest {

    private Long vehicleId;
    private Long customerId;
    private Long mechanicId;
    private List<Long> partIds;
    private BigDecimal laborCost;
    private boolean requiredTasksCompleted;

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(Long mechanicId) {
        this.mechanicId = mechanicId;
    }

    public List<Long> getPartIds() {
        return partIds;
    }

    public void setPartIds(List<Long> partIds) {
        this.partIds = partIds;
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
}
