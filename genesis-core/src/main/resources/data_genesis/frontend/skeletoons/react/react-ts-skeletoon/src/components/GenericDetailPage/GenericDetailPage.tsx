import {useParams, useNavigate, useLocation} from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Box, Paper, Typography, Button, Chip, Divider, Grid } from '@mui/material';
import { pageContainerSx } from '@/styles/mui-patterns';
import BackdropBlocker from '@/components/Backdrop/BackdropBlocker';
import type { ApiResponse } from '@/services/api';
import {ArrowBack} from "@mui/icons-material";
import { Tabs, Tab } from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { base64ToUrl } from "@/utils/imageUtil";

type AnyRecord = Record<string, any>;

export type DetailAction<T> = {
    label: string;
    icon?: React.ReactNode;
    onClick: (row: T, navigate: ReturnType<typeof useNavigate>) => void;
    color?: 'primary' | 'secondary' | 'error' | 'success';
    variant?: 'text' | 'outlined' | 'contained';
    slot?: 'top' | 'bottom'; // où placer le bouton
};

export type DetailTab<T> = {
    label: string;
    render: (row: T) => React.ReactNode;
};

interface DetailConfig<T extends AnyRecord> {
    entityName: string;
    service: {
        getById: (id: number) => Promise<T>;
        delete: (id: string | number) => Promise<ApiResponse<void>>;
    };
    columns: { header: string; accessor: keyof T | ((row: T) => React.ReactNode), type?: string }[];
    backRoute: string; // fallback si pas de state
    actions?: DetailAction<T>[];
    tabs?: DetailTab<T>[];           // <-- ajout
}

export default function GenericDetailPage<T extends AnyRecord>(config: DetailConfig<T>) {
    const { t } = useTranslation();
    return function DetailPage() {
        const { id } = useParams<{ id: string }>();
        const navigate = useNavigate();
        const location = useLocation();           // 👈
        const [record, setRecord] = useState<T | null>(null);
        const [loading, setLoading] = useState(true);
        const [tabIndex, setTabIndex] = useState(0);
        const [searchParams] = useSearchParams();

        // priorité : paramètre URL, puis state, puis backRoute
        const backTo =
            searchParams.get('from') || location.state?.from || config.backRoute;

        useEffect(() => {
            if (!id) return;
            config.service
                .getById(Number(id))
                .then(setRecord)
                .finally(() => setLoading(false));
        }, [id]);

        if (loading) return <BackdropBlocker open />;
        if (!record) return <Typography color="error">{t('messages.state.notFound')}</Typography>;

        const resolveValue = (accessor: keyof T | ((row: T) => React.ReactNode)) =>
            typeof accessor === 'function' ? accessor(record) : record[accessor];

        const bottomActions = config.actions ?? [];

        return (
            <Box sx={pageContainerSx}>
                <Paper
                    elevation={2}
                    sx={{
                        maxWidth: { xs: 960, md: 1200 }, // +240 px de largeur
                        mx: 'auto',
                        p: { xs: 3, md: 6 },             // encore plus d’espace intérieur
                        borderRadius: 3,
                    }}
                >
                    {/* Bouton retour automatique */}
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                        <Button
                            variant="text"
                            startIcon={<ArrowBack />}
                            onClick={() => navigate(backTo)}
                        >
                            {t('messages.button.backToList')}
                        </Button>
                    </Box>

                    <Typography variant="h4" fontWeight={700} mb={2}>
                        {config.entityName} #{id}
                    </Typography>

                    <Divider sx={{ mb: 2 }} />

                    {/* Grille 2 colonnes */}
                    <Grid container spacing={3}>
                        {config.columns.map(({ header, accessor }) => {
                            const value = resolveValue(accessor);
                            return (
                                <Grid item xs={12} sm={6} key={String(header)}>
                                    <Box display="flex" justifyContent="space-between" alignItems="center" px={1}>
                                        <Typography variant="body2" color="text.secondary">
                                            {header}
                                        </Typography>
                                        <Box>
                                            {typeof value === 'boolean' ? (
                                                <Chip label={value ? 'Yes' : 'No'} color={value ? 'success' : 'default'} size="small" />
                                            ) : (
                                                <Typography fontWeight={500}>
                                                    { type == 'file' ? (
                                                        <img
                                                            src={base64ToUrl(value as string)}
                                                            alt={header}
                                                            style={{
                                                                maxWidth: "200px",
                                                                maxHeight: "200px",
                                                                objectFit: "contain"
                                                            }}
                                                        />) :
                                                    (value === null || value === undefined ? '-' : String(value))}
                                                </Typography>
                                            )}
                                        </Box>
                                    </Box>
                                </Grid>
                            );
                        })}
                    </Grid>

                    {/* Actions optionnelles */}
                    {bottomActions.length > 0 && (
                        <>
                            <Divider sx={{ my: 3 }} />
                            <Box display="flex" gap={1.5} justifyContent="flex-end">
                                {bottomActions.map((a, idx) => (
                                    <Button
                                        key={idx}
                                        variant={a.variant ?? 'outlined'}
                                        color={a.color}
                                        startIcon={a.icon}
                                        onClick={() => a.onClick(record, navigate)}
                                    >
                                        {a.label}
                                    </Button>
                                ))}
                            </Box>
                        </>
                    )}
                </Paper>

                {/* Onglets optionnels */}
                {config.tabs && config.tabs.length > 0 && (
                    <>
                        <Divider sx={{ my: 3 }} />
                        <Tabs value={tabIndex} onChange={(_, v) => setTabIndex(v)}>
                            {config.tabs.map((t) => (
                                <Tab key={t.label} label={t.label} />
                            ))}
                        </Tabs>
                        <Box mt={2}>{config.tabs[tabIndex].render(record)}</Box>
                    </>
                )}
            </Box>
        );
    };
}