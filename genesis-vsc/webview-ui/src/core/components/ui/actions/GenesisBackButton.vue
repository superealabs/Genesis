<template>
    <GenesisButtonIcon v-if="canGoBack" variant="secondary" :visible-background="false" size="lg" @click="router.back()">
        <IconChevronLeft/>
    </GenesisButtonIcon>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
// import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import IconChevronLeft from '../icons/IconChevronLeft.vue';
import GenesisButtonIcon from './GenesisButtonIcon.vue';

const router = useRouter();
const canGoBack = computed(() => window.history.length > 1);
</script>