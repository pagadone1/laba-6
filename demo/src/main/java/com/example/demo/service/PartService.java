package com.example.demo.service;

import com.example.demo.entity.Part;
import com.example.demo.repository.PartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PartService {

    private final PartRepository repository;

    public PartService(PartRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Part create(Part part) {
        return repository.save(part);
    }

    @Transactional(readOnly = true)
    public Part getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Part> getAll() {
        return repository.findAll();
    }

    @Transactional
    public Part update(Long id, Part part) {
        Part existing = repository.findById(id).orElse(null);
        if (existing == null) return null;
        part.setId(id);
        return repository.save(part);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
