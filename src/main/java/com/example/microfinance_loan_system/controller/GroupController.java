package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.model.Group;
import com.example.microfinance_loan_system.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','LOAN_OFFICER')")
    public ResponseEntity<Group> createGroup(@RequestBody List<Long> clientIds) {
        Group group = groupService.createGroup(clientIds);
        return new ResponseEntity<>(group, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }
}
