<template>
    <StepperPopup
        title="Créer un nouveau projet"
        :currentStep="props.currentStep"
        :totalSteps="props.totalSteps"
        size="full"
        :content-class="stepContentClass"
        @close="handleClose"
        @previous="emit('previous')"
        @next="emit('next')"
    >
        <FrameworksView
            v-if="props.currentStep === 1"
            :showBackButton="false"
            @select="handleFrameworkSelect"
        />
        <ProjectConfigView
            v-else-if="props.currentStep === 2"
            @request-folder-path="handleRequestFolderPath"
        />
        <DatabaseConfigView v-else-if="props.currentStep === 3" />
        <ScriptConfigView
            v-else-if="props.currentStep === 4"
            @request-file-path="handleRequestFilePath"
        />
        <TableSelectionView v-else-if="props.currentStep === 5" />
        <RelationConfigView v-else-if="props.currentStep === 6" />
        <FrontEndSelectionView
            v-else-if="props.currentStep === 7"
            @select="handleFrontendSelect"
            :showBackButton="false"
        />
        <FrontendLayoutConfigView
            v-else-if="props.currentStep === 8"
            @request-file-path="handleRequestFilePath"
        />
        <GitConfigView v-else-if="props.currentStep === 9" />
    </StepperPopup>
</template>

<script setup lang="ts">
import StepperPopup from '@/core/components/layouts/Popup/StepperPopup.vue';
import FrameworksView from '@/features/frameworks/views/FrameworksView.vue';
import FrontEndSelectionView from '@/features/frontend/views/FrontEndSelectionView.vue';
import ProjectConfigView from './steps/ProjectConfigView.vue';
import DatabaseConfigView from './steps/DatabaseConfigView.vue';
import ScriptConfigView from './steps/ScriptConfigView.vue';
import TableSelectionView from './steps/TableSelectionView.vue';
import RelationConfigView from './steps/RelationConfigView.vue';
import FrontendLayoutConfigView from './steps/FrontendLayoutConfigView.vue';
import GitConfigView from './steps/GitConfigView.vue';

import type { FileRequestPayload } from '../types/generator.types';
import type { Framework } from '@/features/frameworks/types/framework.types';
import type { FrontendFramework } from '@/features/frontend/types/frontend.types.ts';
import { computed } from 'vue';

// L'étape 4 (ScriptConfigView) gère son propre scroll interne
const stepContentClass = computed(() =>
    props.currentStep === 4 ? 'overflow-hidden flex flex-col' : 'overflow-y-auto'
);

const props = defineProps<{
    currentStep: number;
    totalSteps: number;
}>();

const emit = defineEmits<{
    close: [];
    next: [];
    previous: [];
    'select-framework': [framework: Framework];
    'select-frontend': [framework: FrontendFramework];
    'request-folder-path': [];
    'request-file-path': [payload: FileRequestPayload];
}>();

// ← handleClose manquait
function handleClose() {
    emit('close');
}

function handleFrameworkSelect(result: { action: string; framework: Framework; event?: MouseEvent }) {
    emit('select-framework', result.framework);
}

function handleFrontendSelect(framework: FrontendFramework) {
    emit('select-frontend', framework);
}

function handleRequestFolderPath() {
    emit('request-folder-path');
}

function handleRequestFilePath(payload: FileRequestPayload) {
    emit('request-file-path', payload);
}
</script>