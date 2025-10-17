// src/components/PageNavigator/PageNavigator.tsx
import { Pagination } from '@mui/material';

interface Props {
    current: number;
    totalPages: number;
    onChange: (page: number) => void;
}

export default function PageNavigator({ current, totalPages, onChange }: Props) {
    if (totalPages <= 1) return null;

    return (
        <Pagination
            count={totalPages}
            page={current + 1}          // MUI is 1-based, our state is 0-based
            onChange={(_, p) => onChange(p - 1)}
            color="primary"
            size="small"
            showFirstButton
            showLastButton
            siblingCount={1}            // how many pages around the current one
            boundaryCount={1}           // how many pages at start / end
        />
    );
}