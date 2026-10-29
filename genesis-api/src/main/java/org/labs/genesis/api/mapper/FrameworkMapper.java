package org.labs.genesis.api.mapper;

import org.labs.genesis.api.dto.FrameworkDtos.FrameworkResponse;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkMVC;

public final class FrameworkMapper {

    private FrameworkMapper() {
    }

    public static FrameworkResponse toResponse(Framework framework) {
        return new FrameworkResponse(
                framework.getId(),
                framework.getLanguageId(),
                framework.getName(),
                framework.getCoreFramework(),
                framework instanceof FrameworkMVC ? "MVC" : "REST API",
                Boolean.TRUE.equals(framework.getIsProd()),
                Boolean.TRUE.equals(framework.getUseDB()),
                Boolean.TRUE.equals(framework.getUseCloud()),
                Boolean.TRUE.equals(framework.getUseEurekaServer()),
                Boolean.TRUE.equals(framework.getIsGateway()),
                Boolean.TRUE.equals(framework.getUseFrontendApp())
        );
    }
}