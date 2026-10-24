package org.labs.genesis.api.controller;

import org.labs.genesis.api.service.FrontendAssetService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/generator/frontend/assets")
public class FrontendAssetController {

    private final FrontendAssetService service;

    public FrontendAssetController(FrontendAssetService service) {
        this.service = service;
    }

    @PostMapping(value = "/{kind}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@PathVariable String kind, @RequestPart("file") MultipartFile file) throws IOException {
        return Map.of("assetId", service.upload(kind, file));
    }
}
