// src/components/GenericListPage/GenericListPage.tsx
import { ListConfig } from "@/types/generic";
import { usePaginatedResource } from "@/hooks/usePaginatedResource";
import { useState } from "react";
import { Box, Breadcrumbs as MuiBreadcrumbs, Link, Typography } from '@mui/material';
import PaginationSize from "../PaginationSize/PaginationSize";
import PageNavigator from "../PageNavigator/PageNavigator";
import PageSelector from "../PageSelector/PageSelector";
import ListFilterBuilder from "../GenericListPage/ListFilterBuilder";
import ListDataTable from "../GenericListPage/ListDataTable";
import { FilterState } from "@/types/filter";
import BackdropBlocker from "@/components/Backdrop/BackdropBlocker";
import {pageContainerSx, breadcrumbSx} from "@/styles/mui-patterns";

export default function GenericListPage<T extends Record<string, unknown>>(
    config: ListConfig<T>
) {
    return function ListPage() {
        const {
            data,
            page,
            setPage,
            size,
            setSize,
            totalPages,
            sort,
            setSort,
            setFilter,
            loading
        } = usePaginatedResource<T>(config.searchFn, config.defaultSort);

        const [pendingFilters, setPendingFilters] = useState<FilterState>({});

        const handleSearch = () => {
            const payload: Record<string, any> = {};

            for (const [key, value] of Object.entries(pendingFilters)) {
                const meta = config.availableFilters[key];
                if (!meta) continue;

                if ('nestedKey' in meta) {
                    const cast = meta.type === 'number' ? Number(value) : value;
                    const nestedKey = meta.nestedKey as string;
                    payload[key] = { [nestedKey]: cast };
                } else {
                    payload[key] = value;
                }
            }

            setPage(0);
            setFilter(payload);
        };

        return (
            <>
                {loading && <BackdropBlocker open={loading} />}

                <Box sx={pageContainerSx}>
                    <MuiBreadcrumbs sx={breadcrumbSx} separator="/">
                        <Link underline="hover" color="inherit" href="/" sx={{ fontWeight: 'normal' }}>
                            Home
                        </Link>
                        <Link
                            underline="hover"
                            color="inherit"
                            href={`/${config.entityName.toLowerCase()}s`}
                            sx={{ fontWeight: 'normal' }}
                        >
                            {config.entityName}s
                        </Link>
                        <Typography color="text.primary" fontWeight="bold">
                            List
                        </Typography>
                    </MuiBreadcrumbs>

                    <Typography
                        variant="h4"
                        component="h1"
                        sx={{
                            mb: 3,
                            fontWeight: 'bold',
                            color: 'text.primary',
                            textAlign: 'center',
                        }}
                    >
                        {config.pageTitle ?? `${config.entityName} List`}
                    </Typography>

                    {/* Top bar */}
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                        <Box sx={{ flex: '1 1 0', minWidth: 0 }}>
                            <ListFilterBuilder
                                availableFilters={config.availableFilters}
                                filters={pendingFilters}
                                onChange={setPendingFilters}
                                onSearch={handleSearch}
                                setPage={setPage}
                            />
                        </Box>
                        <Box sx={{ flex: '0 0 auto' }}>
                            <PaginationSize
                                size={size}
                                onChange={setSize}
                                onResetPage={() => setPage(0)}
                            />
                        </Box>
                    </Box>

                    <ListDataTable
                        columns={config.columns}
                        data={data}
                        sort={sort}
                        onSort={setSort}
                    />

                    {/* Bottom pagination */}
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, mt: 2 }}>
                        <PageNavigator current={page} totalPages={totalPages} onChange={setPage} />
                        <PageSelector totalPages={totalPages} currentPage={page} onChangePage={setPage} />
                    </Box>
                </Box>
            </>
        );
    };
}