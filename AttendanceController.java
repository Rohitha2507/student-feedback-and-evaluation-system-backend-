package com.studentfeedbacksystem.controllers;

import com.studentfeedbacksystem.models.Attendance;
import com.studentfeedbacksystem.models.Registration;
import com.studentfeedbacksystem.services.AttendanceService;
import com.studentfeedbacksystem.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private RegistrationService registrationService;

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    // Endpoint for faculty/admin to mark attendance
    @PostMapping("/mark")
    public ResponseEntity<Attendance> markAttendance(@RequestBody Attendance attendance) {
        try {
            if (attendance.getWorkshopId() == null || attendance.getUsername() == null) {
                logger.warn("Invalid input: Workshop ID or Username is null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Mark the attendance using the service, passing isPresent as an integer
            Attendance savedAttendance = attendanceService.markAttendance(
                    attendance.getWorkshopId(),
                    attendance.getUsername(),
                    attendance.isPresent() ? 1 : 0 // Convert boolean to int
            );
            logger.info("Successfully marked attendance: {}", savedAttendance);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAttendance);
        } catch (RuntimeException e) {
            logger.error("Error marking attendance: {}", e.getMessage());
            if (e.getMessage().contains("Registration not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Endpoint for faculty/admin to get attendance by workshop
    @GetMapping("/workshop/{workshopId}")
    public ResponseEntity<List<Attendance>> getAttendanceByWorkshop(@PathVariable Long workshopId) {
        List<Attendance> attendanceList = attendanceService.getAttendanceByWorkshop(workshopId);
        if (attendanceList.isEmpty()) {
            logger.warn("No attendance records found for workshopId: {}", workshopId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(attendanceList);
    }

    // Endpoint for students to view their attendance
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Attendance>> getUserAttendance(@PathVariable String username) {
        List<Attendance> attendanceList = attendanceService.getUserAttendance(username);
        if (attendanceList.isEmpty()) {
            logger.warn("No attendance records found for username: {}", username);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(attendanceList);
    }

    // Endpoint to get participants by workshop ID
    @GetMapping("/workshop/{workshopId}/participants")
    public ResponseEntity<List<Registration>> getParticipantsByWorkshop(@PathVariable Long workshopId) {
        List<Registration> participants = registrationService.getRegisteredStudentsByWorkshop(workshopId);
        if (participants.isEmpty()) {
            logger.warn("No participants found for workshopId: {}", workshopId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(participants);
    }
}
