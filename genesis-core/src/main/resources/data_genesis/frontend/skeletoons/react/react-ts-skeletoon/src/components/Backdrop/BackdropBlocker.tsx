// src/components/Backdrop/BackdropBlocker.tsx
import { Backdrop, CircularProgress } from '@mui/material';

interface Props {
    open: boolean;
}

export default function BackdropBlocker({ open }: Props) {
    return (
        <Backdrop
            open={open}
            sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}
        >
            <CircularProgress color="inherit" />
        </Backdrop>
    );
}