<template>
    <div 
        class="flex items-center justify-between w-full p-3 rounded-lg border transition-all duration-200 group cursor-pointer"
        :class="[
            isSelected 
                ? 'bg-accent/5 border-accent border-l-4 border-l-accent' 
                : (isHidden ? 'bg-bg-dark/50 border-secondary/50 opacity-75' : 'bg-bg-light border-secondary hover:border-accent/50 hover:bg-secondary/20')
        ]"
        @click="$emit('select')"
    >
        <!-- ═══ GAUCHE : Nom de la configuration ═══ -->
        <div class="flex items-center gap-3 min-w-0 flex-1">
            <div v-if="isHidden" class="w-2 h-2 rounded-full bg-text-muted flex-shrink-0" title="Configuration cachée"></div>
            
            <!-- MODE LECTURE -->
            <span 
                v-if="!isEditing"
                class="text-sm font-medium truncate"
                :class="isHidden ? 'text-text-muted line-through' : (isSelected ? 'text-accent font-semibold' : 'text-text')"
                :title="name"
                @dblclick="startEditing"
            >
                {{ name }}
            </span>

            <!-- MODE ÉDITION -->
            <input
                v-else
                ref="nameInput"
                v-model="editName"
                type="text"
                class="w-full bg-bg border border-accent rounded px-2 py-1 text-sm text-text focus:outline-none focus:ring-1 focus:ring-accent"
                @keyup.enter="saveName"
                @blur="saveName"
                @click.stop
            />
        </div>

        <!-- ═══ DROITE : Actions de paramétrage ═══ -->
        <div class="flex items-center gap-1 flex-shrink-0 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-opacity duration-200" @click.stop>
            
            <GenesisButtonIcon v-if="showRename" size="sm" variant="tertiary" title="Renommer" @click="startEditing">
                <IconRename />
            </GenesisButtonIcon>

            <GenesisButtonIcon v-if="showEdit" size="sm" variant="tertiary" title="Modifier les composants" @click="$emit('edit')">
                <IconEdit />
            </GenesisButtonIcon>

            <GenesisButtonIcon 
                v-if="showDelete" 
                size="sm" 
                variant="tertiary" 
                class="text-red-500 hover:text-red-400 hover:bg-red-500/10" 
                title="Supprimer" 
                @click="$emit('delete')"
            >
                <IconTrashAlt />
            </GenesisButtonIcon>

            <div v-if="showToggleVisibility" class="w-px h-4 bg-secondary mx-1"></div>

            <GenesisButtonIcon 
                v-if="showToggleVisibility" 
                size="sm" 
                variant="tertiary" 
                :title="isHidden ? 'Afficher' : 'Cacher'" 
                @click="$emit('toggleVisibility')"
            >
                <IconHide v-if="!isHidden" />
                <IconShow v-else />
            </GenesisButtonIcon>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'; // ✅ AJOUT de ref et nextTick
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconRename from '@genesis-labs/web-core/core/components/ui/icons/IconRename.vue';
import IconEdit from '@genesis-labs/web-core/core/components/ui/icons/IconEdit.vue';
import IconShow from '@genesis-labs/web-core/core/components/ui/icons/IconShow.vue';
import IconHide from '@genesis-labs/web-core/core/components/ui/icons/IconHide.vue';
import IconTrashAlt from '@genesis-labs/web-core/core/components/ui/icons/IconTrashAlt.vue';

const props = withDefaults(defineProps<{
    name: string;
    isHidden?: boolean;
    isSelected?: boolean;
    showRename?: boolean;
    showEdit?: boolean;
    showDelete?: boolean;
    showToggleVisibility?: boolean;
}>(), {
    isHidden: false,
    isSelected: false,
    showRename: true,
    showEdit: true,
    showDelete: true,
    showToggleVisibility: true
});

// ✅ SIGNATURE MODIFIÉE : on émet maintenant le nouveau nom
const emit = defineEmits<{
    select: []; 
    rename: [newName: string];
    edit: [];
    delete: [];
    toggleVisibility: [];
}>();

// ═══ LOGIQUE D'ÉDITION INLINE ═══
const isEditing = ref(false);
const editName = ref('');
const nameInput = ref<HTMLInputElement | null>(null);

function startEditing() {
    isEditing.value = true;
    editName.value = props.name;
    // Focus et sélection du texte après le rendu du DOM
    nextTick(() => {
        nameInput.value?.focus();
        nameInput.value?.select();
    });
}

function saveName() {
    const trimmed = editName.value.trim();
    if (trimmed && trimmed !== props.name) {
        emit('rename', trimmed);
    }
    isEditing.value = false; // Quitte le mode édition dans tous les cas (Enter ou Blur)
}
</script>