package com.example.demo.controller;

import com.example.demo.entity.Mechanic;
import com.example.demo.service.MechanicService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mechanics")
public class MechanicController {

    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @PostMapping
    public Mechanic create(@RequestBody Mechanic mechanic) {
        return mechanicService.create(mechanic);
    }

    @GetMapping("/{id}")
    public Mechanic getById(@PathVariable Long id) {
        return mechanicService.getById(id);
    }

    @GetMapping
    public List<Mechanic> getAll() {
        return mechanicService.getAll();
    }

    @PutMapping("/{id}")
    public Mechanic update(@PathVariable Long id, @RequestBody Mechanic mechanic) {
        return mechanicService.update(id, mechanic);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        mechanicService.delete(id);
    }
}
