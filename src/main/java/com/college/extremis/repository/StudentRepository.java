package com.college.extremis.repository;

import com.college.extremis.entity.Section;
import com.college.extremis.entity.Student;
import com.college.extremis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByRollNumber(String rollNumber);
    List<Student> findBySection(Section section);
    List<Student> findBySectionId(Long sectionId);
    boolean existsByRollNumber(String rollNumber);
    boolean existsByRegistrationNumber(String registrationNumber);
}
