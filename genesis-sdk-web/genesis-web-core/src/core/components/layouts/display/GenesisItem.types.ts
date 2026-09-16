import type { ComputedRef } from 'vue';

export type DisplayMode = 'grid' | 'table' | 'list';

export const GENESIS_LIST_CONTEXT = Symbol('GenesisListContext');

export const DISPLAY_MODES_CYCLE: DisplayMode[] = ['grid', 'list', 'table'];

export interface GenesisListContext {
    display: ComputedRef<DisplayMode>;
    deletable: ComputedRef<boolean>;
}