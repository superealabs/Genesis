<template>
    <svg 
        xmlns="http://www.w3.org/2000/svg" 
        :style="{ width: `${size}px`, height: `${size}px` }"
        viewBox="0 0 24 24" 
        :fill="color || 'currentColor'"
        aria-hidden="true"
    >
        <path d="M4 6h2v2H4zm0 5h2v2H4zm0 5h2v2H4zm16-8V6H8.023v2H18.8zM8 11h12v2H8zm0 5h12v2H8z"></path>
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
