package org.labs.genesis.config.langage.generator.ruleToCode;

import lombok.Getter;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

@Getter
public class PromptManagement {
    String generalPrompt  ;
    String underPrompt ;

    public void managementPrompt(int idFramework) {
        try {
            generalPrompt =  readPrompt( ConstantesPrompt.GENERAL_PROMPT_TXT ) ;
            if (idFramework == 1) {
                underPrompt = readPrompt( ConstantesPrompt.SPRING_UNDER_PROMPT_TXT ) ;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading prompt ", e.getCause());
        }
    }
    public String readPrompt(String resourcePath) {
        InputStream in = PromptManagement.class.getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) throw new RuntimeException("Resource not found: " + resourcePath);
        try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
