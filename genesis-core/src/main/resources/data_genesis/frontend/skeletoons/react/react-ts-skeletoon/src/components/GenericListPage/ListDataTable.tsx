// src/components/GenericListPage/ListDataTable.tsx
import DataTable, { Column } from '../DataTable/DataTable';

interface Props<T extends Record<string, any>> {
    columns: Column<T>[];
    data: T[];
    sort?: `${string},${'asc' | 'desc'}`;
    onSort?: (sort: `${string},${'asc' | 'desc'}`) => void;
}

export default function ListDataTable<T extends Record<string, any>>({
                                                                         columns,
                                                                         data,
                                                                         sort,
                                                                         onSort,
                                                                     }: Props<T>) {
    return <DataTable columns={columns} data={data} sort={sort} onSort={onSort} />;
}