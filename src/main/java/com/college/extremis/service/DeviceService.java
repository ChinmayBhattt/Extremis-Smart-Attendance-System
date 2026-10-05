package com.college.extremis.service;

import com.college.extremis.entity.Device;
import com.college.extremis.entity.Student;
import com.college.extremis.entity.User;

import java.util.List;

public interface DeviceService {
    Device verifyOrRegisterDevice(Student student, String deviceFingerprint, String deviceName);
    boolean isDeviceApproved(Student student, String deviceFingerprint);
    List<Device> getPendingDeviceApprovals();
    void approveDevice(Long deviceId, User approver);
    void rejectOrDeleteDevice(Long deviceId);
}
