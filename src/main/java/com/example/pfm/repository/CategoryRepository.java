package com.example.pfm.repository;

import com.example.pfm.entity.Category;
import com.example.pfm.entity.TransactionType;
import com.example.pfm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    @Query("SELECT c FROM Category c WHERE c.user = :user OR c.custom = false")
    List<Category> findAllForUser(@Param("user") User user);
    
    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user = :user OR c.custom = false)")
    Optional<Category> findByNameForUser(@Param("name") String name, @Param("user") User user);
    
    boolean existsByNameAndUser(String name, User user);
    
    boolean existsByNameAndCustomFalse(String name);
    
    Optional<Category> findByNameAndUser(String name, User user);
    
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.custom = false")
    Optional<Category> findDefaultByName(@Param("name") String name);
}
