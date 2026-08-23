package edu.demart_api.repository;

import edu.demart_api.entity.StaffApplication;
import edu.demart_api.entity.StaffApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffApplicationRepository extends JpaRepository<StaffApplication, Long> {

    List<StaffApplication> findByStatusOrderByCreatedAtDesc(StaffApplicationStatus status);

    List<StaffApplication> findAllByOrderByCreatedAtDesc();

    Optional<StaffApplication> findByEmail(String email);

    boolean existsByEmailAndStatus(String email, StaffApplicationStatus status);
}
