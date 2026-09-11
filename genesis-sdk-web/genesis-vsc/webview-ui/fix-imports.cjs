const fs = require('fs');
const path = require('path');

const SRC_DIR = path.resolve(__dirname, 'src');

function processFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf8');
    const originalContent = content;
    
    // Pattern pour capturer les imports avec chemins relatifs
    // Gère : import, export, et les différents types de quotes
    const importRegex = /(import|export)\s+(?:(?:\{[^}]*\}|\*\s+as\s+\w+|\w+)(?:\s*,\s*(?:\{[^}]*\}|\*\s+as\s+\w+|\w+))*\s+from\s+)?['"]([^'"]+)['"]/g;
    
    // Pattern pour les imports dynamiques
    const dynamicImportRegex = /import\s*\(\s*['"]([^'"]+)['"]\s*\)/g;
    
    // Calculer le chemin relatif du fichier par rapport à src/
    const relativePath = path.relative(SRC_DIR, path.dirname(filePath));
    const depth = relativePath === '' ? 0 : relativePath.split(path.sep).length;
    
    // Fonction de remplacement pour les imports statiques
    content = content.replace(importRegex, (match, type, importPath) => {
        // Si ce n'est pas un chemin relatif, on ne touche pas
        if (!importPath.startsWith('.')) return match;
        
        // Résoudre le chemin absolu de l'import
        const absoluteImportPath = path.resolve(path.dirname(filePath), importPath);
        
        // Calculer le chemin relatif par rapport à src/
        let newImportPath = path.relative(SRC_DIR, absoluteImportPath);
        
        // Normaliser les séparateurs pour Windows
        newImportPath = newImportPath.replace(/\\/g, '/');
        
        // Ajouter @/ au début
        newImportPath = '@/' + newImportPath;
        
        // Reconstruire l'import
        return match.replace(importPath, newImportPath);
    });
    
    // Fonction de remplacement pour les imports dynamiques
    content = content.replace(dynamicImportRegex, (match, importPath) => {
        if (!importPath.startsWith('.')) return match;
        
        const absoluteImportPath = path.resolve(path.dirname(filePath), importPath);
        let newImportPath = path.relative(SRC_DIR, absoluteImportPath);
        newImportPath = newImportPath.replace(/\\/g, '/');
        newImportPath = '@/' + newImportPath;
        
        return match.replace(importPath, newImportPath);
    });
    
    // Si le contenu a changé, écrire le fichier
    if (content !== originalContent) {
        fs.writeFileSync(filePath, content, 'utf8');
        console.log(`✅ Modifié : ${path.relative(process.cwd(), filePath)}`);
        return true;
    }
    return false;
}

function scanDirectory(dir) {
    const files = fs.readdirSync(dir);
    let modified = 0;
    
    for (const file of files) {
        const filePath = path.join(dir, file);
        const stat = fs.statSync(filePath);
        
        if (stat.isDirectory()) {
            modified += scanDirectory(filePath);
        } else if (file.endsWith('.vue') || file.endsWith('.ts') || file.endsWith('.tsx')) {
            if (processFile(filePath)) {
                modified++;
            }
        }
    }
    
    return modified;
}

console.log('🔍 Scan des fichiers...');
const modified = scanDirectory(SRC_DIR);
console.log(`\n🎉 Terminé ! ${modified} fichier(s) modifié(s).`);