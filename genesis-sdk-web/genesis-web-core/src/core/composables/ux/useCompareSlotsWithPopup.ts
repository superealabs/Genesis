import { useCompareSlots, type CompareSlotsConfig } from './useCompareSlots';
import { useReplacePopup } from './useReplacePopup';

export function useCompareSlotsWithPopup<T>(config: CompareSlotsConfig<T>) {
    const compare = useCompareSlots(config);
    const popup = useReplacePopup<T>();

    // On surcharge handleSelect pour déclencher automatiquement le popup si nécessaire
    function handleSelect(item: T, event?: MouseEvent) {
        const result = compare.handleSelect(item);

        if (result.action === 'replace-needed') {
            popup.triggerReplace(item, event);
            return { action: 'pending-replace' as const, item, event };
        }

        return { ...result, event };
    }

    return {
        ...compare,
        ...popup,
        handleSelect
    };
}