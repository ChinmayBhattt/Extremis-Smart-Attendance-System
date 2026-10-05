package com.college.extremis.repository;

import com.college.extremis.entity.Section;
import com.college.extremis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByDepartmentId(Long departmentId);
    Optional<Section> findByClassTeacherUser(User teacher);
}
