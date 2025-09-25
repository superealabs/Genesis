// src/components/Backdrop/BackdropBlocker.tsx
import { Backdrop, CircularProgress } from '@mui/material';
import { useTranslation } from 'react-i18next';

interface Props {
    open: boolean;
}

export default function BackdropBlocker({ open }: Props) {
    const { t } = useTranslation();
    return (
        <Backdrop
            open={open}
            sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}
        >
            <CircularProgress color="inherit" title={t('state.loading')} />
        </Backdrop>
    );
}