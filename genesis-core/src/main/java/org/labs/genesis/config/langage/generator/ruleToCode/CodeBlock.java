package org.labs.genesis.config.langage.generator.ruleToCode;

public class CodeBlock {
    String nameImport ;
    String layer;
    String className;
    String code;

    public CodeBlock( String nameImport , String layer, String className, String code) {
        this.nameImport = nameImport ;
        this.layer = layer;
        this.className = className;
        this.code = code;
    }

}