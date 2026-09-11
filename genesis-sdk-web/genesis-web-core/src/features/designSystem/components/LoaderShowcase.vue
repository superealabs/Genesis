<template>
    <ShowcaseLayout title="Loader">
        <div class="space-y-2">
            <h3 class="text-text-muted text-sm">Loading Popup</h3>
            <div class="flex flex-wrap gap-3">
                <GenesisButton
                    v-for="size in sizes"
                    :key="size"
                    variant="secondary"
                    @click="openLoader(size)"
                >
                    {{ size }}
                </GenesisButton>
            </div>
        </div>

        <LoadingPopup
            v-if="activeSize"
            :size="activeSize"
            title="Chargement..."
            message="Veuillez patienter"
            :isClosable="true"
            @close="activeSize = null"
        />
    </ShowcaseLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import LoadingPopup from '@/core/components/layouts/Popup/LoadingPopup.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import ShowcaseLayout from '@/features/designSystem/components/layouts/ShowcaseLayout.vue';

const sizes = ['sm', 'md', 'lg', 'xl', '2xl', '3xl', 'full'] as const;
type Size = typeof sizes[number];

const activeSize = ref<Size | null>(null);

function openLoader(size: Size) {
    activeSize.value = size;
}
</script>