package com.example.demo.service;

import com.example.demo.entity.Mechanic;
import com.example.demo.repository.MechanicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MechanicService {

    private final MechanicRepository repository;

    public MechanicService(MechanicRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Mechanic create(Mechanic mechanic) {
        return repository.save(mechanic);
    }

    @Transactional(readOnly = true)
    public Mechanic getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Mechanic> getAll() {
        return repository.findAll();
    }

    @Transactional
    public Mechanic update(Long id, Mechanic mechanic) {
        Mechanic existing = repository.findById(id).orElse(null);
        if (existing == null) return null;
        mechanic.setId(id);
        return repository.save(mechanic);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
