<template>
    <GenesisGrid min-col-width="200px">
        <FrameworkCard
            v-for="framework in frameworks"
            :key="framework.id"
            :framework="framework"
            :selected="selectedId === framework.id"
            @select="$emit('select', $event)"
            @info="$emit('info', $event)"
        />
    </GenesisGrid>
</template>

<script setup lang="ts">
import type { Framework } from '../types/framework.types';
import FrameworkCard from '@/features/frameworks/components/FrameworkCard.vue';
import GenesisGrid from '@/core/components/layouts/display/GenesisGrid.vue';

defineProps<{
    frameworks: Framework[];
    selectedId?: number;
}>();

defineEmits<{
    select: [framework: Framework];
    info: [framework: Framework];
}>();
</script>