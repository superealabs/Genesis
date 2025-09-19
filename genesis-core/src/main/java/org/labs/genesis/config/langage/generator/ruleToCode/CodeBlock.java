package org.labs.genesis.config.langage.generator.ruleToCode;

public class CodeBlock {
    String layer;
    String className;
    String code;

    public CodeBlock(String layer, String className, String code) {
        this.layer = layer;
        this.className = className;
        this.code = code;
    }
}