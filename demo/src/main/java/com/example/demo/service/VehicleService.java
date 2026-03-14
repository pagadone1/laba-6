package com.example.demo.service;

import com.example.demo.dto.VehicleRequest;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Vehicle;
import com.example.demo.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository repository;
    private final CustomerService customerService;

    public VehicleService(VehicleRepository repository, CustomerService customerService) {
        this.repository = repository;
        this.customerService = customerService;
    }

    @Transactional
    public Vehicle create(VehicleRequest request) {
        Customer owner = customerService.getById(request.getOwnerId());
        if (owner == null) {
            throw new IllegalArgumentException("Customer not found: " + request.getOwnerId());
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(request.getPlateNumber());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setOwner(owner);
        return repository.save(vehicle);
    }

    @Transactional
    public Vehicle create(Vehicle vehicle) {
        if (vehicle.getOwner() == null && vehicle.getOwnerId() != null) {
            Customer owner = customerService.getById(vehicle.getOwnerId());
            if (owner == null) {
                throw new IllegalArgumentException("Customer not found: " + vehicle.getOwnerId());
            }
            vehicle.setOwner(owner);
        }
        return repository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getAll() {
        return repository.findAll();
    }

    @Transactional
    public Vehicle update(Long id, VehicleRequest request) {
        Vehicle existing = repository.findById(id).orElse(null);
        if (existing == null) return null;
        Customer owner = customerService.getById(request.getOwnerId());
        if (owner == null) {
            throw new IllegalArgumentException("Customer not found: " + request.getOwnerId());
        }
        existing.setPlateNumber(request.getPlateNumber());
        existing.setModel(request.getModel());
        existing.setYear(request.getYear());
        existing.setOwner(owner);
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getByOwnerId(Long customerId) {
        return repository.findByOwner_Id(customerId);
    }
}
