<template>
    <div class="p-4 flex flex-col gap-3">
        <GenesisButton @click="isOpen = true">
            <template #leftIcon><IconFolderPlus /></template>
            Créer un nouveau projet
        </GenesisButton>

        <GenesisButton variant="secondary" @click="router.push({ name: 'design-system' })">
            Design System
        </GenesisButton>

        <GeneratorStepper v-if="isOpen" @close="isOpen = false" />

        <GenesisButton variant="secondary" @click="router.push({ name: 'frameworks' })">
            Frameworks
        </GenesisButton>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import IconFolderPlus from '@/core/components/ui/icons/IconFolderPlus.vue';
import GeneratorStepper from '@/features/generator/components/GeneratorStepper.vue';

const router = useRouter();
const isOpen = ref(false);
</script>