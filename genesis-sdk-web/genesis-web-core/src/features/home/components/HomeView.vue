<template>
    <div class="p-4 flex flex-col gap-3">
        <div class="flex items-center justify-between mb-2">
            <h2 class="text-sm font-semibold uppercase tracking-wider text-muted">Menu Principal</h2>
                <GenesisButtonIcon 
                    variant="secondary" 
                    size="md" 
                    @click="tutorial.restart('main-menu')"
                    title="Lancer le tutoriel"
                >
                <IconHelp /> <!-- Remplace par IconHelpCircle si ton icône s'appelle ainsi -->
            </GenesisButtonIcon>
        </div>

        <GenesisTutorialItem id="create-project">
            <GenesisButton @click="emit('open-generator')" :variant="'accent'">
                <template #leftIcon><IconFolderPlus /></template>
                Créer un nouveau projet
            </GenesisButton>
        </GenesisTutorialItem>

        <GenesisButton variant="secondary" @click="emit('navigate', 'design-system')">
            Design System
        </GenesisButton>

        <GenesisTutorialItem id="frameworks">
            <GenesisButton variant="primary" @click="emit('navigate', 'frameworks')">
                Frameworks
            </GenesisButton>
        </GenesisTutorialItem>

        <GenesisButton variant="primary" @click="emit('navigate', 'frontend')">
            Frontend
        </GenesisButton>

        <GenesisTutorialItem id="database">
            <GenesisButton variant="primary" @click="emit('navigate', 'database')">
                Database
            </GenesisButton>
        </GenesisTutorialItem>

        <GenesisButton variant="primary" @click="emit('navigate', 'repository')">
            Repository
        </GenesisButton>        
    </div>
</template>

<script setup lang="ts">
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import IconFolderPlus from '@genesis-labs/web-core/core/components/ui/icons/IconFolderPlus.vue';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue';

const emit = defineEmits<{
    'open-generator': [];
    'navigate': [routeName: string];
}>();

// L'ordre de cette liste est l'ordre d'affichage des étapes
const tutorial = useGenesisTutorial('home', {
    'main-menu': [
        { id: 'create-project', text: 'Commencez ici : le générateur vous guide pas à pas pour créer un nouveau projet.' },
        { id: 'frameworks',     text: 'Parcourez les frameworks disponibles et comparez-les avant de choisir.' },
        { id: 'database',       text: 'Consultez les moteurs de base de données pris en charge.' }
    ]
});
</script>