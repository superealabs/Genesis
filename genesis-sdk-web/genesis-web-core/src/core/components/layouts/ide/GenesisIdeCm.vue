<template>
    <div class="genesis-ide-cm">
        <!-- ═══ HEADER ═══ -->
        <div class="genesis-ide-cm__header">
            <div class="genesis-ide-cm__lang-badge">{{ language.toUpperCase() }}</div>
            <span v-if="filename" class="genesis-ide-cm__filename">{{ filename }}</span>
            <div class="genesis-ide-cm__spacer" />
            <slot name="actions" />
        </div>

        <!-- ═══ ÉDITEUR ═══ -->
        <div class="genesis-ide-cm__editor" ref="editorContainerRef" />

        <!-- ═══ FOOTER ═══ -->
        <div class="genesis-ide-cm__footer">
            <span class="genesis-ide-cm__stat">{{ lineCount }} lignes</span>
            <span class="genesis-ide-cm__stat">{{ charCount }} caractères</span>
            <span class="genesis-ide-cm__stat genesis-ide-cm__stat--muted">
                {{ readonly ? 'lecture seule' : 'éditable' }}
            </span>
        </div>
    </div>
</template>

<script setup lang="ts">
import {
    ref, computed, watch,
    onMounted, onBeforeUnmount,
    shallowRef
} from 'vue';

// ── CodeMirror core ──────────────────────────────────────────
import { EditorView, keymap, lineNumbers, highlightActiveLine } from '@codemirror/view';
import { EditorState, Compartment }                             from '@codemirror/state';
import { defaultKeymap, indentWithTab }                         from '@codemirror/commands';
import { sql }                                                  from '@codemirror/lang-sql';
import { json }                                                 from '@codemirror/lang-json';
import { oneDark }                                              from '@codemirror/theme-one-dark';

// ════════════════════════════════════════════════════════════
// Props & Emits
// ════════════════════════════════════════════════════════════
const props = withDefaults(defineProps<{
    modelValue:      string;
    language?:       'sql' | 'json' | 'plain';
    filename?:       string;
    showLineNumbers?: boolean;
    readonly?:       boolean;
    placeholder?:    string;
    isDark?:         boolean;
}>(), {
    language:        'sql',
    showLineNumbers: true,
    readonly:        false,
    placeholder:     '-- Entrez votre script SQL ici...',
    isDark:          false,
});

const emit = defineEmits<{
    'update:modelValue': [value: string];
}>();

// ════════════════════════════════════════════════════════════
// Stats
// ════════════════════════════════════════════════════════════
const lineCount = computed(() => (props.modelValue || '').split('\n').length);
const charCount = computed(() => (props.modelValue || '').length);

// ════════════════════════════════════════════════════════════
// Thème Genesis — construit à partir des CSS vars du document
// ════════════════════════════════════════════════════════════
function getCssVar(name: string): string {
    return getComputedStyle(document.documentElement)
        .getPropertyValue(name).trim();
}

function buildGenesisTheme(dark: boolean) {
    const bg       = dark ? 'hsl(175,100%,4%)'  : 'hsl(145,100%,92%)';
    const bgPanel  = dark ? 'hsl(175,100%,6%)'  : 'hsl(145,100%,94%)';
    const fg       = dark ? 'hsl(145,100%,94%)' : 'hsl(175,100%,4%)';
    const fgMuted  = dark ? 'hsl(145,100%,88%)' : 'hsl(175,100%,15%)';
    const accent   = getCssVar('--color-accent') || 'hsl(145,100%,44%)';
    const border   = dark ? 'hsl(175,100%,10%)' : 'hsl(160,100%,40%)';

    return EditorView.theme({
        // Conteneur principal — prend tout l'espace disponible
        '&': {
            height:          '100%',
            backgroundColor: bg,
            color:           fg,
            fontSize:        '13px',
            fontFamily:      "var(--font-mono, 'JetBrains Mono', 'Fira Code', monospace)",
        },

        // Scroller — C'est ICI que le scroll interne est défini
        '.cm-scroller': {
            overflow:   'auto',
            height:     '100%',
            fontFamily: "var(--font-mono, 'JetBrains Mono', 'Fira Code', monospace)",
            lineHeight: '1.6',
        },

        // Gouttière numéros de ligne
        '.cm-gutters': {
            backgroundColor: bgPanel,
            borderRight:     `1px solid ${border}`,
            color:           fgMuted,
            paddingRight:    '8px',
        },
        '.cm-lineNumbers .cm-gutterElement': {
            minWidth: '32px',
        },
        '.cm-activeLineGutter': {
            backgroundColor: 'transparent',
            color:           accent,
        },

        // Ligne active
        '.cm-activeLine': {
            backgroundColor: dark
                ? 'rgba(255,255,255,0.04)'
                : 'rgba(0,0,0,0.03)',
        },

        // Curseur
        '.cm-cursor': {
            borderLeftColor: accent,
        },

        // Sélection
        '.cm-selectionBackground, ::selection': {
            backgroundColor: dark
                ? 'rgba(145,255,145,0.15)'
                : 'rgba(0,120,60,0.15)',
        },

        // Placeholder
        '.cm-placeholder': {
            color:   fgMuted,
            opacity: '0.5',
        },

        // Scrollbars webkit
        '.cm-scroller::-webkit-scrollbar': {
            width: '8px', height: '8px',
        },
        '.cm-scroller::-webkit-scrollbar-track': {
            background: 'transparent',
        },
        '.cm-scroller::-webkit-scrollbar-thumb': {
            backgroundColor: border,
            borderRadius:    '9999px',
            border:          '2px solid transparent',
            backgroundClip:  'content-box',
        },
        '.cm-scroller::-webkit-scrollbar-thumb:hover': {
            backgroundColor: accent,
        },
    }, { dark });
}

// Tokens SQL colorés avec la couleur accent du thème
function buildSyntaxHighlight(dark: boolean) {
    const accent  = getCssVar('--color-accent') || 'hsl(145,100%,44%)';
    const fg      = dark ? 'hsl(145,100%,94%)' : 'hsl(175,100%,4%)';
    const fgMuted = dark ? 'hsl(145,100%,88%)' : 'hsl(175,100%,15%)';

    // On surcharge les classes hl- de CodeMirror
    return EditorView.baseTheme({
        '.ql-keyword, .tok-keyword':  { color: accent,  fontWeight: '600' },
        '.tok-string':                { color: fg,       opacity: '0.85'   },
        '.tok-number':                { color: fgMuted                     },
        '.tok-comment':               { color: fgMuted,  fontStyle: 'italic', opacity: '0.6' },
        '.tok-operator':              { color: fgMuted,  opacity: '0.8'    },
        '.tok-typeName':              { color: accent,   opacity: '0.8'    },
        '.tok-name':                  { color: fg                          },
    });
}

// ════════════════════════════════════════════════════════════
// Instance CodeMirror
// ════════════════════════════════════════════════════════════
const editorContainerRef = ref<HTMLDivElement | null>(null);
const editorView         = shallowRef<EditorView | null>(null);

// Compartiments reconfigurables à chaud
const themeCompartment    = new Compartment();
const readonlyCompartment = new Compartment();
const langCompartment     = new Compartment();

function getLangExtension() {
    if (props.language === 'sql')  return sql();
    if (props.language === 'json') return json();
    return [];
}

// Mise à jour externe → éditeur (évite la boucle infinie)
let internalChange = false;

function initEditor() {
    if (!editorContainerRef.value) return;

    const state = EditorState.create({
        doc: props.modelValue || '',
        extensions: [
            // Langue
            langCompartment.of(getLangExtension()),

            // Thème
            themeCompartment.of([
                buildGenesisTheme(props.isDark),
                buildSyntaxHighlight(props.isDark),
            ]),

            // Readonly
            readonlyCompartment.of(EditorState.readOnly.of(props.readonly)),

            // Features
            props.showLineNumbers ? lineNumbers() : [],
            highlightActiveLine(),
            keymap.of([...defaultKeymap, indentWithTab]),

            // Listener de changement → v-model
            EditorView.updateListener.of((update) => {
                if (update.docChanged) {
                    internalChange = true;
                    emit('update:modelValue', update.state.doc.toString());
                    internalChange = false;
                }
            }),

            // Placeholder
            EditorView.contentAttributes.of({ 'data-placeholder': props.placeholder || '' }),
        ],
    });

    editorView.value = new EditorView({
        state,
        parent: editorContainerRef.value,
    });
}

// ════════════════════════════════════════════════════════════
// Watchers
// ════════════════════════════════════════════════════════════

// Sync modèle externe → éditeur (sans boucle)
watch(() => props.modelValue, (newVal) => {
    if (internalChange) return;
    const view = editorView.value;
    if (!view) return;
    const current = view.state.doc.toString();
    if (current === newVal) return;

    view.dispatch({
        changes: { from: 0, to: current.length, insert: newVal || '' },
    });
});

// Reconfiguration du thème à chaud
watch(() => props.isDark, (dark) => {
    editorView.value?.dispatch({
        effects: themeCompartment.reconfigure([
            buildGenesisTheme(dark),
            buildSyntaxHighlight(dark),
        ]),
    });
});

// Reconfiguration readonly à chaud
watch(() => props.readonly, (ro) => {
    editorView.value?.dispatch({
        effects: readonlyCompartment.reconfigure(EditorState.readOnly.of(ro)),
    });
});

// ════════════════════════════════════════════════════════════
// Lifecycle
// ════════════════════════════════════════════════════════════
onMounted(() => initEditor());
onBeforeUnmount(() => editorView.value?.destroy());
</script>

<style scoped>
/* ══════════════════════════════════════
   Conteneur principal
══════════════════════════════════════ */
.genesis-ide-cm {
    display:        flex;
    flex-direction: column;
    height:         100%;      /* Prend toute la hauteur du parent */
    min-height:     0;         /* Indispensable dans un flex parent */
    border:         1px solid var(--color-secondary);
    border-radius:  8px;
    overflow:       hidden;
    background:     var(--color-bg-dark);
    font-family:    var(--font-mono);
    font-size:      13px;
}

/* ══════════════════════════════════════
   Header
══════════════════════════════════════ */
.genesis-ide-cm__header {
    display:         flex;
    align-items:     center;
    gap:             10px;
    padding:         6px 12px;
    background:      var(--color-bg);
    border-bottom:   1px solid var(--color-secondary);
    flex-shrink:     0;
}

.genesis-ide-cm__lang-badge {
    font-size:      10px;
    font-weight:    700;
    letter-spacing: 0.08em;
    padding:        2px 7px;
    border-radius:  4px;
    background:     var(--color-accent);
    color:          var(--color-bg-dark);
    font-family:    var(--font-mono);
}

.genesis-ide-cm__filename {
    font-size:   11px;
    color:       var(--color-text-muted);
    font-family: var(--font-mono);
    opacity:     0.8;
}

.genesis-ide-cm__spacer { flex: 1; }

/* ══════════════════════════════════════
   Zone éditeur — clé du scroll interne
══════════════════════════════════════ */
.genesis-ide-cm__editor {
    flex:       1;
    min-height: 0;    /* ← Sans ça, flex ignore la contrainte de hauteur */
    overflow:   hidden;
}

/*
   CodeMirror génère un div.cm-editor directement dans .genesis-ide-cm__editor.
   On force ce div à prendre toute la hauteur disponible.
*/
.genesis-ide-cm__editor :deep(.cm-editor) {
    height: 100%;
}

.genesis-ide-cm__editor :deep(.cm-scroller) {
    overflow: auto;   /* Le scroll est ici, nulle part ailleurs */
}

/* ══════════════════════════════════════
   Footer
══════════════════════════════════════ */
.genesis-ide-cm__footer {
    display:       flex;
    align-items:   center;
    gap:           16px;
    padding:       4px 12px;
    background:    var(--color-bg);
    border-top:    1px solid var(--color-secondary);
    flex-shrink:   0;
}

.genesis-ide-cm__stat {
    font-size: 11px;
    color:     var(--color-text-muted);
    opacity:   0.7;
}

.genesis-ide-cm__stat--muted {
    margin-left: auto;
    opacity:     0.4;
}
</style>