<script setup lang="ts">
import ToggleSwitch from '@/components/button/ToggleSwitch.vue'
import type { EntitySearchField } from '@/models/EntityModel'

const props = defineProps<{
  fields: EntitySearchField[]
  visibleFields: string[]
}>()

const emit = defineEmits<{
  (e: 'update:visibleFields', value: string[]): void
}>()

const handleToggle = (key: string, isChecked: boolean) => {
  const newFields = [...props.visibleFields]
  if (isChecked) {
    if (!newFields.includes(key)) {
      newFields.push(key)
      newFields.sort((a, b) => {
        const indexA = props.fields.findIndex((f) => f.key === a)
        const indexB = props.fields.findIndex((f) => f.key === b)
        return indexA - indexB
      })
    }
  } else {
    const index = newFields.indexOf(key)
    if (index > -1) {
      newFields.splice(index, 1)
    }
  }
  emit('update:visibleFields', newFields)
}
</script>

<template>
  <div class="p-4 space-y-3 max-h-[60vh] overflow-y-auto">
    <p class="text-sm text-base-content/70 mb-2">
      Activez ou désactivez l'affichage des champs ci-dessous.
    </p>

    <template v-for="field in fields" :key="field.key">
      <div
        v-if="field.showInTable"
        class="flex items-center justify-between py-2 border-b border-base-200 last:border-0"
      >
        <span class="text-sm font-medium text-base-content">
          {{ field.label }} <span class="text-xs text-base-content/50">({{ field.type }})</span>
        </span>

        <ToggleSwitch
          :modelValue="visibleFields.includes(field.key)"
          @update:modelValue="(val) => handleToggle(field.key, val)"
          size="sm"
          color="primary"
        />
      </div>
    </template>
  </div>
</template>
