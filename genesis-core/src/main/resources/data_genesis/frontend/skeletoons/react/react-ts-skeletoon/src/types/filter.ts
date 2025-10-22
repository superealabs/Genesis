// src/types/filter.ts
export type FilterValue = string | number | boolean | null;
export type FilterState = Record<string, FilterValue>;
export type FilterType = 'text' | 'number' | 'date' | 'checkbox';