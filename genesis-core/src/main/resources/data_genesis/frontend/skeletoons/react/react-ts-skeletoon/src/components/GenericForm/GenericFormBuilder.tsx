// src/components/GenericForm/GenericFormBuilder.tsx
import { useState, useEffect } from 'react';
import {
    TextField,
    MenuItem,
    FormControl,
    InputLabel,
    Select,
    Checkbox,
    FormControlLabel,
    Box,
    Paper,
    Typography,
    Button,
    FormHelperText
} from '@mui/material';
import { Add, Save, ArrowBack } from '@mui/icons-material'; // 👈 NOUVEAU
import Breadcrumbs from '@mui/material/Breadcrumbs';
import Link from '@mui/material/Link';
import { Link as RouterLink, useNavigate } from 'react-router-dom'; // 👈 AJOUT useNavigate
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs, { Dayjs } from 'dayjs';
import { useSnackbar } from 'notistack';
import { ApiResponse } from "@/services/api";
import BackdropBlocker from "@/components/Backdrop/BackdropBlocker";
import { pageContainerSx, breadcrumbSx } from "@/styles/mui-patterns";
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import { TimePicker } from "@mui/x-date-pickers";
import { parseTimeString } from '@/utils/timeParser';
import { formatTimeTz, parseTimeTz } from "@/utils/timeTzParser";
import { fileToArray, bytesToUrl } from "@/utils/imageUtil";
import { DurationInput } from "@/components/Input/DurationInput";
import { useTranslation } from 'react-i18next';
import axios from 'axios';

interface FormFieldConfig {
    label: string;
    type: 'text' | 'number' | 'Date' | 'datetime' | 'time' | 'timeTz' | 'checkbox' | 'select' | 'interval'  | 'Uint8Array';
    required?: boolean;
    readonly?: boolean;
    options?: readonly { readonly value: string | number; readonly label: string }[];
    foreignKey?: {
        endpoint: string;
        labelKey: string;
        valueKey: string;
    };
    transform?: (value: any) => any;
}

interface Props<T> {
    entityName: string;
    fields: Record<string, FormFieldConfig>;
    onSubmit: (data: Partial<T>) => Promise<ApiResponse<T>>;
    redirectTo?: string;
    title?: string;
    cardSx?: React.ComponentProps<typeof Paper>['sx'];
    className?: string;
    initialData?: Partial<T>;
    mode?: 'create' | 'update';
    detailRedirect?: string | ((id: string | number) => string);
    idKey?: string;
    listRoute?: string;
}

export default function GenericFormBuilder<T extends Record<string, any>>({
                                                                              entityName,
                                                                              fields,
                                                                              onSubmit,
                                                                              redirectTo,
                                                                              title,
                                                                              initialData,
                                                                              mode,
                                                                              detailRedirect,
                                                                              idKey = 'id',
                                                                              listRoute,
                                                                          }: Props<T>) {
    const { enqueueSnackbar } = useSnackbar();
    const navigate = useNavigate();
    const [formData, setFormData] = useState<Partial<T>>(initialData || {});
    const [foreignOptions, setForeignOptions] = useState<
        Record<string, { value: string | number; label: string }[]>
    >({});
    const [loading, setLoading] = useState(false);
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
    const { t } = useTranslation();

    useEffect(() => {
        const loadForeignKeys = async () => {
            for (const [key, config] of Object.entries(fields)) {
                if (config.foreignKey) {
                    try {
                        // ✅ Utiliser axios au lieu de fetch
                        const response = await axios.get(
                            `${import.meta.env.VITE_API_BASE}${config.foreignKey.endpoint}`
                        )
                        const data = response.data
                        const content = data.content || data.data?.content || data

                        const options = Array.isArray(content)
                            ? content.map((item: any) => ({
                                value: item[config.foreignKey!.valueKey],
                                label: item[config.foreignKey!.labelKey],
                            }))
                            : []

                        setForeignOptions((prev) => ({ ...prev, [key]: options }))
                    } catch (error) {
                        console.error('Erreur chargement ' + key, error)
                    }
                }
            }
        }
        loadForeignKeys()
    }, [fields])

    useEffect(() => {
        if (initialData) {
            const preloaded: Partial<T> = {};
            Object.entries(fields).forEach(([key, config]) => {
                const value = initialData[key as keyof T];
                if (config.foreignKey && value && typeof value === 'object') {
                    preloaded[key as keyof T] = (value as any)[config.foreignKey.valueKey];
                } else {
                    preloaded[key as keyof T] = value;
                }
            });
            setFormData(preloaded);
        }
    }, [initialData, fields]);

    const handleChange = async (key: string, value: any) => {
        if (value instanceof File) {
            value = await fileToArray(value)
        }
        setFormData((prev) => ({ ...prev, [key]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setFieldErrors({});

        let payload: any = { ...formData };
        Object.entries(fields).forEach(([key, config]) => {
            if (config.transform) {
                payload[key] = config.transform(payload[key]);
            }
        });
        Object.keys(payload).forEach(key => {
            if (payload[key] === ''
                    || payload[key] === null
                    || payload[key] === undefined) {
                delete payload[key];
            }
        });

        console.log('Payload:', payload);

        try {
            const res = await onSubmit(payload);
            enqueueSnackbar(res.message, { variant: 'success' });

            if (mode === 'create') {
                const recordId = res.data?.[idKey];
                if (recordId != null) {
                    const target =
                        typeof detailRedirect === 'function'
                            ? detailRedirect(recordId)
                            : (detailRedirect || `/${entityName.toLowerCase()}s/:id`).replace(
                                ':id',
                                String(recordId)
                            );
                    setTimeout(() => navigate(target), 1500);
                } else if (redirectTo) {
                    setTimeout(() => navigate(redirectTo), 1500);
                }
            } else if (redirectTo) {
                setTimeout(() => navigate(redirectTo), 1500);
            }
        } catch (err: any) {
            const body = err?.response?.data;
            if (body?.errors && typeof body.errors === 'object') {
                setFieldErrors(body.errors);
            }
            enqueueSnackbar(body?.message || 'Erreur', { variant: 'error' });
        } finally {
            setLoading(false);
        }
    };

    const fieldCount = Object.keys(fields).length;

    return (
        <>
            <BackdropBlocker open={loading} />
            <Box sx={pageContainerSx}>
                {/* 👇 NOUVEAU : Conteneur flex pour Breadcrumbs + Bouton */}
                <Box
                    sx={{
                        width: 1,
                        py: 3,
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                    }}
                >
                    <Breadcrumbs sx={breadcrumbSx}>
                        <Link underline="hover" color="inherit" component={RouterLink} to="/">
                            {t('messages.projectName')}
                        </Link>
                        <Link
                            underline="hover"
                            color="inherit"
                            component={RouterLink}
                            to={`/${entityName.toLowerCase()}s`}
                        >
                            {entityName}s
                        </Link>
                        <Typography color="text.primary">{title}</Typography>
                    </Breadcrumbs>

                    {/* 👇 NOUVEAU : Bouton conditionnel retour à la liste */}
                    <Button
                        variant="text"
                        color="primary"
                        startIcon={<ArrowBack />}
                        onClick={() => (window.location.href = redirectTo || '/')}
                        sx={{ flexShrink: 0 }}
                    >
                        {t('messages.button.backToList')}
                    </Button>
                </Box>

                <Paper
                    elevation={3}
                    sx={{
                        maxWidth: 720,
                        mx: 'auto',
                        p: { xs: 3, md: 5 },
                        borderRadius: 4,
                        bgcolor: 'background.paper',
                    }}
                >
                    <Typography
                        variant="h5"
                        component="h1"
                        sx={{ mb: 4, textAlign: 'center', fontWeight: 'bold', color: 'text' }}
                    >
                        {title}
                    </Typography>

                    <form onSubmit={handleSubmit}>
                        <Box
                            sx={{
                                display: 'grid',
                                gridTemplateColumns: {
                                    base: '1fr',
                                    md: fieldCount > 10 ? 'repeat(2, 1fr)' : 'minmax(480px, 1fr)',
                                },
                                gap: 4,
                                mb: 4,
                            }}
                        >
                            {Object.entries(fields).map(([key, config]) => {
                                const value = formData[key as keyof T] ?? '';
                                const options = foreignOptions[key] || [...(config.options || [])];

                                const isLong = ['description', 'notes', 'adresse'].includes(key);

                                return (
                                    <Box key={key} sx={{ gridColumn: isLong ? '1 / -1' : undefined }}>
                                        {config.type === 'select' || config.foreignKey ? (
                                            <FormControl fullWidth margin="normal">
                                                <InputLabel>{config.label}</InputLabel>
                                                <Select
                                                    value={value}
                                                    label={config.label}
                                                    onChange={(e) => handleChange(key, e.target.value)}
                                                    disabled={config.readonly}
                                                >
                                                    {options.map((opt) => (
                                                        <MenuItem key={opt.value} value={opt.value}>
                                                            {opt.label}
                                                        </MenuItem>
                                                    ))}
                                                </Select>
                                                {fieldErrors[key] && (
                                                    <FormHelperText>{fieldErrors[key]}</FormHelperText>
                                                )}
                                            </FormControl>
                                        ) : config.type === 'checkbox' ? (
                                            <FormControlLabel
                                                control={
                                                    <Checkbox
                                                        checked={Boolean(value)}
                                                        onChange={(e) => handleChange(key, e.target.checked)}
                                                    />
                                                }
                                                label={config.label}
                                            />
                                        ) : config.type === 'datetime' ? (
                                            <DateTimePicker
                                                label={config.label}
                                                value={value ? dayjs(value) : null}
                                                onChange={(val) =>
                                                    handleChange(key, val ? val.utc().toISOString() : '')
                                                }
                                                slotProps={{
                                                    textField: {
                                                        fullWidth: true,
                                                        margin: 'normal',
                                                        error: Boolean(fieldErrors[key]),
                                                        helperText: fieldErrors[key] ?? ' ',
                                                    },
                                                }}
                                            />
                                        ) : config.type === 'time' ? (
                                            <TimePicker
                                                label={config.label}
                                                value={value ? parseTimeString(String(value)) : null}
                                                onChange={(val) => {
                                                    const timeString = val ? val.format('HH:mm:ss') : null;
                                                    handleChange(key, timeString);
                                                }}
                                                slotProps={{
                                                    textField: {
                                                        fullWidth: true,
                                                        margin: 'normal',
                                                        required: config.required,
                                                        error: Boolean(fieldErrors[key]),
                                                        helperText: fieldErrors[key] ?? ' ',
                                                    },
                                                }}
                                            />
                                        ) : config.type === 'timeTz' ? (
                                            <TimePicker
                                                label={config.label}
                                                value={formData[key] ? parseTimeTz(String(formData[key])) : null}
                                                onChange={(val) =>
                                                    handleChange(
                                                        key,
                                                        val ? formatTimeTz(val) : null
                                                    )
                                                }
                                                timezone="UTC"
                                                ampm={false}
                                                slotProps={{
                                                    textField: {
                                                        fullWidth: true,
                                                        margin: 'normal',
                                                        required: config.required,
                                                        error: Boolean(fieldErrors[key]),
                                                        helperText: fieldErrors[key] ?? ' ',
                                                    },
                                                }}
                                            />
                                        ) : config.type === 'Date' ? (
                                            <DatePicker
                                                label={config.label}
                                                value={value ? dayjs(value) : null}
                                                onChange={(val: Dayjs | null) =>
                                                    handleChange(key, val ? val.format('YYYY-MM-DD') : null)
                                                }
                                                slotProps={{
                                                    textField: {
                                                        fullWidth: true,
                                                        margin: 'normal',
                                                        error: Boolean(fieldErrors[key]),
                                                        helperText: fieldErrors[key] ?? ' ',
                                                    },
                                                }}
                                            />
                                        ) : config.type === 'interval' ? (
                                            <DurationInput
                                                value={String(value)}
                                                onChange={(iso) => handleChange(key, iso)}
                                                label={config.label}
                                                error={Boolean(fieldErrors[key])}
                                                helperText={fieldErrors[key] ?? ' '}
                                            />
                                        ) : config.type === 'Uint8Array' ? (
                                            <>
                                                <input
                                                    type="file"
                                                    onChange={(e) => handleChange(key, e.target.files?.[0] ?? null)}
                                                />
                                                <img
                                                    src={bytesToUrl(formData[key] as number[])}
                                                    alt={key}
                                                    style={{
                                                        maxWidth: "200px",
                                                        maxHeight: "200px",
                                                        objectFit: "contain"
                                                    }}
                                                />
                                            </>
                                        )  : (
                                            <TextField
                                                fullWidth
                                                margin="normal"
                                                label={config.label}
                                                type={config.type}
                                                value={value}
                                                onChange={(e) => handleChange(key, e.target.value)}
                                                required={config.required}
                                                variant="outlined"
                                                InputProps={{ readOnly: config.readonly }}
                                                disabled={config.readonly}
                                                error={Boolean(fieldErrors[key])}
                                                helperText={fieldErrors[key]}
                                            />
                                        )}
                                    </Box>
                                );
                            })}
                        </Box>

                        <Box
                            sx={{
                                display: 'flex',
                                gap: 3,
                                justifyContent: 'center',
                                mt: 4,
                            }}
                        >
                            <Button
                                variant="outlined"
                                onClick={() => (window.location.href = redirectTo || '/')}
                            >
                                {t('messages.common.cancel')}
                            </Button>
                            <Button
                                type="submit"
                                variant="contained"
                                color="primary"
                                disabled={loading}
                                startIcon={mode === 'create' ? <Add /> : <Save />}
                            >
                                {loading
                                    ? t(mode === 'create' ? 'messages.state.creating' : 'messages.state.updating')
                                    : t(mode === 'create' ? 'messages.common.create' : 'messages.common.edit')}
                            </Button>
                        </Box>
                    </form>
                </Paper>
            </Box>
        </>
    );
}
