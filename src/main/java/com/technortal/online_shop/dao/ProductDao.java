package com.technortal.online_shop.dao;

import com.technortal.online_shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductDao extends JpaRepository<Product, Long> {

    List<Product> findAllByIsDeleteFalseOrderByIdDesc();

    Optional<Product> findByIdAndIsDeleteFalse(Long id);
}
