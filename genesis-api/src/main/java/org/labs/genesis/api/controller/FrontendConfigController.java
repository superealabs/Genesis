package org.labs.genesis.api.controller;

import org.labs.genesis.api.service.FrontendConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/frontend/programming-languages")
    public List<FrontendConfigService.ProgrammingLanguageOption> programmingLanguages() {
        return service.programmingLanguages();
    }

    @GetMapping("/frontend/interface-languages")
    public List<FrontendConfigService.InterfaceLanguageOption> interfaceLanguages() {
        return service.interfaceLanguages();
    }

    @GetMapping("/frontend/navbar-types")
    public List<String> navbarTypes() {
        return service.navbarTypes();
    }

    @PostMapping("/generator/frontend-layout-config")
    public Map<String, Object> configureLayout(@RequestBody FrontendConfigService.FrontendLayoutRequest request) {
        service.configureLayout(request);
        return Map.of("success", true, "message", "Configuration frontend enregistrée");
    }

}
