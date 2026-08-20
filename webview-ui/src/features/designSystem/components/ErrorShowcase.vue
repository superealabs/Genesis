<template>
    <ShowcaseLayout title="Errors">

        <div class="space-y-4">

            <!-- Erreur simple -->
            <div class="space-y-2">
                <h3 class="text-text-muted text-sm">Erreur simple</h3>
                <GenesisButton variant="secondary" @click="openError('simple')">
                    Ouvrir
                </GenesisButton>
            </div>

            <!-- Erreur avec stacktrace -->
            <div class="space-y-2">
                <h3 class="text-text-muted text-sm">Erreur avec stacktrace</h3>
                <GenesisButton variant="secondary" @click="openError('stacktrace')">
                    Ouvrir
                </GenesisButton>
            </div>

            <!-- Erreur par taille -->
            <div class="space-y-2">
                <h3 class="text-text-muted text-sm">Tailles</h3>
                <div class="flex flex-wrap gap-3">
                    <GenesisButton
                        v-for="size in sizes"
                        :key="size"
                        variant="secondary"
                        @click="openErrorWithSize(size)"
                    >
                        {{ size }}
                    </GenesisButton>
                </div>
            </div>
        </div>

        <!-- Popups -->
        <ErrorPopup
            v-if="activeError === 'simple'"
            title="Erreur simple"
            message="Une erreur est survenue lors de l'opération."
            :size="activeSize"
            @close="closeError"
        />

        <ErrorPopup
            v-if="activeError === 'stacktrace'"
            title="Erreur avec stacktrace"
            message="NullPointerException : impossible d'accéder à la propriété 'name'."
            :stackTrace="fakeStackTrace"
            :showStackTrace="true"
            :size="activeSize"
            @close="closeError"
        />

    </ShowcaseLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import ShowcaseLayout from '@/features/designSystem/components/layouts/ShowcaseLayout.vue';
import ErrorPopup from '@/core/components/layouts/Popup/ErrorPopup.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';

const sizes = ['sm', 'md', 'lg', 'xl', '2xl', '3xl', 'full'] as const;
type Size = typeof sizes[number];

const activeError = ref<'simple' | 'stacktrace' | null>(null);
const activeSize = ref<Size>('md');

const fakeStackTrace = `java.lang.NullPointerException: Cannot read field "name"
    at mg.genesis.core.JavaFileGenerator.generate(JavaFileGenerator.java:24)
    at mg.genesis.api.controller.GeneratorController.generateJavaFile(GeneratorController.java:18)
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)`;

function openError(type: 'simple' | 'stacktrace') {
    activeSize.value = 'md';
    activeError.value = type;
}

function openErrorWithSize(size: Size) {
    activeSize.value = size;
    activeError.value = 'simple';
}

function closeError() {
    activeError.value = null;
}
</script>