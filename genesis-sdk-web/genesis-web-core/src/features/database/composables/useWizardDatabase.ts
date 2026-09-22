import { computed } from 'vue';
import { useDatabase } from './useDatabase';
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

export function useWizardDatabase(onSelect: (engine: DatabaseEngineDto, event?: MouseEvent) => void) {
    const db = useDatabase();

    const replaceOptions = computed<SelectionOption[]>(() => {
        if (!db.compare?.slots?.value) return [];
        return Object.entries(db.compare.slots.value)
            .filter(([, engine]) => engine !== null)
            .map(([slot, engine]) => ({
                id: slot,
                label: `Slot ${slot}`,
                description: (engine as DatabaseEngineDto).name
            }));
    });

    async function handleSelectWrapper(engine: DatabaseEngineDto, event?: MouseEvent) {
        try {
            await db.handleSelect(engine, event);
            onSelect(engine, event);
        } catch (error) {
            console.error("❌ [WizardDatabase] Erreur lors de la sélection :", error);
        }
    }

    function handleReplaceSelection(slotId: string | number) {
        if (db.pendingEngine.value) {
            db.handleReplace(slotId, db.pendingEngine.value);
        }
        db.cancelReplace();
    }

    return {
        // Données réactives (Vue les déballera automatiquement)
        engines: db.engines,
        selectedId: db.selectedId,
        databaseSlots: db.databaseSlots,
        displayMode: db.displayMode,
        compareMode: db.compareMode,
        showReplacePopup: db.showReplacePopup,
        pendingEngine: db.pendingEngine,
        mouseX: db.mouseX,
        mouseY: db.mouseY,
        isLoading: db.isLoading,
        replaceOptions,
        
        // Actions
        handleSelectWrapper,
        handleReplaceSelection,
        cancelReplace: db.cancelReplace,
        handleModeChange: db.handleModeChange,
        // Note: Si setDisplayMode n'existe pas dans useDatabase, on peut l'ajouter ou gérer le v-model autrement
        setDisplayMode: (mode: DisplayMode) => { if(db.setDisplayMode) db.setDisplayMode(mode); }
    };
}