<template>
    <div class="genesis-ide" :class="{ 'genesis-ide--dark': isDark }">
        <!-- ═══ HEADER ═══ -->
        <div class="genesis-ide__header">
            <div class="genesis-ide__lang-badge">SQL</div>
            <span v-if="filename" class="genesis-ide__filename">{{ filename }}</span>
            <div class="genesis-ide__spacer" />
            <slot name="actions" />
        </div>

        <!-- ═══ CORPS : numéros + code ═══ -->
        <div class="genesis-ide__body" @scroll="syncScroll" ref="bodyRef">
            <!-- Numéros de ligne (optionnel via prop) -->
            <div v-if="showLineNumbers" class="genesis-ide__gutter" ref="gutterRef">
                <div
                    v-for="n in lineCount"
                    :key="n"
                    class="genesis-ide__line-number"
                    :class="{ 'genesis-ide__line-number--active': n === activeLine }"
                >
                    {{ n }}
                </div>
            </div>

            <!-- Zone éditable -->
            <div class="genesis-ide__editor-wrap">
                <!-- Couche de mise en forme (derrière) -->
                <pre
                    class="genesis-ide__highlight"
                    aria-hidden="true"
                    ref="highlightRef"
                ><code v-html="highlighted" /></pre>

                <!-- Textarea transparent (devant) -->
                <textarea
                    ref="textareaRef"
                    class="genesis-ide__textarea"
                    :value="modelValue"
                    :readonly="readonly"
                    :placeholder="placeholder"
                    spellcheck="false"
                    autocorrect="off"
                    autocapitalize="off"
                    @input="onInput"
                    @keydown="onKeydown"
                    @click="updateActiveLine"
                    @keyup="updateActiveLine"
                    @scroll="syncHighlight"
                />
            </div>
        </div>

        <!-- ═══ FOOTER ═══ -->
        <div class="genesis-ide__footer">
            <span class="genesis-ide__stat">{{ lineCount }} lignes</span>
            <span class="genesis-ide__stat">{{ charCount }} caractères</span>
            <span v-if="!readonly" class="genesis-ide__stat genesis-ide__stat--muted">éditable</span>
            <span v-else class="genesis-ide__stat genesis-ide__stat--muted">lecture seule</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue';

// ═══ Props ═══
const props = withDefaults(defineProps<{
    modelValue: string;
    language?: 'sql' | 'json' | 'plain';
    filename?: string;
    showLineNumbers?: boolean;
    readonly?: boolean;
    placeholder?: string;
    isDark?: boolean;          // si non fourni, suit les variables CSS du thème courant
}>(), {
    language: 'sql',
    showLineNumbers: true,
    readonly: false,
    placeholder: '-- Entrez votre script SQL ici...',
    isDark: false,
});

const emit = defineEmits<{
    'update:modelValue': [value: string];
}>();

// ═══ Refs DOM ═══
const textareaRef = ref<HTMLTextAreaElement | null>(null);
const highlightRef = ref<HTMLPreElement | null>(null);
const gutterRef    = ref<HTMLDivElement | null>(null);
const bodyRef      = ref<HTMLDivElement | null>(null);

// ═══ État ═══
const activeLine = ref(1);

// ═══ Computed ═══
const lineCount = computed(() => (props.modelValue || '').split('\n').length);
const charCount = computed(() => (props.modelValue || '').length);

// ─── Tokenizer SQL ───────────────────────────────────────────
const SQL_KEYWORDS = new Set([
    'SELECT','FROM','WHERE','AND','OR','NOT','IN','IS','NULL','LIKE',
    'JOIN','LEFT','RIGHT','INNER','OUTER','FULL','CROSS','ON',
    'INSERT','INTO','VALUES','UPDATE','SET','DELETE','TRUNCATE',
    'CREATE','TABLE','VIEW','INDEX','DROP','ALTER','ADD','COLUMN',
    'PRIMARY','KEY','FOREIGN','REFERENCES','UNIQUE','DEFAULT',
    'GROUP','BY','ORDER','HAVING','LIMIT','OFFSET','UNION','ALL',
    'DISTINCT','AS','CASE','WHEN','THEN','ELSE','END',
    'BEGIN','COMMIT','ROLLBACK','TRANSACTION',
    'COUNT','SUM','AVG','MIN','MAX','COALESCE','NULLIF','CAST','CONVERT',
    'DATE','NOW','CURRENT_TIMESTAMP','CONCAT','SUBSTRING','TRIM','UPPER','LOWER',
    'EXISTS','WITH','RECURSIVE','OVER','PARTITION','ROW_NUMBER','RANK',
    'INT','INTEGER','VARCHAR','TEXT','BOOLEAN','FLOAT','DECIMAL','DATETIME','TIMESTAMP',
]);

function tokenizeSQL(code: string): string {
    // ═══ PASSE 1 : Segmenter le code en tokens AVANT d'échapper ═══
    // On travaille sur le texte brut, puis on échappe token par token

    const result: string[] = [];
    let remaining = code;

    // Regex ordonnées par priorité (commentaires et strings en premier)
    const patterns: [RegExp, string][] = [
        [/^(--[^\n]*)/, 'comment'],
        [/^(\/\*[\s\S]*?\*\/)/, 'comment'],
        [/^('(?:[^'\\]|\\.)*')/, 'string'],
        [/^(\b\d+(?:\.\d+)?\b)/, 'number'],
        [/^([=<>!]+|\*|,|;|\(|\))/, 'operator'],
        [/^([A-Za-z_][A-Za-z0-9_]*)/, 'word'],
        [/^(\s+)/, 'whitespace'],
        [/^(.)/, 'other'],             // fallback caractère par caractère
    ];

    while (remaining.length > 0) {
        let matched = false;

        for (const [regex, type] of patterns) {
            const m = remaining.match(regex);
            if (!m) continue;

            const raw = m[1];
            // Échapper le HTML du token brut
            const safe = raw
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');

            if (type === 'comment') {
                result.push(`<span class="ide-comment">${safe}</span>`);
            } else if (type === 'string') {
                result.push(`<span class="ide-string">${safe}</span>`);
            } else if (type === 'number') {
                result.push(`<span class="ide-number">${safe}</span>`);
            } else if (type === 'operator') {
                result.push(`<span class="ide-operator">${safe}</span>`);
            } else if (type === 'word') {
                if (SQL_KEYWORDS.has(raw.toUpperCase())) {
                    result.push(`<span class="ide-keyword">${safe}</span>`);
                } else {
                    result.push(safe);
                }
            } else {
                result.push(safe);
            }

            remaining = remaining.slice(m[0].length);
            matched = true;
            break;
        }

        if (!matched) {
            result.push(remaining[0]);
            remaining = remaining.slice(1);
        }
    }

    return result.join('');
}

function tokenizeJSON(code: string): string {
    const result: string[] = [];
    let remaining = code;

    const patterns: [RegExp, string][] = [
        [/^("(?:[^"\\]|\\.)*")\s*(?=:)/, 'key'],      // clé d'objet
        [/^("(?:[^"\\]|\\.)*")/, 'string'],
        [/^(-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)/, 'number'],
        [/^(true|false|null)/, 'literal'],
        [/^([{}\[\],:\s]+)/, 'other'],
        [/^(.)/, 'other'],
    ];

    while (remaining.length > 0) {
        let matched = false;

        for (const [regex, type] of patterns) {
            const m = remaining.match(regex);
            if (!m) continue;

            const raw = m[1] ?? m[0];
            const safe = raw
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');

            if (type === 'key') {
                result.push(`<span class="ide-keyword">${safe}</span>`);
                // Ajouter ce qui suit la clé (le : et espaces)
                result.push(m[0].slice(raw.length));
            } else if (type === 'string') {
                result.push(`<span class="ide-string">${safe}</span>`);
            } else if (type === 'number' || type === 'literal') {
                result.push(`<span class="ide-number">${safe}</span>`);
            } else {
                result.push(safe);
            }

            remaining = remaining.slice(m[0].length);
            matched = true;
            break;
        }

        if (!matched) {
            result.push(remaining[0]);
            remaining = remaining.slice(1);
        }
    }

    return result.join('');
}

const highlighted = computed(() => {
    const code = props.modelValue || '';
    if (props.language === 'sql')  return tokenizeSQL(code);
    if (props.language === 'json') return tokenizeJSON(code);
    // plain : juste escape
    return code.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
});

// ═══ Synchronisation scroll textarea ↔ highlight ═══
function syncHighlight() {
    if (!textareaRef.value || !highlightRef.value) return;
    highlightRef.value.scrollTop  = textareaRef.value.scrollTop;
    highlightRef.value.scrollLeft = textareaRef.value.scrollLeft;
}

function syncScroll() {
    if (!bodyRef.value || !gutterRef.value) return;
    gutterRef.value.scrollTop = bodyRef.value.scrollTop;
}

// ═══ Handlers ═══
function onInput(e: Event) {
    const val = (e.target as HTMLTextAreaElement).value;
    emit('update:modelValue', val);
    nextTick(() => syncHighlight());
}

function updateActiveLine() {
    if (!textareaRef.value) return;
    const text = textareaRef.value.value.substring(0, textareaRef.value.selectionStart);
    activeLine.value = text.split('\n').length;
}

function onKeydown(e: KeyboardEvent) {
    // Tab → insérer 2 espaces au lieu de changer de focus
    if (e.key === 'Tab') {
        e.preventDefault();
        const ta = textareaRef.value!;
        const start = ta.selectionStart;
        const end   = ta.selectionEnd;
        const val   = ta.value;
        const newVal = val.substring(0, start) + '  ' + val.substring(end);
        emit('update:modelValue', newVal);
        nextTick(() => {
            ta.selectionStart = ta.selectionEnd = start + 2;
            syncHighlight();
        });
    }
}

// ═══ Watch pour resync au changement externe ═══
watch(() => props.modelValue, () => {
    nextTick(() => syncHighlight());
});

onMounted(() => syncHighlight());
</script>

<style scoped>
/* ══════════════════════════════════════════
   CONTENEUR PRINCIPAL
══════════════════════════════════════════ */
.genesis-ide {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    border: 1px solid var(--color-secondary);
    border-radius: 8px;
    overflow: hidden;
    background: var(--color-bg-dark);
    font-family: var(--font-mono);
    font-size: 13px;
    line-height: 1.6;
}

/* ══════════════════════════════════════════
   HEADER
══════════════════════════════════════════ */
.genesis-ide__header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 6px 12px;
    background: var(--color-bg);
    border-bottom: 1px solid var(--color-secondary);
    flex-shrink: 0;
}

.genesis-ide__lang-badge {
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 0.08em;
    padding: 2px 7px;
    border-radius: 4px;
    background: var(--color-accent);
    color: var(--color-bg-dark);
    font-family: var(--font-mono);
}

.genesis-ide__filename {
    font-size: 11px;
    color: var(--color-text-muted);
    font-family: var(--font-mono);
    opacity: 0.8;
}

.genesis-ide__spacer { flex: 1; }

/* ══════════════════════════════════════════
   CORPS
══════════════════════════════════════════ */
.genesis-ide__body {
    display: flex;
    flex: 1;
    min-height: 0;
    overflow: hidden;       /* Le scroll est géré en interne */
    position: relative;
}

/* ── Gouttière numéros de ligne ── */
.genesis-ide__gutter {
    flex-shrink: 0;
    width: 46px;
    overflow: hidden;       /* Synchronisé via JS */
    padding: 12px 0;
    background: var(--color-bg);
    border-right: 1px solid var(--color-secondary);
    user-select: none;
}

.genesis-ide__line-number {
    text-align: right;
    padding-right: 10px;
    color: var(--color-text-muted);
    opacity: 0.4;
    font-size: 12px;
    line-height: 1.6;
    transition: opacity 0.1s;
}

.genesis-ide__line-number--active {
    opacity: 1;
    color: var(--color-accent);
}

/* ── Éditeur (superposition textarea / highlight) ── */
.genesis-ide__editor-wrap {
    flex: 1;
    position: relative;
    min-width: 0;
    overflow: hidden;
}

/* Couche highlight (derrière) */
.genesis-ide__highlight {
    position: absolute;
    inset: 0;
    margin: 0;
    padding: 12px 16px;
    overflow: hidden;       /* Le textarea gère le scroll visible */
    white-space: pre;
    word-break: normal;
    pointer-events: none;
    font-family: var(--font-mono);
    font-size: 13px;
    line-height: 1.6;
    color: var(--color-text);
    background: transparent;
    tab-size: 2;
}

.genesis-ide__highlight code {
    font-family: inherit;
    font-size: inherit;
    line-height: inherit;
    background: transparent;
}

/* Textarea transparent (devant) */
.genesis-ide__textarea {
    position: absolute;
    inset: 0;
    padding: 12px 16px;
    width: 100%;
    height: 100%;
    resize: none;
    border: none;
    outline: none;
    background: transparent;
    color: transparent;         /* Texte invisible : la couleur vient du highlight */
    caret-color: var(--color-accent);
    font-family: var(--font-mono);
    font-size: 13px;
    line-height: 1.6;
    tab-size: 2;
    white-space: pre;
    overflow: auto;             /* ← Scroll ici uniquement */
    scrollbar-width: thin;
    scrollbar-color: var(--color-secondary) transparent;
}

.genesis-ide__textarea::placeholder {
    color: var(--color-text-muted);
    opacity: 0.4;
}

.genesis-ide__textarea::-webkit-scrollbar       { width: 8px; height: 8px; }
.genesis-ide__textarea::-webkit-scrollbar-track  { background: transparent; }
.genesis-ide__textarea::-webkit-scrollbar-thumb  {
    background-color: var(--color-secondary);
    border-radius: 9999px;
    border: 2px solid transparent;
    background-clip: content-box;
}
.genesis-ide__textarea::-webkit-scrollbar-thumb:hover {
    background-color: var(--color-accent);
}

/* ══════════════════════════════════════════
   FOOTER
══════════════════════════════════════════ */
.genesis-ide__footer {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 4px 12px;
    background: var(--color-bg);
    border-top: 1px solid var(--color-secondary);
    flex-shrink: 0;
}

.genesis-ide__stat {
    font-size: 11px;
    color: var(--color-text-muted);
    opacity: 0.7;
}

.genesis-ide__stat--muted {
    margin-left: auto;
    opacity: 0.4;
}

/* ══════════════════════════════════════════
   TOKENS SQL / JSON
   (utilisent les CSS vars du thème courant)
══════════════════════════════════════════ */
:deep(.ide-keyword)  { color: var(--color-accent);    font-weight: 600; }
:deep(.ide-string)   { color: var(--color-text);      opacity: 0.85; }
:deep(.ide-number)   { color: var(--color-text-muted); }
:deep(.ide-comment)  { color: var(--color-text-muted); opacity: 0.5; font-style: italic; }
:deep(.ide-operator) { color: var(--color-text-muted); opacity: 0.7; }

/* ══════════════════════════════════════════
   VARIANTE SOMBRE FORCÉE (prop isDark)
══════════════════════════════════════════ */
.genesis-ide--dark {
    --color-bg-dark: hsl(175, 100%, 4%);
    --color-bg:      hsl(175, 100%, 6%);
    --color-text:    hsl(145, 100%, 94%);
}
</style>