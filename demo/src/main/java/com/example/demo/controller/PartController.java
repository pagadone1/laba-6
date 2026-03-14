package com.example.demo.controller;

import com.example.demo.entity.Part;
import com.example.demo.service.PartService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @PostMapping
    public Part create(@RequestBody Part part) {
        return partService.create(part);
    }

    @GetMapping("/{id}")
    public Part getById(@PathVariable Long id) {
        return partService.getById(id);
    }

    @GetMapping
    public List<Part> getAll() {
        return partService.getAll();
    }

    @PutMapping("/{id}")
    public Part update(@PathVariable Long id, @RequestBody Part part) {
        return partService.update(id, part);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        partService.delete(id);
    }
}
