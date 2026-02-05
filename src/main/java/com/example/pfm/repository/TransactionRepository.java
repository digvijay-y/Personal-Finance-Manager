package com.example.pfm.repository;

import com.example.pfm.entity.Transaction;
import com.example.pfm.entity.TransactionType;
import com.example.pfm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByUserOrderByDateDesc(User user);
    
    Optional<Transaction> findByIdAndUser(Long id, User user);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "ORDER BY t.date DESC")
    List<Transaction> findByUserWithFilters(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") String category);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND t.date >= :startDate " +
           "AND t.type = :type")
    List<Transaction> findByUserAndDateAfterAndType(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("type") TransactionType type);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND YEAR(t.date) = :year " +
           "AND MONTH(t.date) = :month")
    List<Transaction> findByUserAndYearAndMonth(
            @Param("user") User user,
            @Param("year") int year,
            @Param("month") int month);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND YEAR(t.date) = :year")
    List<Transaction> findByUserAndYear(
            @Param("user") User user,
            @Param("year") int year);
    
    boolean existsByUserAndCategory(User user, String category);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date >= :startDate")
    List<Transaction> findByUserAndDateGreaterThanEqual(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate);
}
