// src/styles/mui-patterns.ts
import { SxProps, Theme } from '@mui/material';

export const pageContainerSx: SxProps<Theme> = ({
    p: 6,
    display: 'flex',
    flexDirection: 'column',
    minHeight: 'calc(100vh - 60px)',
});

export const tableWrapperSx: SxProps<Theme> = {
    width: 1,
    borderCollapse: 'collapse',
    border: 1,
    borderColor: 'divider',
};

export const tableHeaderSx: SxProps<Theme> = {
    bgcolor: 'grey.100',
};

export const tableCellSx: SxProps<Theme> = {
    p: 3,
    borderBottom: 1,
    borderColor: 'divider',
};

export const smallSelectSx: SxProps<Theme> = (theme) => ({
    minWidth: 60,
    maxWidth: 100,
    height: '2rem',
    px: 0.5,
    py: 0.25,
    fontSize: '0.75rem',
    border: 1,
    borderColor: 'divider',
    borderRadius: 1,
    bgcolor: theme.palette.background.paper,
    color: theme.palette.text.primary,
    cursor: 'pointer',
    outline: 'none',
});

export const smallFormControlSx: SxProps<Theme> = {
    m: 0,
    minWidth: 60,
    maxWidth: 100,
};

export const smallMenuItemSx: SxProps<Theme> = {
    fontSize: '0.75rem',
};

export const inlineLabelSx: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    fontSize: '0.75rem',
};

export const breadcrumbSx: SxProps<Theme> = {
    fontSize: '0.875rem',
    color: 'text.primary',
    pb: 2,
};