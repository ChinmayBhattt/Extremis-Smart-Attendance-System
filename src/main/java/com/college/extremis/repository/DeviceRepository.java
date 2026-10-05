package com.college.extremis.repository;

import com.college.extremis.entity.Device;
import com.college.extremis.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByStudentAndDeviceFingerprint(Student student, String deviceFingerprint);
    Optional<Device> findByStudentIdAndDeviceFingerprint(Long studentId, String deviceFingerprint);
    List<Device> findByStudent(Student student);
    List<Device> findByApprovedFalse();
}
