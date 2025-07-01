package com.jaba.p2_t.extensions;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExtenService {
    private final ExtensionsRepository repository;

    public Extensions save(Extensions ext) {
        return repository.save(ext);
    }

    public Optional<Extensions> findById(Long id) {
        return repository.findById(id);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<Extensions> findAll() {
        return repository.findAll();
    }
}
