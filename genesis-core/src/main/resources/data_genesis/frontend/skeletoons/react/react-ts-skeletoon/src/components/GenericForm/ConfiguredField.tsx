import { useEffect, useRef, useState } from 'react';
import {
    Box,
    Button,
    FormControl,
    FormControlLabel,
    FormHelperText,
    InputAdornment,
    MenuItem,
    Select,
    Stack,
    Switch,
    TextField,
    Typography,
    IconButton,
    Slider,
} from '@mui/material';
import { Add, Remove } from '@mui/icons-material';
import { CKEditor } from '@ckeditor/ckeditor5-react';
import {
    Bold,
    ClassicEditor,
    Essentials,
    Heading,
    Italic,
    Link,
    List,
    Paragraph,
} from 'ckeditor5';
import * as L from 'leaflet';
import 'ckeditor5/ckeditor5.css';
import 'leaflet/dist/leaflet.css';

export interface ConfiguredFieldConfig {
    label: string;
    required?: boolean;
    readonly?: boolean;
    component?: string;
    min?: number | string;
    max?: number | string;
    step?: number | string;
    currency?: string;
    accept?: string;
    geographic?: boolean;
    latitude?: string;
    longitude?: string;
    options?: readonly { readonly value: string | number; readonly label: string }[];
}

interface Props {
    name: string;
    config: ConfiguredFieldConfig;
    value: any;
    values: Record<string, any>;
    onChange: (name: string, value: any) => void;
    onChangeMany?: (values: Record<string, any>) => void;
    error?: string;
}

const phoneCountries = [
    { name: 'Madagascar', code: '+261' }, { name: 'France', code: '+33' },
    { name: 'Reunion', code: '+262' }, { name: 'United States', code: '+1' },
    { name: 'Canada', code: '+1' }, { name: 'United Kingdom', code: '+44' },
    { name: 'Germany', code: '+49' }, { name: 'Italy', code: '+39' },
    { name: 'Spain', code: '+34' }, { name: 'Belgium', code: '+32' },
    { name: 'Switzerland', code: '+41' }, { name: 'India', code: '+91' },
    { name: 'China', code: '+86' }, { name: 'Japan', code: '+81' },
    { name: 'Australia', code: '+61' }, { name: 'South Africa', code: '+27' },
    { name: 'Mauritius', code: '+230' }, { name: 'Comoros', code: '+269' },
    { name: 'Seychelles', code: '+248' }, { name: 'Kenya', code: '+254' },
    { name: 'Tanzania', code: '+255' }, { name: 'Uganda', code: '+256' },
    { name: 'Rwanda', code: '+250' }, { name: 'Senegal', code: '+221' },
    { name: 'Ivory Coast', code: '+225' }, { name: 'Cameroon', code: '+237' },
    { name: 'Nigeria', code: '+234' }, { name: 'Brazil', code: '+55' },
    { name: 'Mexico', code: '+52' }, { name: 'United Arab Emirates', code: '+971' },
];

const editorConfig = {
    licenseKey: 'GPL',
    plugins: [Essentials, Paragraph, Bold, Italic, Link, List, Heading],
    toolbar: ['undo', 'redo', '|', 'heading', '|', 'bold', 'italic', 'link', '|', 'bulletedList', 'numberedList'],
};

function toCoordinate(value: unknown): number | null {
    if (value === null || value === undefined || value === '') return null;
    const coordinate = Number(value);
    return Number.isFinite(coordinate) ? coordinate : null;
}

function getPhoneParts(value: unknown) {
    const phone = String(value ?? '').trim();
    const country = [...phoneCountries]
        .sort((first, second) => second.code.length - first.code.length)
        .find((item) => phone.startsWith(item.code));
    const digits = phone.replace(/\D/g, '');
    return {
        countryCode: country?.code ?? '+261',
        number: country
            ? digits.slice(country.code.length - 1)
            : (phone.startsWith('+') ? digits.replace(/^\d{1,3}/, '') : digits),
    };
}

export default function ConfiguredField({ name, config, value, values, onChange, onChangeMany, error }: Props) {
    const component = (config.component ?? 'text').toLowerCase();
    const mapElement = useRef<HTMLDivElement>(null);
    const mapRef = useRef<L.Map | null>(null);
    const markerRef = useRef<L.CircleMarker | null>(null);
    const latestProps = useRef({ config, values, onChange, onChangeMany, name });
    latestProps.current = { config, values, onChange, onChangeMany, name };

    const initialPhone = getPhoneParts(value);
    const [phoneCountryCode, setPhoneCountryCode] = useState(initialPhone.countryCode);
    const [phoneNumber, setPhoneNumber] = useState(initialPhone.number);

    useEffect(() => {
        const parts = getPhoneParts(value);
        setPhoneCountryCode(parts.countryCode);
        setPhoneNumber(parts.number);
    }, [value]);
    const usesCoordinateFields = !config.geographic && Boolean(config.latitude && config.longitude);
    const mapLatitude = usesCoordinateFields ? values[config.latitude!] : value?.latitude ?? value?.lat;
    const mapLongitude = usesCoordinateFields ? values[config.longitude!] : value?.longitude ?? value?.lng;
    const latitude = toCoordinate(mapLatitude);
    const longitude = toCoordinate(mapLongitude);

    useEffect(() => {
        if (component !== 'map' || !mapElement.current) return;

        const initialPosition: L.LatLngExpression = latitude !== null && longitude !== null
            ? [latitude, longitude]
            : [-18.8792, 47.5079];
        const map = L.map(mapElement.current).setView(initialPosition, 5);
        mapRef.current = map;
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors',
            maxZoom: 19,
        }).addTo(map);
        markerRef.current = L.circleMarker(initialPosition, { radius: 7 }).addTo(map);
        map.on('click', (event) => {
            const current = latestProps.current;
            if (!current.config.geographic && current.config.latitude && current.config.longitude) {
                current.onChangeMany?.({
                    [current.config.latitude]: event.latlng.lat,
                    [current.config.longitude]: event.latlng.lng,
                });
                if (!current.onChangeMany) {
                    current.onChange(current.config.latitude, event.latlng.lat);
                    current.onChange(current.config.longitude, event.latlng.lng);
                }
            } else {
                current.onChange(current.name, { longitude: event.latlng.lng, latitude: event.latlng.lat });
            }
        });

        return () => {
            map.remove();
            mapRef.current = null;
            markerRef.current = null;
        };
    }, [component, name, config.latitude, config.longitude]);

    useEffect(() => {
        if (component !== 'map' || latitude === null || longitude === null) return;
        const position: L.LatLngExpression = [latitude, longitude];
        markerRef.current?.setLatLng(position);
        mapRef.current?.setView(position);
    }, [component, latitude, longitude]);

    const sharedTextFieldProps = {
        fullWidth: true,
        margin: 'normal' as const,
        label: config.label,
        required: config.required,
        disabled: config.readonly,
        error: Boolean(error),
        helperText: error,
    };

    if (component === 'phone') {
        return (
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ mt: 2 }}>
                <FormControl sx={{ minWidth: 150 }}>
                    <Select
                        value={phoneCountryCode}
                        aria-label={`${config.label} country code`}
                        disabled={config.readonly}
                        onChange={(event) => {
                            const code = event.target.value;
                            setPhoneCountryCode(code);
                            onChange(name, phoneNumber ? `${code}${phoneNumber}` : '');
                        }}
                    >
                        {phoneCountries.map((country, index) => (
                            <MenuItem key={`${country.name}-${index}`} value={country.code}>
                                {country.name} ({country.code})
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
                <TextField
                    {...sharedTextFieldProps}
                    type="tel"
                    value={phoneNumber}
                    onChange={(event) => {
                        const number = event.target.value.replace(/\D/g, '');
                        setPhoneNumber(number);
                        onChange(name, number ? `${phoneCountryCode}${number}` : '');
                    }}
                />
            </Stack>
        );
    }

    if (component === 'quantity') {
        const numericValue = Number(value) || 0;
        const step = Number(config.step) || 1;
        const min = config.min === undefined ? undefined : Number(config.min);
        const max = config.max === undefined ? undefined : Number(config.max);
        const adjust = (direction: number) => {
            const next = Number((numericValue + direction * step).toFixed(10));
            if ((min === undefined || next >= min) && (max === undefined || next <= max)) {
                onChange(name, next);
            }
        };

        return (
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mt: 2 }}>
                <IconButton aria-label={`Decrease ${config.label}`} disabled={config.readonly} onClick={() => adjust(-1)}>
                    <Remove />
                </IconButton>
                <TextField
                    {...sharedTextFieldProps}
                    type="number"
                    value={value ?? ''}
                    inputProps={{ min: config.min, max: config.max, step: config.step ?? 1 }}
                    onChange={(event) => onChange(name, event.target.value === '' ? '' : Number(event.target.value))}
                />
                <IconButton aria-label={`Increase ${config.label}`} disabled={config.readonly} onClick={() => adjust(1)}>
                    <Add />
                </IconButton>
            </Stack>
        );
    }

    if (component === 'currency') {
        const formatCurrency = (amount: number | string) => {
            if (amount === '' || amount === null || amount === undefined) {
                return '';
            }

            const number = Number(amount);

            if (!Number.isFinite(number)) {
                return '';
            }

            return new Intl.NumberFormat('fr-FR', {
                maximumFractionDigits: 20,
                useGrouping: true,
            }).format(number);
        };

        return (
            <TextField
                {...sharedTextFieldProps}
                type="text"
                value={formatCurrency(value ?? '')}
                inputProps={{
                    inputMode: 'decimal',
                    min: config.min,
                    max: config.max,
                }}
                InputProps={{
                    endAdornment: config.currency ? (
                        <InputAdornment position="end">
                            {config.currency}
                        </InputAdornment>
                    ) : undefined,
                }}
                onChange={(event) => {
                    const input = event.target.value;

                    // Autorise uniquement les chiffres et le séparateur décimal.
                    const cleaned = input
                        .replace(/\s/g, '')
                        .replace(/[^\d.,-]/g, '')
                        .replace(',', '.');

                    if (cleaned === '' || cleaned === '-') {
                        onChange(name, '');
                        return;
                    }

                    const number = Number(cleaned);

                    if (Number.isFinite(number)) {
                        onChange(name, number);
                    }
                }}
            />
        );
    }

    if (component === 'range') {
        const parseNumber = (input: unknown, fallback: number): number => {
            if (input === null || input === undefined || input === '') {
                return fallback;
            }

            const parsed = Number(input);
            return Number.isFinite(parsed) ? parsed : fallback;
        };

        const min = parseNumber(config.min, 0);
        const max = Math.max(min, parseNumber(config.max, 100));
        const step = Math.max(0.000001, parseNumber(config.step, 1));

        const currentValue = parseNumber(value, min);

        const safeValue = Math.min(max, Math.max(min, currentValue));

        return (
            <Stack sx={{ mt: 2 }}>
                <Typography variant="body2">
                    {config.label}
                </Typography>

                <Slider
                    name={name}
                    value={safeValue}
                    min={min}
                    max={max}
                    step={step}
                    onChange={(_, newValue) => {
                        if (typeof newValue === 'number') {
                            onChange(name, newValue);
                        }
                    }}
                    aria-label={config.label || name}
                />

                <Typography variant="caption" align="right">
                    {safeValue}
                </Typography>
            </Stack>
        );
    }

    if (component === 'color') {
        return (
            <TextField
                {...sharedTextFieldProps}
                type="color"
                value={value || '#000000'}
                onChange={(event) => onChange(name, event.target.value)}
                sx={{ '& input': { minHeight: 48, p: 0.5 } }}
            />
        );
    }

    if (component === 'textarea' || component === 'json-editor') {
        const invalidJson = component === 'json-editor' && Boolean(value) && (() => {
            try {
                JSON.parse(value);
                return false;
            } catch {
                return true;
            }
        })();

        return (
            <Box sx={{ mt: 2 }}>
                <TextField
                    {...sharedTextFieldProps}
                    multiline
                    minRows={component === 'json-editor' ? 6 : 3}
                    value={value ?? ''}
                    error={Boolean(error) || invalidJson}
                    helperText={error || (invalidJson ? 'Enter valid JSON' : undefined)}
                    inputProps={component === 'json-editor' ? { spellCheck: false } : undefined}
                    onChange={(event) => onChange(name, event.target.value)}
                />
                {component === 'json-editor' && (
                    <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                        <Button size="small" onClick={() => {
                            try { onChange(name, JSON.stringify(JSON.parse(value || ''), null, 2)); } catch { return; }
                        }}>Format JSON</Button>
                        <Button size="small" onClick={() => {
                            try { onChange(name, JSON.stringify(JSON.parse(value || ''))); } catch { return; }
                        }}>Minify JSON</Button>
                    </Stack>
                )}
            </Box>
        );
    }

    if (component === 'ckeditor') {
        return (
            <Box sx={{ mt: 2 }}>
                <Typography component="label" variant="body2">{config.label}</Typography>
                <CKEditor
                    editor={ClassicEditor}
                    data={value ?? ''}
                    disabled={config.readonly}
                    config={editorConfig}
                    onChange={(_, editor) => onChange(name, editor.getData())}
                />
                {error && <FormHelperText error>{error}</FormHelperText>}
            </Box>
        );
    }

    if (component === 'map') {
        return (
            <Box sx={{ mt: 2 }}>
                <Typography component="label" variant="body2">{config.label}</Typography>
                <Box ref={mapElement} role="application" aria-label={`Choose location for ${config.label}`} sx={{ width: '100%', height: 320, mt: 1, border: '1px solid', borderColor: 'divider', borderRadius: 1, overflow: 'hidden' }} />
                <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                    <TextField label="Latitude" type="number" size="small" value={latitude ?? ''} disabled={config.readonly}
                               inputProps={{ step: 'any' }}
                               onChange={(event) => {
                                   const rawValue = event.target.value;
                                   const nextLatitude = rawValue === '' ? null : Number(rawValue);

                                   if (config.geographic || !config.latitude || !config.longitude) {
                                       onChange(name, {
                                           longitude: longitude ?? null,
                                           latitude: nextLatitude,
                                       });
                                   } else {
                                       onChange(config.latitude, nextLatitude);
                                   }
                               }}
                    />
                    <TextField label="Longitude" type="number" size="small" value={longitude ?? ''} disabled={config.readonly} inputProps={{ step: 'any' }} onChange={(event) => {
                        const rawValue = event.target.value;
                        const nextLongitude = rawValue === '' ? null : Number(rawValue);
                        if (config.geographic || !config.latitude || !config.longitude) {
                            onChange(name, { longitude: nextLongitude, latitude: latitude ?? null });
                        } else {
                            onChange(config.longitude, event.target.value);
                        }
                    }} />
                </Stack>
                {error && <FormHelperText error>{error}</FormHelperText>}
            </Box>
        );
    }

    if (component === 'file') {
        return (
            <TextField
                {...sharedTextFieldProps}
                type="file"
                inputProps={{ accept: config.accept }}
                onChange={(event) => onChange(name, (event.target as HTMLInputElement).files?.[0] ?? null)}
            />
        );
    }

    if (component === 'checkbox') {
        return (
            <FormControlLabel
                control={<Switch checked={Boolean(value)} disabled={config.readonly} onChange={(event) => onChange(name, event.target.checked)} />}
                label={config.label}
            />
        );
    }

    return (
        <TextField
            {...sharedTextFieldProps}
            type={component}
            value={value ?? ''}
            inputProps={{ min: config.min, max: config.max, step: config.step, accept: config.accept }}
            onChange={(event) => onChange(name, event.target.value)}
        />
    );
}