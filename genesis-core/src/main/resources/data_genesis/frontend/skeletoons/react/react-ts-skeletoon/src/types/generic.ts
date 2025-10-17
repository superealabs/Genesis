// src/types/generic.ts
import {Column} from "@/components/DataTable/DataTable";
import {FilterState} from "@/types/filter";

export interface ListConfig<T> {
    searchFn: (params: {
        page: number;
        size: number;
        sort: `${string},${'asc' | 'desc'}`;
        filter: FilterState;
    }) => Promise<{ content: T[]; totalPages: number }>;
    columns: Column<T>[];
    availableFilters: Record<string, { label: string; type: string }>;
    defaultSort: `${string},${'asc' | 'desc'}`;
    entityName: string;
    pageTitle?: string;
    createRoute?: string;
}