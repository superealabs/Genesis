package org.labs.genesis.api.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.labs.genesis.api.dto.FrontendDtos;
import org.labs.genesis.api.dto.FrontendDtos.*;
import org.labs.genesis.api.service.FrontendAssetService;
import org.labs.genesis.api.service.FrontendConfigService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FrontendController {

    private final FrontendConfigService frontendConfigService;

    private final FrontendAssetService frontendAssetService;

    public FrontendController(FrontendConfigService frontendConfigService, FrontendAssetService frontendAssetService) {
        this.frontendConfigService = frontendConfigService;
        this.frontendAssetService = frontendAssetService;
    }

    @GetMapping({
            "/api/frontend/frameworks",
            "/api/frontend/frameworks/{idFramework}"
    })
    public List<FrontendDtos.FrontendFrameworkOption> frameworks(@PathVariable(required = false) Integer idFramework) {
        if (idFramework == null) {
            return frontendConfigService.frameworks();
        }
        return frontendConfigService.frameworks(idFramework);
    }

    @PostMapping("/api/generator/frontend-config")
    public FrontendDtos.FrontendConfigResponse configure(@RequestBody FrontendDtos.FrontendConfigRequest request) {
        return frontendConfigService.configure(request);
    }

    @GetMapping("/api/frontend/programming-languages")
    public List<FrontendDtos.ProgrammingLanguageOption> programmingLanguages() {
        return frontendConfigService.programmingLanguages();
    }

    @GetMapping("/api/frontend/interface-languages")
    public List<FrontendDtos.InterfaceLanguageOption> interfaceLanguages() {
        return frontendConfigService.interfaceLanguages();
    }

    @GetMapping("/api/frontend/navbar-types")
    public List<String> navbarTypes() {
        return frontendConfigService.navbarTypes();
    }

    @PostMapping("/api/generator/frontend-layout-config")
    public Map<String, Object> configureLayout(@RequestBody FrontendDtos.FrontendLayoutRequest request) {
        frontendConfigService.configureLayout(request);
        return Map.of("success", true, "message", "Configuration frontend enregistrée");
    }

    @PostMapping(value = "/api/generator/frontend/assets/{kind}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@PathVariable String kind, @RequestPart("file") MultipartFile file) throws IOException {
        return Map.of("assetId", frontendAssetService.upload(kind, file));
    }
}
