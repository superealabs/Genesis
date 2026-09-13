<template>
    <svg 
        xmlns="http://www.w3.org/2000/svg" 
        :style="{ width: `${size}px`, height: `${size}px` }"
        viewBox="0 0 24 24" 
        :fill="color || 'currentColor'"
        aria-hidden="true"
    >
        <path d="M7 5c-1.103 0-2 .897-2 2v10c0 1.103.897 2 2 2h10c1.103 0 2-.897 2-2V7c0-1.103-.897-2-2-2H7zm0 12V7h10l.002 10H7z"></path>
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
