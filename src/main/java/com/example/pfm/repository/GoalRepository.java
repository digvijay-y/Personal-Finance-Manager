package com.example.pfm.repository;

import com.example.pfm.entity.Goal;
import com.example.pfm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    
    List<Goal> findByUser(User user);
    
    Optional<Goal> findByIdAndUser(Long id, User user);
}
