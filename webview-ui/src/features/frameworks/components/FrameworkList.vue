<template>
    <GenesisList>
        <FrameworkRow
            v-for="framework in frameworks"
            :key="framework.id"
            :framework="framework"
            :selected="selectedId === framework.id"
            @select="$emit('select', $event)"
        />
    </GenesisList>
</template>

<script setup lang="ts">
import type { Framework } from '../types/framework.types';
import FrameworkRow from '@/features/frameworks/components/FrameworkRow.vue';
import GenesisList from '@/core/components/layouts/display/GenesisList.vue';

defineProps<{
    frameworks: Framework[];
    selectedId?: number;
}>();

defineEmits<{
    select: [framework: Framework];
}>();
</script>