package com.example.digitaldisasters.services;

import com.amazonaws.HttpMethod;
import com.example.digitaldisasters.model.Memory;
import com.example.digitaldisasters.model.MemoryRepository;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class S3MemoryService {

    private final S3StorageService s3StorageService;
    private final MemoryRepository repository;

    public S3MemoryService(MemoryRepository repository, S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
        this.repository = repository;
    }

    public Memory saveMemory(InputStream imageStream, String fileName, String description, Set<String> tags) {
        try {
            // Generate a unique file name
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String key = UUID.randomUUID() + extension;

            s3StorageService.uploadFile(key, imageStream);

            String imageUrl = s3StorageService.generatePresignedUrl(key, HttpMethod.GET, 3600);

            Memory memory = new Memory();
            memory.setImageUrl(imageUrl);
            memory.setDescription(description);
            memory.setTags(tags);

            return repository.save(memory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Memory> findAll() {
        return repository.findAll();
    }

    public Memory findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void updateMemory(Memory memory) {
        repository.save(memory);
    }

    public List<Memory> findByTag(String tag) {
        if (tag.isBlank()) {
            return repository.findAll();
        } else {
            return repository.findByTagsContaining(tag);
        }
    }
}