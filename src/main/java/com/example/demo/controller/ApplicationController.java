package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;

    public ApplicationController(
            ApplicationService applicationService,
            UserService userService
    ) {
        this.applicationService = applicationService;
        this.userService = userService;
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<String> completeApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        User company = userService.findByEmail(userDetails.getUsername());

        applicationService.completeApplication(id, company);

        return ResponseEntity.ok("근무 완료 처리");
    }
}