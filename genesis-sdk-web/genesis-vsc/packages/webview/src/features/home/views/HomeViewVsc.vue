<!-- genesis-vsc/packages/webview/src/features/home/views/HomeViewVsc.vue -->
<template>
    <HomeView
        @open-generator="isOpen = true"
        @navigate="(name) => router.push({ name })"
    />

    <GeneratorViewVsc
        v-if="isOpen"
        @close="isOpen = false"
    />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { HomeView } from '@genesis-labs/core/features/home/manifest';
import GeneratorViewVsc from '../../generator/views/GeneratorViewVsc.vue';

const router = useRouter();
const isOpen = ref(false);
</script>