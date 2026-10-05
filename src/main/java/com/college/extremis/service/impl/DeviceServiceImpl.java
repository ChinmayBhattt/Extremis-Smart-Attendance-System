package com.college.extremis.service.impl;

import com.college.extremis.entity.Device;
import com.college.extremis.entity.Student;
import com.college.extremis.entity.User;
import com.college.extremis.repository.DeviceRepository;
import com.college.extremis.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Device verifyOrRegisterDevice(Student student, String deviceFingerprint, String deviceName) {
        Optional<Device> existing = deviceRepository.findByStudentAndDeviceFingerprint(student, deviceFingerprint);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Check if student has any previously registered device
        List<Device> studentDevices = deviceRepository.findByStudent(student);
        Device newDevice = new Device();
        newDevice.setStudent(student);
        newDevice.setDeviceFingerprint(deviceFingerprint);
        newDevice.setDeviceName(deviceName != null && !deviceName.isBlank() ? deviceName : "Unknown Browser/Device");
        newDevice.setRegisteredAt(LocalDateTime.now());

        if (studentDevices.isEmpty()) {
            // First device: automatically bound and approved
            newDevice.setApproved(true);
            newDevice.setApprovedAt(LocalDateTime.now());
        } else {
            // Subsequent device: requires administrator approval
            newDevice.setApproved(false);
        }

        return deviceRepository.save(newDevice);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeviceApproved(Student student, String deviceFingerprint) {
        return deviceRepository.findByStudentAndDeviceFingerprint(student, deviceFingerprint)
                .map(Device::isApproved)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Device> getPendingDeviceApprovals() {
        return deviceRepository.findByApprovedFalse();
    }

    @Override
    public void approveDevice(Long deviceId, User approver) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));
        device.setApproved(true);
        device.setApprovedAt(LocalDateTime.now());
        device.setApprovedByUser(approver);
        deviceRepository.save(device);
    }

    @Override
    public void rejectOrDeleteDevice(Long deviceId) {
        deviceRepository.deleteById(deviceId);
    }
}
