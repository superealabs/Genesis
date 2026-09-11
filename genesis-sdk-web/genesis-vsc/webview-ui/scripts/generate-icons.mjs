import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const SVG_DIR = path.resolve(__dirname, '../src/assets/ICONS');
const OUT_DIR = path.resolve(__dirname, '../src/core/components/ui/icons');

function toUpperCamel(str) {
    return str
        .split(/[-_]/)
        .map(part => part.charAt(0).toUpperCase() + part.slice(1))
        .join('');
}

function extractSvgContent(svgRaw) {
    // Extraire uniquement le contenu intérieur du <svg> (paths, circles, etc.)
    const innerMatch = svgRaw.match(/<svg[^>]*>([\s\S]*?)<\/svg>/i);
    return innerMatch ? innerMatch[1].trim() : '';
}

function generateComponent(innerSvg) {
    return `<template>
    <svg 
        xmlns="http://www.w3.org/2000/svg" 
        :style="{ width: \`\${size}px\`, height: \`\${size}px\` }"
        viewBox="0 0 24 24" 
        :fill="color || 'currentColor'"
        aria-hidden="true"
    >
        ${innerSvg}
    </svg>
</template>

<script setup lang="ts">
interface Props {
    size?: number;
    color?: string;
}

withDefaults(defineProps<Props>(), {
    size: 16,
    color: undefined
});
</script>
`;
}

// Créer le dossier output si nécessaire
if (!fs.existsSync(OUT_DIR)) {
    fs.mkdirSync(OUT_DIR, { recursive: true });
}

// Vérifier que le dossier SVG existe
if (!fs.existsSync(SVG_DIR)) {
    console.error(`❌ Dossier SVG introuvable : ${SVG_DIR}`);
    process.exit(1);
}

const files = fs.readdirSync(SVG_DIR).filter(f => f.endsWith('.svg'));

if (files.length === 0) {
    console.log('⚠️  Aucun fichier SVG trouvé dans', SVG_DIR);
    process.exit(0);
}

let generated = 0;
let replaced = 0;

for (const file of files) {
    const name = path.basename(file, '.svg');
    const componentName = `Icon${toUpperCamel(name)}`;
    const outPath = path.join(OUT_DIR, `${componentName}.vue`);

    const svgRaw = fs.readFileSync(path.join(SVG_DIR, file), 'utf8');
    const innerSvg = extractSvgContent(svgRaw);

    if (!innerSvg) {
        console.warn(`⚠️  SVG vide ou invalide : ${file}`);
        continue;
    }

    const exists = fs.existsSync(outPath);
    fs.writeFileSync(outPath, generateComponent(innerSvg), 'utf8');

    if (exists) {
        console.log(`♻️  Remplacé  : ${componentName}.vue`);
        replaced++;
    } else {
        console.log(`✅ Généré    : ${componentName}.vue`);
        generated++;
    }
}

console.log(`\n📦 ${generated} généré(s), ${replaced} remplacé(s) sur ${files.length} fichier(s) SVG.`);