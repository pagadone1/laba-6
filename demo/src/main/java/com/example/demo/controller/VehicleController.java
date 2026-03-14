package com.example.demo.controller;

import com.example.demo.dto.VehicleRequest;
import com.example.demo.entity.Vehicle;
import com.example.demo.service.VehicleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public Vehicle create(@RequestBody VehicleRequest request) {
        return vehicleService.create(request);
    }

    @GetMapping("/{id}")
    public Vehicle getById(@PathVariable Long id) {
        Vehicle v = vehicleService.getById(id);
        if (v == null) throw new IllegalArgumentException("Vehicle not found: " + id);
        return v;
    }

    @GetMapping
    public List<Vehicle> getAll() {
        return vehicleService.getAll();
    }

    @PutMapping("/{id}")
    public Vehicle update(@PathVariable Long id, @RequestBody VehicleRequest request) {
        Vehicle v = vehicleService.update(id, request);
        if (v == null) throw new IllegalArgumentException("Vehicle not found: " + id);
        return v;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        vehicleService.delete(id);
    }

    @GetMapping("/by-owner/{customerId}")
    public List<Vehicle> getByOwner(@PathVariable Long customerId) {
        return vehicleService.getByOwnerId(customerId);
    }
}
