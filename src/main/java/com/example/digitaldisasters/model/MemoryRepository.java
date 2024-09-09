package com.example.digitaldisasters.model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemoryRepository extends JpaRepository<Memory, Long> {

    List<Memory> findByTagsContaining(String tag);
}
