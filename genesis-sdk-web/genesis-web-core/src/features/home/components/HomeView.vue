<template>
    <div class="p-4 flex flex-col gap-3">
        <GenesisButton @click="emit('open-generator')">
            <template #leftIcon><IconFolderPlus /></template>
            Créer un nouveau projet
        </GenesisButton>

        <GenesisButton variant="secondary" @click="emit('navigate', 'design-system')">
            Design System
        </GenesisButton>

        <GenesisButton variant="secondary" @click="emit('navigate', 'frameworks')">
            Frameworks
        </GenesisButton>

        <GenesisButton variant="secondary" @click="emit('navigate', 'frontend')">
            Frontend
        </GenesisButton>
    </div>
</template>

<script setup lang="ts">
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import IconFolderPlus from '@/core/components/ui/icons/IconFolderPlus.vue';

const emit = defineEmits<{
    'open-generator': [];
    'navigate': [routeName: string];
}>();
</script>