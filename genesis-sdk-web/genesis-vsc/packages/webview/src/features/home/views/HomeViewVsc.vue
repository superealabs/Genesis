<template>
    <HomeView
        :color-mode="colorMode"
        @open-generator="handleOpenGenerator"
        @navigate="(name) => router.push({ name })"
        @theme="handleThemeChange"
    />

    <GeneratorViewVsc v-if="isOpen" @close="handleCloseGenerator" />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { HomeView } from '@genesis-labs/core/features/home/manifest';
import GeneratorViewVsc from '@/features/generator/views/GeneratorViewVsc.vue';
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
import { useGenerator } from '@genesis-labs/web-core/index';
import { useApp } from '@/core/composables/useApp';   // 🔧 ajuste l'alias

const router = useRouter();
const isOpen = ref(false);
const store = useGeneratorStore();
const { reset } = useGenerator();

// 🔧 NOUVEAU
const { colorMode, setThemeMode } = useApp();

function handleOpenGenerator() {
    isOpen.value = true;
}

function handleCloseGenerator() {
    isOpen.value = false;
    reset();
    store.setIsInitializing(false);
}

// 🔧 NOUVEAU
function handleThemeChange(mode: 'light' | 'auto' | 'dark') {
    setThemeMode(mode);
}
</script>