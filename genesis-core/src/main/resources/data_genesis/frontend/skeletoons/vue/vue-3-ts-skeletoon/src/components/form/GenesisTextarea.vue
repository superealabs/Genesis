<template>
  <div
    :class="[
      'flex',
      { 'flex-col items-start w-auto': !rowInput },
      { 'items-start gap-2': rowInput },
    ]"
  >
    <div class="flex gap-2 bg-transparent pt-2">
      <label
        v-if="label || $slots.label"
        :for="inputFormId"
        class="label font-medium"
        :class="{ 'whitespace-nowrap': rowInput }"
      >
        <slot name="label">{{ label }}</slot>

        <span v-if="mandatory" class="text-error">*</span>
      </label>
    </div>

    <div class="flex-grow w-full">
      <div class="relative w-full">
        <!-- 1. CkEditor -->
        <CkeditorComponent
          v-if="isCkeditor"
          :editor="editor"
          :model-value="modelValue ?? ''"
          :config="editorConfig"
          v-bind="$attrs"
          @update:model-value="onCkeditorInput"
        />

        <!-- 2. JSON CodeMirror Editor avec Coloration Syntaxique -->
        <div v-else-if="isJsonEditor" class="json-editor-container w-full">
          <div class="flex justify-between items-center mb-1 text-xs text-base-content/70">
            <span class="font-semibold">JSON Editor</span>
            <button
              type="button"
              class="btn btn-ghost btn-xs text-primary hover:bg-base-200"
              @click="formatJson"
            >
              ✨ Formater le JSON
            </button>
          </div>

          <div
            class="codemirror-wrapper border rounded-lg overflow-hidden"
            :class="{ 'border-error': jsonError, 'border-base-300': !jsonError }"
          >
            <Codemirror
              :model-value="jsonStringValue"
              :style="{ height: editorHeight }"
              :extensions="codemirrorExtensions"
              :tab-size="2"
              @change="onJsonChange"
            />
          </div>

          <span v-if="jsonError" class="text-xs text-error mt-1 block">
            ⚠️ {{ jsonError }}
          </span>
        </div>

        <!-- 3. Textarea HTML natif -->
        <textarea
          v-else
          v-bind="$attrs"
          :id="inputFormId"
          :class="textareaClasses"
          :rows="rows"
          :value="modelValue ?? ''"
          @input="onInput"
        />
      </div>

      <ErrorMessage v-if="violation" :message="violation" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import ErrorMessage from '@/components/common/ErrorMessage.vue'

// --- Imports CkEditor ---
import { Ckeditor as CkeditorComponent } from '@ckeditor/ckeditor5-vue'
import {
  ClassicEditor,
  Essentials,
  Paragraph,
  Bold,
  Italic,
  Link,
  List,
  Heading,
  Undo,
} from 'ckeditor5'
import 'ckeditor5/ckeditor5.css'

// --- Imports CodeMirror (JSON Editor) ---
import { Codemirror } from 'vue-codemirror'
import { json } from '@codemirror/lang-json'
import { oneDark } from '@codemirror/theme-one-dark'

defineOptions({
  inheritAttrs: false,
})

const props = withDefaults(
  defineProps<{
    modelValue?: string | Record<string, unknown> | Array<unknown> | null
    label?: string
    inputId?: string
    rowInput?: boolean
    violation?: string
    mandatory?: boolean
    rows?: number | string
    isCkeditor?: boolean
    isJsonEditor?: boolean
    darkModeJson?: boolean
    editorConfig?: Record<string, unknown>
  }>(),
  {
    modelValue: '',
    rows: 5,
    isCkeditor: false,
    isJsonEditor: false,
    darkModeJson: false,
    editorConfig: () => ({}),
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'update:model-value', value: string): void
}>()

/* ----------------------------------------------------
 * SECTION CKEDITOR
 * ---------------------------------------------------- */
const editor = ClassicEditor

const defaultEditorConfig = {
  licenseKey: 'GPL',
  plugins: [Essentials, Paragraph, Bold, Italic, Link, List, Heading, Undo],
  toolbar: [
    'undo',
    'redo',
    '|',
    'heading',
    '|',
    'bold',
    'italic',
    'link',
    '|',
    'bulletedList',
    'numberedList',
  ],
}

const editorConfig = computed(() => ({
  ...defaultEditorConfig,
  ...props.editorConfig,
}))

/* ----------------------------------------------------
 * SECTION JSON EDITOR (CodeMirror)
 * ---------------------------------------------------- */
const jsonError = ref<string | null>(null)

// Hauteur dynamique selon les `rows` spécifiés
const editorHeight = computed(() => {
  const rowCount = typeof props.rows === 'number' ? props.rows : parseInt(String(props.rows), 10) || 5
  return `${rowCount * 24}px`
})

// Extensions CodeMirror (Langage JSON + Thème optionnel)
const codemirrorExtensions = computed(() => {
  const exts = [json()]
  if (props.darkModeJson) {
    exts.push(oneDark)
  }
  return exts
})

// Conversion de la valeur reçue (objet ou chaîne) vers une chaîne affichable
const jsonStringValue = computed(() => {
  if (typeof props.modelValue === 'object' && props.modelValue !== null) {
    return JSON.stringify(props.modelValue, null, 2)
  }
  return typeof props.modelValue === 'string' ? props.modelValue : ''
})

// Saisie dans CodeMirror
const onJsonChange = (newVal: string) => {
  try {
    if (newVal.trim() === '') {
      jsonError.value = null
      emitValue('')
      return
    }

    // Validation de la syntaxe JSON en direct
    JSON.parse(newVal)
    jsonError.value = null
    emitValue(newVal)
  } catch (err: any) {
    jsonError.value = err.message
    emitValue(newVal)
  }
}

// Bouton de formatage automatique (Indent avec 2 espaces)
const formatJson = () => {
  try {
    const raw = jsonStringValue.value
    if (!raw.trim()) return

    const parsed = JSON.parse(raw)
    const formatted = JSON.stringify(parsed, null, 2)
    jsonError.value = null
    emitValue(formatted)
  } catch (err: any) {
    jsonError.value = 'Impossible de formater : ' + err.message
  }
}

/* ----------------------------------------------------
 * FONCTIONS COMMUNES
 * ---------------------------------------------------- */
const inputFormId = computed(() => {
  if (props.inputId) return props.inputId
  if (props.label) {
    return 'txta-' + props.label.replace(/\s+/g, '-').toLowerCase()
  }
  return 'txta-' + Math.random().toString(36).substring(2, 8)
})

const textareaClasses = computed(() => {
  const classes = ['textarea', 'w-full', 'bg-transparent']
  if (props.violation) {
    classes.push('border-error')
  }
  return classes
})

const emitValue = (value: string) => {
  emit('update:modelValue', value)
  emit('update:model-value', value)
}

const onInput = (event: Event) => {
  const target = event.target as HTMLTextAreaElement
  emitValue(target.value)
}

const onCkeditorInput = (value: string) => {
  emitValue(value)
}
</script>

<style scoped>
:deep(.ck-editor) {
  width: 100%;
}

:deep(.ck-editor__editable_inline) {
  min-height: 150px;
}

:deep(.cm-editor) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 0.8125rem;
}

:deep(.cm-scroller) {
  overflow: auto;
}
</style>
