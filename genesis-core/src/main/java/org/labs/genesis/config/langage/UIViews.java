package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UIViews {
    private int id;
    private String name;
    private int langageId;
    private String fileName;
    private String template;
    private String listTemplate;
    private String createTemplate;
}
