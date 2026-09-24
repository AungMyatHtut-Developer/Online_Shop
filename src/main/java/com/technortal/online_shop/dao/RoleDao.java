package com.technortal.online_shop.dao;

import com.technortal.online_shop.entity.Role;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleDao extends JpaRepository<Role, Long> {
    @Override
    @EntityGraph(attributePaths = "menus")
    Optional<Role> findById(Long id);

    @EntityGraph(attributePaths = "menus")
    List<Role> findAllByOrderByNameAsc();
    Optional<Role> findBySystemCode(String systemCode);
    boolean existsByNameKeyAndIdNot(String nameKey, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Role r where r.id = :id")
    Optional<Role> findForUpdate(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Role r where r.id in :ids order by r.id")
    List<Role> findAllForUpdate(Collection<Long> ids);
}
