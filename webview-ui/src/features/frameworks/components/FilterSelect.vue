<!-- webview-ui/src/features/frameworks/components/FilterSelect.vue -->
<template>
    <div class="space-y-1">
        <label class="text-xs text-text-muted block">{{ label }}</label>
        <select
            :value="modelValue"
            @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
            class="w-full bg-bg-light text-text text-xs border border-secondary rounded px-2 py-1.5
                   focus:outline-none focus:border-accent cursor-pointer"
        >
            <option
                v-for="option in options"
                :key="option.value"
                :value="option.value"
            >
                {{ option.label }}
            </option>
        </select>
    </div>
</template>

<script setup lang="ts">
interface Option {
    label: string;
    value: string;
}

defineProps<{
    label: string;
    options: Option[];
    modelValue?: string;
}>();

defineEmits<{
    'update:modelValue': [value: string];
}>();
</script>