<template>
    <div
        class="grid gap-3"
        :style="`grid-template-columns: repeat(auto-fill, minmax(${minColWidth}, 1fr))`"
    >
        <slot />
    </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
    minColWidth?: string;
}>(), {
    minColWidth: '120px'
});
</script>