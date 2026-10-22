package org.labs.genesis.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class FrontendFrameworkIsolationService {

    private final ObjectMapper objectMapper;

    public FrontendFrameworkIsolationService() {
        this.objectMapper = new ObjectMapper();
    }

    public void isolate(ProjectGenerationContext context) {
        Objects.requireNonNull(context, "Le contexte d'exécution est obligatoire");

        FrontendFramework original = context.getFrontendFramework();

        if (original == null) {
            return;
        }

        try {
            FrontendFramework isolated = objectMapper.readValue(objectMapper.writeValueAsBytes(original), FrontendFramework.class);

            if (isolated == null || isolated == original) {
                throw new IllegalStateException("Copie frontend invalide");
            }

            if (!Objects.equals(original.getName(), isolated.getName()) || !Objects.equals(original.getCoreFramework(), isolated.getCoreFramework()) || !Objects.equals(original.getTemplate(), isolated.getTemplate())) {
                throw new IllegalStateException("La copie a perdu des paramètres du framework");
            }

            verifyCount("components", original.getComponents(), isolated.getComponents());
            verifyCount("additionalFiles", original.getAdditionalFiles(), isolated.getAdditionalFiles());
            verifyCount("conditionalFiles", original.getConditionalFiles(), isolated.getConditionalFiles());
            verifyCount("authenticationFiles", original.getAuthenticationFiles(), isolated.getAuthenticationFiles());
            // Réinitialiser uniquement les routes de la copie.
            isolated.setComponentRoutes(new ArrayList<>());
            context.setFrontendFramework(isolated);
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible d'isoler la configuration frontend", exception);
        }
    }

    private void verifyCount(String property, java.util.List<?> original, java.util.List<?> isolated) {
        if ((original == null) != (isolated == null)) {
            throw new IllegalStateException("Propriété perdue : " + property);
        }

        if (original != null && original.size() != isolated.size()) {
            throw new IllegalStateException("Nombre d'éléments différent : " + property);
        }
    }
}