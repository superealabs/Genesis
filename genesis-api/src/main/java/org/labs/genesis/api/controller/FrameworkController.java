package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.FrameworkDtos.FrameworkResponse;
import org.labs.genesis.api.dto.FrameworkDtos.FrameworkSelectionResponse;
import org.labs.genesis.api.service.FrameworkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/frameworks")
public class FrameworkController {

    private final FrameworkService frameworkService;

    public FrameworkController(FrameworkService frameworkService) {
        this.frameworkService = frameworkService;
    }

    @GetMapping
    public List<FrameworkResponse> getAll() {
        return frameworkService.getAll();
    }

    @PostMapping("/{id}/select")
    public FrameworkSelectionResponse select(@PathVariable int id) {
        return frameworkService.select(id);
    }
}