package com.college.extremis.repository;

import com.college.extremis.entity.Faculty;
import com.college.extremis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByUser(User user);
    Optional<Faculty> findByUserId(Long userId);
    Optional<Faculty> findByEmployeeCode(String employeeCode);
    List<Faculty> findByDepartmentId(Long departmentId);
    boolean existsByEmployeeCode(String employeeCode);
}
