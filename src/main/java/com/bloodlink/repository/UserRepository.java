package com.bloodlink.repository;

import com.bloodlink.entity.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    long countByAvailableTrue();

    @Query("select u from User u where u.enabled = true and u.available = true and u.bloodGroup in :groups "
         + "and lower(u.city) = lower(:city) and u.id <> :requesterId "
         + "and (u.lastDonationDate is null or u.lastDonationDate <= :cutoff)")
    List<User> findMatchingDonors(@Param("groups") List<String> groups, @Param("city") String city,
                                  @Param("requesterId") Long requesterId, @Param("cutoff") LocalDate cutoff);
}
