// src/components/DataTable/DataTable.tsx
import { Table, TableHead, TableBody, TableRow, TableCell } from '@mui/material';
import { tableWrapperSx, tableHeaderSx, tableCellSx } from '@/styles/mui-patterns';
import SortableHeader from '../SortableHeader/SortableHeader';
import { Link } from '@mui/material';

export type Column<T> = {
    header: string;
    accessor: keyof T | ((row: T) => React.ReactNode);
    link?: (row: T) => string;   // ← URL complète ou fonction
    sortKey?: string;
};

interface Props<T> {
    columns: Column<T>[];
    data: T[];
    sort?: `${string},${'asc' | 'desc'}`;
    onSort?: (sort: `${string},${'asc' | 'desc'}`) => void;
}

// Fonction utilitaire pour formater les valeurs null en '-'
const formatNull = (value: any) => {
    if (typeof value === 'boolean') return value.toString();
    return value ?? '-';
};

export default function DataTable<T extends Record<string, any>>({
                                                                     columns,
                                                                     data,
                                                                     sort,
                                                                     onSort,
                                                                 }: Props<T>) {
    return (
        <Table sx={tableWrapperSx}>
            <TableHead sx={tableHeaderSx}>
                <TableRow>
                    {columns.map((c) =>
                        onSort && c.sortKey ? (
                            <SortableHeader
                                key={c.sortKey}
                                columnKey={c.sortKey}
                                label={c.header}
                                sort={sort}
                                onSort={onSort}
                            />
                        ) : (
                            <TableCell
                                    key={c.header}
                                sx={{
                                    ...tableCellSx,
                                    color: (theme) => theme.palette.text.primary, // 👈 dynamique
                                    bgcolor: (theme) => theme.palette.background.paper,
                                    '&:hover': (theme) => theme.palette.action.hover,
                                }}
                            >
                                {c.header}
                            </TableCell>
                        ))}
                </TableRow>
            </TableHead>

            <TableBody>
                {data.map((row, idx) => (
                    <TableRow key={idx}>
                        {columns.map((col, j) => (
                            <TableCell key={j} sx={tableCellSx}>
                                {col.link ? (
                                    <Link
                                        href={col.link(row)}
                                        color="primary"
                                        underline="hover"
                                        sx={{ cursor: 'pointer' }}
                                    >
                                        {formatNull(
                                            typeof col.accessor === 'function'
                                                ? col.accessor(row)
                                                : row[col.accessor]
                                        )}
                                    </Link>
                                ) : (
                                    formatNull(
                                        typeof col.accessor === 'function'
                                            ? col.accessor(row)
                                            : row[col.accessor]
                                    )
                                )}
                            </TableCell>
                        ))}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}