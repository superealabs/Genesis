<template>
    <ShowcaseLayout title="Popup">
        <h3 class="text-text-muted text-sm">Popups</h3>

        <div class="flex flex-wrap gap-3">
            <GenesisButton
                v-for="size in sizes"
                :key="size"
                variant="secondary"
                @click="openPopup(size)"
            >
                {{ size }}
            </GenesisButton>
        </div>

        <BaseFormPopup
            v-if="activeSize"
            :size="activeSize"
            :title="`Popup — ${activeSize}`"
            @close="closePopup"
        >
            <div class="flex flex-col gap-2" ref="contentRef">
                <p class="text-sm text-text">This is a popup with size {{ activeSize }}</p>
                <p class="text-xs text-text-muted">
                    Dimension : {{ dimension.width }} x {{ dimension.height }}
                </p>
            </div>
        </BaseFormPopup>
    </ShowcaseLayout>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import BaseFormPopup from '@/core/components/layouts/Popup/BaseFormPopup.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import ShowcaseLayout from '@/features/designSystem/components/layouts/ShowcaseLayout.vue';

const sizes = ['sm', 'md', 'lg', 'xl', '2xl', '3xl', 'full'] as const;
type Size = typeof sizes[number];

const activeSize = ref<Size | null>(null);
const contentRef = ref<HTMLElement | null>(null);
const dimension = ref({ width: 0, height: 0 });

async function openPopup(size: Size) {
    activeSize.value = size;
    await nextTick();

    const container = contentRef.value?.closest('.bg-bg') as HTMLElement | null;
    if (container) {
        dimension.value = {
            width: container.offsetWidth,
            height: container.offsetHeight
        };
    }
}

function closePopup() {
    activeSize.value = null;
    dimension.value = { width: 0, height: 0 };
}
</script>