package com.studentfeedbacksystem.controllers;

import com.studentfeedbacksystem.models.Workshop;
import com.studentfeedbacksystem.services.WorkshopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/workshops")
public class WorkshopController {

    @Autowired
    private WorkshopService workshopService;

    // Path to save uploaded materials
    private final String UPLOAD_DIR = "src/main/resources/static/materials";

    // Create a new workshop
    @PostMapping
    public ResponseEntity<Workshop> createWorkshop(
            @RequestParam("name") String name,
            @RequestParam("date") String date,
            @RequestParam("time") String time,
            @RequestParam("meetingLink") String meetingLink,
            @RequestParam("description") String description,
            @RequestParam("instructor") String instructor,
            @RequestParam("material") MultipartFile material) {

        Workshop workshop = new Workshop();
        workshop.setName(name);
        workshop.setDate(date);
        workshop.setTime(time);
        workshop.setMeetingLink(meetingLink);
        workshop.setDescription(description);
        workshop.setInstructor(instructor);

        // Save the material and set its path in the workshop object
        try {
            String materialPath = saveMaterial(material);
            workshop.setMaterial(materialPath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return an error response
        }

        Workshop savedWorkshop = workshopService.saveWorkshop(workshop);
        return ResponseEntity.ok(savedWorkshop);
    }

    // Get all workshops
    @GetMapping
    public List<Workshop> getAllWorkshops() {
        return workshopService.getAllWorkshops();
    }

    // Get a workshop by ID
    @GetMapping("/{id}")
    public ResponseEntity<Workshop> getWorkshopById(@PathVariable Long id) {
        Workshop workshop = workshopService.getWorkshopById(id);
        if (workshop == null) {
            return ResponseEntity.notFound().build(); // Handle workshop not found
        }
        return ResponseEntity.ok(workshop);
    }

    // Update an existing workshop
    @PutMapping("/{id}")
    public ResponseEntity<Workshop> updateWorkshop(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("date") String date,
            @RequestParam("time") String time,
            @RequestParam("meetingLink") String meetingLink,
            @RequestParam("description") String description,
            @RequestParam("instructor") String instructor,
            @RequestParam(value = "material", required = false) MultipartFile material) {
        
        Workshop workshop = workshopService.getWorkshopById(id);
        if (workshop == null) {
            return ResponseEntity.notFound().build(); // Handle workshop not found
        }

        // Update workshop details
        workshop.setName(name);
        workshop.setDate(date);
        workshop.setTime(time);
        workshop.setMeetingLink(meetingLink);
        workshop.setDescription(description);
        workshop.setInstructor(instructor);

        // Handle file upload if provided
        if (material != null && !material.isEmpty()) {
            try {
                String materialPath = saveMaterial(material);
                workshop.setMaterial(materialPath);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return an error response
            }
        }

        Workshop updatedWorkshop = workshopService.saveWorkshop(workshop);
        return ResponseEntity.ok(updatedWorkshop);
    }

    // Delete a workshop by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkshop(@PathVariable Long id) {
        workshopService.deleteWorkshop(id);
        return ResponseEntity.noContent().build();
    }

    // Save the uploaded material to the specified directory
    private String saveMaterial(MultipartFile material) throws IOException {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory if it doesn't exist
        }

        String filename = System.currentTimeMillis() + "_" + material.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, filename);

        Files.copy(material.getInputStream(), filePath);

        return filename;  // Return just the filename for storage in the Workshop object
    }

    // Retrieve the uploaded material
    @GetMapping("/materials/{filename:.+}")
    public ResponseEntity<Resource> getMaterial(@PathVariable String filename) {
        Path file = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
        Resource resource = new org.springframework.core.io.FileSystemResource(file);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build(); // Handle file not found
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // Use the appropriate content type
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
