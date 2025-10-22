// src/hooks/usePaginatedResource.ts
import { useEffect, useState } from 'react';
import type { FilterState } from '@/types/filter';

export function usePaginatedResource<T>(
    searchFn: (params: {
        page: number;
        size: number;
        sort: `${string},${'asc' | 'desc'}`;
        filter: FilterState;
    }) => Promise<{ content: T[]; totalPages: number }>,
    defaultSort: `${string},${'asc' | 'desc'}`
) {
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);
    const [sort, setSort] = useState(defaultSort);
    const [filter, setFilter] = useState<FilterState>({});
    const [data, setData] = useState<T[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setLoading(true);
        searchFn({ page, size, sort, filter })
            .then(res => {
                setData(res.content);
                setTotalPages(res.totalPages);
            })
            .finally(() => setLoading(false));
    }, [page, size, sort, filter, searchFn]);

    return {
        data,
        page,
        setPage,
        size,
        setSize,
        totalPages,
        sort,
        setSort,
        filter,
        setFilter,
        loading
    };
}