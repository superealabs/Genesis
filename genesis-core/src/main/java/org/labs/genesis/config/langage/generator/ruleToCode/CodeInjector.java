package org.labs.genesis.config.langage.generator.ruleToCode;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeInjector {

    public CodeInjector() {} ;

    public List<CodeBlock> splitCode(String bigString, int idFramework) {
        List<CodeBlock> blocks = new ArrayList<>();
        String pattern = "";

        if (idFramework == 1 ) {
            pattern = "@(Service|Repository):\\s*(\\w+)\\s*((?:import\\s+[\\w\\.\\*]+;\\s*)*)(.*?)(?=@Service|@Repository|$)";
        }
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
        Matcher m = p.matcher(bigString);

        while (m.find()) {
            String layer = m.group(1).trim();           // Service oR Repository
            String className = m.group(2).trim();       // Name of class
            String importsBlock = m.group(3).trim();    // All import
            String code = m.group(4).trim();            // Rule code

            if (!importsBlock.isEmpty()) {
                String[] imports = importsBlock.split("\\r?\\n");
                for (String imp : imports) {
                    blocks.add(new CodeBlock("import", className, imp.trim()));
                }
            }
            if (!code.isEmpty()) {
                code = code.replaceAll("^```\\s*", "");
                code = code.replaceAll("\\s*```$", "");
                blocks.add(new CodeBlock(layer, className, code));
            }
        }
        return blocks;
    }

    public void injectBlocks(List<CodeBlock> blocks, String projectPath , int idFramework , String projectName ) throws Exception {
        String targetDir="";
        Path filePath = null ;
        for (CodeBlock block : blocks) {

            String layer = block.layer.trim();
            layer = layer.replaceAll("\\s", "") ;

            if ( idFramework == 1 ) {
                String subDir = "";
                if (layer.equalsIgnoreCase("Service") || (layer.equalsIgnoreCase("import") && block.className.endsWith("Service"))) {
                    subDir = "services";
                } else if (layer.equalsIgnoreCase("Repository") || (layer.equalsIgnoreCase("import") && block.className.endsWith("Repository"))) {
                    subDir = "repositories";
                }
                targetDir = projectPath + "/" + projectName + "/src/main/java/org/example/" + projectName + "/" + subDir;
                System.out.println(targetDir) ;
                filePath = Paths.get(targetDir, block.className + ".java");
            }

            if (block.layer.equals("import")) {
                injectImportInClass(filePath, block.code);
            } else {
                injectCodeInClass(filePath, block.code);
            }
        }
    }

    public void injectCodeInClass(Path filePath, String codeToInject) throws Exception {
        String content = Files.readString(filePath);
        int insertPos = content.lastIndexOf("}");
        if (insertPos == -1) {
            throw new Exception("Classe mal formée, accolade fermante non trouvée");
        }
        String before = content.substring(0, insertPos).trim();
        String after = content.substring(insertPos);

        String codeIndented = indentCode(codeToInject);
        String newContent = before + "\n\n" + codeIndented + "\n" + after;
        Files.writeString(filePath, newContent);
    }

    public void injectImportInClass(Path filePath, String importToInject) throws Exception {
        String content = Files.readString(filePath);

        if (!importToInject.trim().endsWith(";")) {
            importToInject = importToInject.trim() + ";";
        }
        if (content.contains(importToInject)) {
            System.out.println("Import déjà présent : " + importToInject);
            return;
        }
        int packageIndex = content.indexOf("package ");
        StringBuilder newContent = new StringBuilder();

        if (packageIndex != -1) {
            int endOfPackageLine = content.indexOf(";", packageIndex) + 1;

            newContent.append(content, 0, endOfPackageLine);
            newContent.append("\n\n");
            newContent.append(importToInject).append("\n");
            newContent.append(content.substring(endOfPackageLine).trim()).append("\n");
        } else {
            newContent.append(importToInject).append("\n\n").append(content);
        }
        Files.writeString(filePath, newContent.toString());
    }

    private String indentCode(String code) {
        StringBuilder sb = new StringBuilder();
        for (String line : code.split("\n")) {
            sb.append("    ").append(line).append("\n");
        }
        return sb.toString();
    }

}
