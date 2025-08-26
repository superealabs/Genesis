// src/components/Input/DurationInput.tsx
import React from 'react';
import { TextField, Stack, Box, Typography } from '@mui/material';

interface Props {
    value: string;            // ISO-8601
    onChange: (iso: string) => void;
    label?: string;
}

const units = [
    { key: 'years',   label: 'y', max: 99, step: 1 },
    { key: 'months',  label: 'm', max: 11, step: 1 },
    { key: 'days',    label: 'd', max: 30, step: 1 },
    { key: 'hours',   label: 'h', max: 23, step: 1 },
    { key: 'minutes', label: 'min', max: 59, step: 1 },
    { key: 'seconds', label: 's', max: 59, step: 0.01 },
];

export const DurationInput: React.FC<Props> = ({ value, onChange, label = 'Durée' }) => {
    const obj = React.useMemo(() => parseISO(value), [value]);

    const handle = (k: string, v: string) => {
        const num = parseFloat(v) || 0;
        onChange(toISO({ ...obj, [k]: num }));
    };

    return (
        <Box>
            <Typography
                variant="caption"
                display="block"
                sx={{ mb: 0.5, ml: 1, color: 'text.secondary' }}
            >
                {label}
            </Typography>
            <Stack
                direction="row"
                spacing={1}
                flexWrap="wrap"
                sx={{
                    p: 1,
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1,
                    bgcolor: 'background.paper',
                }}
            >
                {units.map(({ key, label: lbl, max, step }) => (
                    <TextField
                        key={key}
                        type="number"
                        size="small"
                        label={lbl}
                        sx={{ width: 65 }}
                        value={obj[key as keyof typeof obj] || ''}
                        inputProps={{ min: 0, max, step }}
                        onChange={(e) => handle(key, e.target.value)}
                    />
                ))}
            </Stack>
        </Box>
    );
};

/* ---------- helpers inchangés ---------- */
function parseISO(iso: string) {
    const r = /P(?:(\d+(?:\.\d+)?)Y)?(?:(\d+(?:\.\d+)?)M)?(?:(\d+(?:\.\d+)?)D)?(?:T(?:(\d+(?:\.\d+)?)H)?(?:(\d+(?:\.\d+)?)M)?(?:(\d+(?:\.\d+)?)S)?)?/;
    const m = iso.match(r) || [];
    return {
        years:   Number(m[1] || 0),
        months:  Number(m[2] || 0),
        days:    Number(m[3] || 0),
        hours:   Number(m[4] || 0),
        minutes: Number(m[5] || 0),
        seconds: Number(m[6] || 0),
    };
}

function toISO(o: any) {
    let str = 'P';
    if (o.years)   str += `${o.years}Y`;
    if (o.months)  str += `${o.months}M`;
    if (o.days)    str += `${o.days}D`;
    const t = [];
    if (o.hours)   t.push(`${o.hours}H`);
    if (o.minutes) t.push(`${o.minutes}M`);
    if (o.seconds) t.push(`${o.seconds}S`);
    if (t.length) str += 'T' + t.join('');
    return str === 'P' ? 'PT0S' : str;
}