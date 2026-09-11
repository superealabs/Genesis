<template>
    <div class="p-4 space-y-4">
        <GenesisBackButton />

        <h2 class="text-sm font-semibold text-text">Design System</h2>

        <div class="flex flex-col gap-2">
            <GenesisButton
                v-for="item in showcases"
                :key="item.name"
                variant="secondary"
                @click="router.push({ name: item.name })"
            >
                {{ item.label }} →
            </GenesisButton>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import GenesisBackButton from '@/core/components/ui/actions/GenesisBackButton.vue';

const router = useRouter();

const showcases = [
    { name: 'design-system-buttons', label: 'Buttons' },
    { name: 'design-system-popups',  label: 'Popups'  },
    { name: 'design-system-loaders', label: 'Loaders' },
    { name: 'design-system-colors',  label: 'Colors'  },
    { name: 'design-system-errors', label: 'Errors' },
    { name: 'design-system-progressBar', label: 'progressBar' }
];
</script>