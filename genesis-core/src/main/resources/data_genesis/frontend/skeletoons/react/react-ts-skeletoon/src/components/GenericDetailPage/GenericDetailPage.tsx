import {useParams, useNavigate, useLocation} from 'react-router-dom';
import { useEffect, useRef, useState } from 'react';
import { Box, Paper, Typography, Button, Chip, Divider, Grid } from '@mui/material';
import { pageContainerSx } from '@/styles/mui-patterns';
import BackdropBlocker from '@/components/Backdrop/BackdropBlocker';
import type { ApiResponse } from '@/services/api';
import {ArrowBack} from "@mui/icons-material";
import { Tabs, Tab } from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { base64ToUrl } from "@/utils/imageUtil";
import * as L from 'leaflet';
import 'leaflet/dist/leaflet.css';

type AnyRecord = Record<string, any>;

function DetailMap({ latitude, longitude }: { latitude: unknown; longitude: unknown }) {
    const elementRef = useRef<HTMLDivElement>(null);
    const lat = Number(latitude);
    const lng = Number(longitude);
    const hasPosition = latitude !== null && latitude !== undefined && latitude !== ''
        && longitude !== null && longitude !== undefined && longitude !== ''
        && Number.isFinite(lat) && Number.isFinite(lng);

    useEffect(() => {
        if (!elementRef.current) return;
        const center: L.LatLngExpression = hasPosition ? [lat, lng] : [0, 0];
        const map = L.map(elementRef.current, { scrollWheelZoom: false }).setView(center, hasPosition ? 13 : 2);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors',
            maxZoom: 19,
        }).addTo(map);
        if (hasPosition) L.circleMarker(center, { radius: 7 }).addTo(map);
        const resizeTimer = window.setTimeout(() => map.invalidateSize(), 0);
        return () => {
            window.clearTimeout(resizeTimer);
            map.remove();
        };
    }, [lat, lng, hasPosition]);

    return <Box sx={{ width: '100%', height: 280, border: '1px solid', borderColor: 'divider', borderRadius: 1, overflow: 'hidden' }} ref={elementRef} role="img" aria-label="Location map" />;
}

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
    columns: { header: string; accessor: keyof T | ((row: T) => React.ReactNode), type?: string; geographic?: boolean; latitude?: string; longitude?: string }[];
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
                        {config.columns.map(({ header, accessor, type, geographic, latitude, longitude }) => {
                            const value = resolveValue(accessor);
                            const mapValue = value as any;
                            const mapLatitude = geographic ? mapValue?.latitude ?? mapValue?.lat : latitude ? record[latitude] : mapValue?.latitude ?? mapValue?.lat;
                            const mapLongitude = geographic ? mapValue?.longitude ?? mapValue?.lng : longitude ? record[longitude] : mapValue?.longitude ?? mapValue?.lng;
                            return (
                                <Grid item xs={12} sm={6} key={String(header)}>
                                    <Box display="flex" flexDirection={type === 'map' ? 'column' : 'row'} justifyContent="space-between" alignItems={type === 'map' ? 'stretch' : 'center'} gap={type === 'map' ? 1 : 0} px={1}>
                                        <Typography variant="body2" color="text.secondary">
                                            {header}
                                        </Typography>
                                        <Box>
                                            {typeof value === 'boolean' ? (
                                                <Chip label={value ? 'Yes' : 'No'} color={value ? 'success' : 'default'} size="small" />
                                            ) : type === 'ckeditor' ? (
                                                <Box
                                                    sx={{ fontWeight: 500, overflowWrap: 'anywhere', '& h1': { fontSize: '1.5rem', fontWeight: 700 }, '& h2': { fontSize: '1.25rem', fontWeight: 700 }, '& ul': { listStyle: 'disc', pl: 3 }, '& ol': { listStyle: 'decimal', pl: 3 }, '& a': { color: 'primary.main', textDecoration: 'underline' } }}
                                                    dangerouslySetInnerHTML={{ __html: value == null ? '' : String(value) }}
                                                />
                                            ) : type === 'map' ? (
                                                <DetailMap latitude={mapLatitude} longitude={mapLongitude} />
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
