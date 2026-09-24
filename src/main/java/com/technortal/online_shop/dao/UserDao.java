package com.technortal.online_shop.dao;

import com.technortal.online_shop.entity.UserAccount;
import com.technortal.online_shop.dto.RoleUserCountDto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserDao extends JpaRepository<UserAccount, Long> {
    @Override
    @EntityGraph(attributePaths = {"roles", "roles.menus"})
    Optional<UserAccount> findById(Long id);

    @EntityGraph(attributePaths = {"roles", "roles.menus"})
    Optional<UserAccount> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    @EntityGraph(attributePaths = {"roles", "roles.menus"})
    List<UserAccount> findAllByOrderByIdAsc();
    long countByRoles_Id(Long roleId);

    @Query("select new com.technortal.online_shop.dto.RoleUserCountDto(r.id, count(u)) from UserAccount u join u.roles r group by r.id")
    List<RoleUserCountDto> countUsersByRole();

    @Query("select u from UserAccount u where u.roles is empty")
    List<UserAccount> findWithoutRoles();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserAccount u where u.id = :id")
    Optional<UserAccount> findForUpdate(Long id);
}
