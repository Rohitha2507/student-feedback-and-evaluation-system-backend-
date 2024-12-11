package com.studentfeedbacksystem.controllers;

import com.studentfeedbacksystem.models.Registration;
import com.studentfeedbacksystem.models.User; // Assuming you have a User model
import com.studentfeedbacksystem.models.Workshop;
import com.studentfeedbacksystem.services.RegistrationService;
import com.studentfeedbacksystem.services.UserService; // Assuming you have a UserService
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserService userService; // Service to get user details

    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    @PostMapping
    public ResponseEntity<Registration> registerWorkshop(@RequestBody Registration registration) {
        // Fetch email based on the username
        User user = userService.findByUsername(registration.getUsername()); // Assuming this method exists
        if (user == null) {
            logger.warn("User not found for username: {}", registration.getUsername());
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }

        // Create registration with email
        registration.setEmail(user.getEmail()); // Assuming the User model has a getEmail method
        Registration registered = registrationService.registerWorkshop(registration);
        logger.info("Successfully registered workshop: {}", registered);
        return ResponseEntity.ok(registered);
    }

    @DeleteMapping("/{workshopId}")
    public ResponseEntity<Void> unregisterWorkshop(@PathVariable Long workshopId, @RequestParam String username) {
        boolean success = registrationService.unregisterWorkshop(workshopId, username);
        if (success) {
            logger.info("Successfully unregistered username: {} from workshopId: {}", username, workshopId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            logger.warn("Failed to unregister username: {} from workshopId: {} (not found)", username, workshopId);
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    @GetMapping("/workshops/{username}")
    public ResponseEntity<List<Workshop>> getRegisteredWorkshops(@PathVariable String username) {
        logger.info("Fetching registered workshops for username: {}", username);
        List<Workshop> workshops = registrationService.getRegisteredWorkshops(username);
        if (workshops.isEmpty()) {
            logger.warn("No registered workshops found for username: {}", username);
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(workshops); // 200 OK with list of workshops
    }
}
