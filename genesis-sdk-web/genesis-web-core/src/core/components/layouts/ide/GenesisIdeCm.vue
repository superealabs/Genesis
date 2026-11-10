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

import {
    HighlightStyle,
    syntaxHighlighting,
} from '@codemirror/language';

import { tags } from '@lezer/highlight';

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
// const getCssVar = (name: string) => {
//     return getComputedStyle(document.body)
//         .getPropertyValue(name)
//         .trim();
// };

function buildGenesisTheme(dark: boolean) {
    return EditorView.theme({
        '&': {
            height: '100%',
            backgroundColor: 'var(--color-bg-dark, #ffffff)',
            color: 'var(--color-text, #111111)',
            fontSize: '13px',
            fontFamily: 'var(--font-mono, monospace)',
        },

        '.cm-scroller': {
            overflow: 'auto',
            height: '100%',
            fontFamily: 'var(--font-mono, monospace)',
            lineHeight: '1.6',
        },

        '.cm-gutters': {
            backgroundColor: 'var(--color-bg-dark, var(--color-bg-dark))',
            color: 'var(--color-text-muted, #888888)',
            border: 'none',
            paddingRight: '8px',
        },

        '.cm-lineNumbers .cm-gutterElement': {
            minWidth: '32px',
        },

        '.cm-activeLineGutter': {
            backgroundColor: 'transparent',
            color: 'var(--color-accent, #00aa66)',
        },

        '.cm-activeLine': {
            backgroundColor: 'color-mix(in srgb, var(--color-secondary) 8%, transparent)',
        },

        '.cm-cursor': {
            borderLeftColor: 'var(--color-neutral-light-genesis, #00aa66)',
        },

        '.cm-selectionBackground, ::selection': {
            backgroundColor: 'color-mix(in srgb, var(--color-secondary) 25%, transparent)',
        },

        '&.cm-focused .cm-selectionBackground': {
            backgroundColor: 'color-mix(in srgb, var(--color-secondary) 30%, transparent)',
        },

        '.cm-placeholder': {
            color: 'var(--color-text-muted, #888888)',
            opacity: '0.6',
        },

        '.cm-scroller::-webkit-scrollbar': {
            width: '8px',
            height: '8px',
        },

        '.cm-scroller::-webkit-scrollbar-track': {
            background: 'transparent',
        },

        '.cm-scroller::-webkit-scrollbar-thumb': {
            backgroundColor: 'var(--color-bg-neutral-genesis, #aaaaaa)',
            borderRadius: '9999px',
            border: '2px solid transparent',
            backgroundClip: 'content-box',
        },

        '.cm-scroller::-webkit-scrollbar-thumb:hover': {
            backgroundColor: 'var(--color-accent, #00aa66)',
        },

    }, { dark });
}

// Tokens SQL colorés avec la couleur accent du thème
function buildSyntaxHighlight() {
    return syntaxHighlighting(
        HighlightStyle.define([
            // Mots-clés SQL
            {
                tag: tags.keyword,
                color: 'var(--color-primary)',
                fontWeight: '600',
            },

            // Chaînes de caractères
            {
                tag: tags.string,
                color: 'var(--color-accent, var(--color-text))',
            },

            // Nombres
            {
                tag: tags.number,
                color: 'var(--color-accent, var(--color-text))',
            },

            // Commentaires
            {
                tag: tags.comment,
                color: 'var(--color-text-muted)',
                fontStyle: 'italic',
            },

            // Opérateurs
            {
                tag: tags.operator,
                color: 'var(--color-text-muted)',
            },

            // Types
            {
                tag: tags.typeName,
                color: 'var(--color-secondary, var(--color-accent))',
            },

            // Propriétés JSON
            {
                tag: tags.propertyName,
                color: 'var(--color-accent-text, var(--color-text))',
            },
            // Booléens et valeurs null
            {
                tag: [tags.bool, tags.null],
                color: 'var(--color-accent)',
                fontWeight: '600',
            },
        ])
    );
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
                buildSyntaxHighlight(),
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
            buildSyntaxHighlight(),
        ]),
    });
});

// Reconfiguration readonly à chaud
watch(() => props.readonly, (ro) => {
    editorView.value?.dispatch({
        effects: readonlyCompartment.reconfigure(EditorState.readOnly.of(ro)),
    });
});

watch(() => props.language, () => {
    editorView.value?.dispatch({
        effects: langCompartment.reconfigure(getLangExtension()),
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
    border:         none;
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
    border-bottom:   2px solid var(--color-bg-neutral-genesis);
    flex-shrink:     0;
}

.genesis-ide-cm__lang-badge {
    font-size:      8px;
    font-weight:    700;
    letter-spacing: 0.08em;
    padding:        2px 8px;
    border-radius:  4px;
    background:     var(--color-bg-secondary);
    color:          var(--color-secondary);
    font-family:    var(--font-body);
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
    background:    var(--color-bg-dark);
    border-top:    2px solid var(--color-bg-neutral-genesis);
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