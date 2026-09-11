<template>
    <div class="space-y-6 p-4">
        <div class="flex items-center gap-3">
            <GenesisBackButton />
        </div>
        <div>
            <h1 class="font-semibold text-text font-heading text-6xl">{{ title }}</h1>
        </div>
        <slot />
    </div>
</template>

<script setup lang="ts">
import GenesisBackButton from '@/core/components/ui/actions/GenesisBackButton.vue';

defineProps<{
    title: string;
}>();
</script>