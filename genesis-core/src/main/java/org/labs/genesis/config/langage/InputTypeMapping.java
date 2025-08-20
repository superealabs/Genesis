package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.Constantes;
import org.labs.utils.FileUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
public class InputTypeMapping {
    private int id;
    private int languageId;
    private Map<String, String> types;

    public static InputTypeMapping getInputTypeMapping(int languageId) {
        InputTypeMapping inputTypeMapping = new InputTypeMapping();
        try {
            inputTypeMapping = Arrays.stream(FileUtils.fromJson(InputTypeMapping[].class, Constantes.INPUT_TYPE_MAPPING_JSON))
                    .filter(itm -> itm.getLanguageId() == languageId)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputTypeMapping;
    }
}
