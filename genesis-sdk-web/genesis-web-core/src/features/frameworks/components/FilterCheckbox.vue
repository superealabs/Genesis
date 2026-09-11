<!-- webview-ui/src/features/frameworks/components/FilterCheckbox.vue -->
<template>
    <label class="flex items-center gap-2 cursor-pointer group">
        <input
            type="checkbox"
            :checked="modelValue"
            @change="$emit('update:modelValue', ($event.target as HTMLInputElement).checked)"
            class="w-3.5 h-3.5 rounded border-secondary text-accent focus:ring-accent focus:ring-offset-0 cursor-pointer"
        />
        <span class="text-xs text-text group-hover:text-accent transition-colors">
            {{ label }}
        </span>
    </label>
</template>

<script setup lang="ts">
defineProps<{
    label: string;
    modelValue?: boolean;
}>();

defineEmits<{
    'update:modelValue': [value: boolean];
}>();
</script>