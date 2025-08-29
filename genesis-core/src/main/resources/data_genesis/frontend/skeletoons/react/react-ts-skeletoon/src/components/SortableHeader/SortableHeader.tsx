// src/components/SortableHeader/SortableHeader.tsx
import { ArrowDownward, ArrowUpward, ImportExport } from "@mui/icons-material";
import {Box, TableCell} from "@mui/material";
import { useTheme } from '@mui/material/styles';

interface Props {
    columnKey: string;
    label: string;
    sort: `${string},${'asc' | 'desc'}` | undefined;
    onSort: (sort: `${string},${'asc' | 'desc'}`) => void;
}

export default function SortableHeader({ columnKey, label, sort, onSort }: Props) {
    const [currentKey, currentDir] = sort?.split(',') ?? [];
    const isActive = currentKey === columnKey;

    const handleClick = () => {
        const dir = !isActive || currentDir === 'desc' ? 'asc' : 'desc';
        onSort(`${columnKey},${dir}`);
    };

    return (
        <TableCell
            onClick={handleClick}
            sx={(theme) => ({
                cursor: 'pointer',
                userSelect: 'none',
                whiteSpace: 'nowrap',
                color: theme.palette.text.primary,
                bgcolor: theme.palette.background.paper,
                transition: 'background-color 0.15s',
                '&:hover': {
                    bgcolor: theme.palette.mode === 'light'
                        ? theme.palette.grey[200]  // gris plus foncé en light
                        : theme.palette.grey[700],  // gris plus clair en dark
                    color: theme.palette.text.primary,
                },
            })}
        >
            <Box display="flex" alignItems="center" gap={1}>
                {label}
                {isActive ? (
                    currentDir === 'asc' ? (
                        <ArrowUpward fontSize="inherit" />
                    ) : (
                        <ArrowDownward fontSize="inherit" />
                    )
                ) : (
                    <ImportExport fontSize="inherit" opacity={0.5} style={{ fontSize: '0.75rem' }} />
                )}
            </Box>
        </TableCell>
    );
}