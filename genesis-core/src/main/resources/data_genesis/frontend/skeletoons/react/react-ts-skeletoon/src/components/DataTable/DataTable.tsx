// src/components/DataTable/DataTable.tsx
import { Table, TableHead, TableBody, TableRow, TableCell, Box, Button, Typography } from '@mui/material';
import { Dialog, DialogContent, IconButton } from '@mui/material';
import { useEffect, useRef, useState } from 'react';
import Download from '@mui/icons-material/Download';
import ZoomIn from '@mui/icons-material/ZoomIn';
import Close from '@mui/icons-material/Close';
import { tableWrapperSx, tableHeaderSx, tableCellSx } from '@/styles/mui-patterns';
import SortableHeader from '../SortableHeader/SortableHeader';
import { Link } from '@mui/material';
import { buildFileSource, downloadFile, getFileSize, getGeneratedFileName, isImageContent } from "@/utils/file-utils";

export type Column<T> = {
    header: string;
    accessor: keyof T | ((row: T) => React.ReactNode);
    link?: (row: T) => string;   // ← URL complète ou fonction
    sortKey?: string;
    type?: string;
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

const isFileType = (type?: string) => ['file', 'uint8array', 'bytea', 'blob', 'varbinary', 'byte[]', 'bytearray']
    .includes((type ?? '').replace(/\s/g, '').toLowerCase());

export default function DataTable<T extends Record<string, any>>({
                                                                     columns,
                                                                     data,
                                                                     sort,
                                                                     onSort,
                                                                 }: Props<T>) {
    const [preview, setPreview] = useState<{ source: string; alt: string } | null>(null);
    const [downloadedFile, setDownloadedFile] = useState<string | null>(null);
    const downloadFeedbackTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => () => {
        if (downloadFeedbackTimeout.current) clearTimeout(downloadFeedbackTimeout.current);
    }, []);

    const handleDownload = (content: unknown, fileName: string) => {
        downloadFile(content, fileName);
        setDownloadedFile(fileName);
        if (downloadFeedbackTimeout.current) clearTimeout(downloadFeedbackTimeout.current);
        downloadFeedbackTimeout.current = setTimeout(() => setDownloadedFile(null), 1600);
    };

    return (
        <>
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
                                {isFileType(col.type) ? (() => {
                                    const value = typeof col.accessor === 'function' ? col.accessor(row) : row[col.accessor];
                                    if (value == null || value === '') return '-';
                                    if (!isImageContent(value)) {
                                        const fileName = getGeneratedFileName(value, col.header);
                                        return (
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                                                <Typography variant="body2">{fileName}</Typography>
                                                <Typography variant="caption" color="text.secondary">{getFileSize(value)}</Typography>
                                                <Button size="small" startIcon={<Download />} onClick={() => handleDownload(value, fileName)}>
                                                    {downloadedFile === fileName ? 'Téléchargement…' : 'Télécharger'}
                                                </Button>
                                            </Box>
                                        );
                                    }
                                    const fileName = getGeneratedFileName(value, col.header);
                                    const image = <img src={buildFileSource(value)} alt={col.header} style={{ maxWidth: 100, maxHeight: 100, objectFit: 'contain' }} />;
                                    return (
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                                            {col.link ? <Link href={col.link(row)} color="primary" underline="hover" sx={{ cursor: 'pointer' }}>{image}</Link> : image}
                                            <IconButton size="small" aria-label={`Prévisualiser ${fileName}`} onClick={() => setPreview({ source: buildFileSource(value), alt: fileName })}>
                                                <ZoomIn fontSize="small" />
                                            </IconButton>
                                            <Typography variant="caption" color="text.secondary">{getFileSize(value)}</Typography>
                                            <Button size="small" startIcon={<Download />} onClick={() => handleDownload(value, fileName)}>
                                                {downloadedFile === fileName ? 'Téléchargement…' : 'Télécharger'}
                                            </Button>
                                        </Box>
                                    );
                                })() : col.link ? (
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
        <Dialog open={Boolean(preview)} onClose={() => setPreview(null)} maxWidth={false}>
            <DialogContent sx={{ p: 2, bgcolor: 'black', position: 'relative', display: 'grid', placeItems: 'center' }}>
                <IconButton aria-label="Fermer l'aperçu" onClick={() => setPreview(null)} sx={{ position: 'absolute', top: 8, right: 8, color: 'white', zIndex: 1 }}>
                    <Close />
                </IconButton>
                {preview && <img src={preview.source} alt={preview.alt} style={{ maxWidth: '90vw', maxHeight: '90vh', objectFit: 'contain' }} />}
            </DialogContent>
        </Dialog>
        </>
    );
}
