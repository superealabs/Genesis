package org.labs.genesis.api.controller;

import org.labs.genesis.api.service.FrontendConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FrontendConfigController {
    private final FrontendConfigService service;

    public FrontendConfigController(FrontendConfigService service) {
        this.service = service;
    }

    @GetMapping("/frontend/frameworks")
    public List<FrontendConfigService.FrameworkOption> frameworks() {
        return service.frameworks();
    }

    @PostMapping("/generator/frontend-config")
    public FrontendConfigService.FrontendConfigResponse configure(
            @RequestBody FrontendConfigService.FrontendConfigRequest request) {
        return service.configure(request);
    }
}
