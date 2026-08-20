<template>
    <svg 
        xmlns="http://www.w3.org/2000/svg" 
        :style="{ width: `${size}px`, height: `${size}px` }"
        viewBox="0 0 24 24" 
        :fill="color || 'currentColor'"
        aria-hidden="true"
    >
        <rect id="Artboard6" x="-0" y="-0" width="24" height="24" style="fill:none;"/><clipPath id="_clip1"><rect x="-0" y="-0" width="24" height="24"/></clipPath><g clip-path="url(#_clip1)"><g transform="matrix(32.258065,0,0,32.258065,20.274194,23.677419)"></g><text x="4.048px" y="23.677px" style="font-family:'Outfit-SemiBold', 'Outfit';font-weight:600;font-size:32.258px;fill:#001414;">?</text></g>
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
