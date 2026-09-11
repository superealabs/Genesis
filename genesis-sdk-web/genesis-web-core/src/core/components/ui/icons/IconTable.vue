<template>
    <svg 
        xmlns="http://www.w3.org/2000/svg" 
        :style="{ width: `${size}px`, height: `${size}px` }"
        viewBox="0 0 24 24" 
        :fill="color || 'currentColor'"
        aria-hidden="true"
    >
        <path d="M4 21h15.893c1.103 0 2-.897 2-2V5c0-1.103-.897-2-2-2H4c-1.103 0-2 .897-2 2v14c0 1.103.897 2 2 2zm0-2v-5h4v5H4zM14 7v5h-4V7h4zM8 7v5H4V7h4zm2 12v-5h4v5h-4zm6 0v-5h3.894v5H16zm3.893-7H16V7h3.893v5z"></path>
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
