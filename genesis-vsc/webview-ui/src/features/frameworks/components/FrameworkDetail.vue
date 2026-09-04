<template>
    <BottomPanel
        :title="framework.name"
        :initialHeight="300"
        :initialWidth="400"
        @close="$emit('close')"
    >
        <div class="flex flex-col gap-4">

            <!-- Infos communes -->
            <section class="flex flex-col gap-2">
                <DetailRow label="Nom" :value="framework.name" />
                <DetailRow label="Core Framework" :value="framework.coreFramework" />
                <DetailRow label="Type" :value="framework.type" />
                <DetailRow label="Language" :value="String(framework.languageId)" />
                <DetailRow label="Prod Ready" :value="framework.isProd ? 'Oui' : 'Non'" />
            </section>

            <div class="h-px bg-secondary" />

            <!-- Capacités -->
            <section class="flex flex-col gap-2">
                <p class="text-xs font-semibold text-text-muted uppercase tracking-wide">Capacités</p>
                <div class="flex flex-wrap gap-2">
                    <span v-if="framework.useDB"           class="text-xs px-2 py-0.5 rounded bg-secondary text-text-muted">DB</span>
                    <span v-if="framework.useCloud"        class="text-xs px-2 py-0.5 rounded bg-secondary text-text-muted">Cloud</span>
                    <span v-if="framework.useFrontendApp"  class="text-xs px-2 py-0.5 rounded bg-secondary text-text-muted">Frontend</span>
                    <span v-if="framework.isGateway"       class="text-xs px-2 py-0.5 rounded bg-secondary text-text-muted">Gateway</span>
                    <span v-if="framework.useEurekaServer" class="text-xs px-2 py-0.5 rounded bg-secondary text-text-muted">Eureka</span>
                    <span v-if="!framework.useDB && !framework.useCloud && !framework.useFrontendApp && !framework.isGateway"
                        class="text-xs text-text-muted">Aucune capacité spéciale</span>
                </div>
            </section>

            <!-- Version (placeholder) -->
            <div class="h-px bg-secondary" />
            <section class="flex flex-col gap-2">
                <p class="text-xs font-semibold text-text-muted uppercase tracking-wide">Version</p>
                <p class="text-xs text-text-muted italic">— à définir côté Java</p>
            </section>

        </div>
    </BottomPanel>
</template>

<script setup lang="ts">
import BottomPanel from '@/core/components/layouts/Popup/BottomPanel.vue';
import type { Framework } from '../types/framework.types';

defineProps<{
    framework: Framework;
}>();

defineEmits<{
    close: [];
}>();
</script>

<!-- DetailRow inline pour éviter un fichier supplémentaire -->
<script lang="ts">
import { defineComponent, h } from 'vue';

export const DetailRow = defineComponent({
    props: { label: String, value: String },
    setup(props) {
        return () => h('div', { class: 'flex justify-between items-center' }, [
            h('span', { class: 'text-xs text-text-muted' }, props.label),
            h('span', { class: 'text-xs text-text font-medium' }, props.value ?? '—'),
        ]);
    }
});
</script>

