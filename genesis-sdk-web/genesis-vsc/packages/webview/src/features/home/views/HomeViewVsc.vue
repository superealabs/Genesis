<template>
    <HomeView
        @open-generator="handleOpenGenerator"
        @navigate="(name) => router.push({ name })"
    />

    <GeneratorViewVsc
        v-if="isOpen"
        @close="handleCloseGenerator"
    />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { HomeView } from '@genesis-labs/core/features/home/manifest';
import GeneratorViewVsc from '@/features/generator/views/GeneratorViewVsc.vue';
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';

const router = useRouter();
const isOpen = ref(false);
const store = useGeneratorStore();

function handleOpenGenerator() {
    isOpen.value = true;
}

function handleCloseGenerator() {
    isOpen.value = false;

    // au cas où s'il est démonté sans close dans generatorView
    store.setIsInitializing(false);
}
</script>