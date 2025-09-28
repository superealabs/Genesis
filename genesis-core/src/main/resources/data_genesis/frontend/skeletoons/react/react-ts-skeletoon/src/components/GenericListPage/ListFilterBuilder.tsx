// src/components/GenericListPage/ListFilterBuilder.tsx
import { useState, useRef } from 'react';
import { useClickOutside } from '@/hooks/useClickOutside';
import { Add, Close } from '@mui/icons-material';
import type { FilterState, FilterValue } from "@/types/filter";
import { Button, TextField, InputAdornment, IconButton, Box } from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { DateTimePicker } from '@mui/x-date-pickers/DateTimePicker';
import dayjs from 'dayjs';
import { TimePicker } from '@mui/x-date-pickers/TimePicker';
import {formatTimeTz, parseTimeTz} from "@/utils/timeTzParser";
import {DurationFilter} from "@/components/Input/DurationFilter";
import { useTranslation } from 'react-i18next';

interface Props {
    availableFilters: Record<string, { label: string; type: string }>;
    filters: FilterState;
    onChange: (filters: FilterState) => void;
    onSearch: () => void;
    setPage: (page: number) => void;
}

export default function ListFilterBuilder({
                                              availableFilters,
                                              filters,
                                              onChange,
                                              onSearch,
                                              setPage,
                                          }: Props) {
    const [openMenu, setOpenMenu] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);
    useClickOutside(menuRef, () => setOpenMenu(false));
    const { t } = useTranslation();

    const addFilter = (key: string) => {
        onChange({ ...filters, [key]: '' });
        setOpenMenu(false);
    };

    const removeFilter = (key: string) => {
        const next = { ...filters };
        delete next[key];
        onChange(next);
        if (Object.keys(next).length === 0) {
            setPage(0);
            onSearch();
        }
    };

    const setValue = (key: string, val: FilterValue) =>
        onChange({ ...filters, [key]: val });

    const usedKeys = new Set(Object.keys(filters));

    // Rendu d’un filtre date/datetime avec icône flottante unique
    const renderDateFilter = (
        key: string,
        label: string,
        renderer: React.ReactNode
    ) => (
        <Box key={key} position="relative" sx={{ width: 220, flexShrink: 0 }}>
            {renderer}
            <IconButton
                size="small"
                sx={{
                    position: 'absolute',
                    top: '50%',
                    right: 44,
                    transform: 'translateY(-50%)',
                    bgcolor: '#fff',
                    border: '1px solid #ccc',
                    width: 20,
                    height: 20,
                }}
                onClick={() => removeFilter(key)}
                title={t('messages.entity.list.removeFilter', { label })}
            >
                <Close fontSize="inherit" />
            </IconButton>
        </Box>
    );

    const renderTimeFilter = (
        key: string,
        label: string,
        renderer: React.ReactNode
    ) => (
        <Box key={key} position="relative" sx={{ width: 220, flexShrink: 0 }}>
            {renderer}
            <IconButton
                size="small"
                sx={{
                    position: 'absolute',
                    top: '50%',
                    right: 44,
                    transform: 'translateY(-50%)',
                    bgcolor: '#fff',
                    border: '1px solid #ccc',
                    width: 20,
                    height: 20,
                }}
                onClick={() => removeFilter(key)}
                title={t('messages.entity.list.removeFilter', { label })}
            >
                <Close fontSize="inherit" />
            </IconButton>
        </Box>
    );

    // Rendu avec icône flottante
    const renderTimeTzFilter = (
        key: string,
        label: string,
        renderer: React.ReactNode
    ) => (
        <Box key={key} position="relative" sx={{ width: 220, flexShrink: 0 }}>
            {renderer}
            <IconButton
                size="small"
                sx={{
                    position: 'absolute',
                    top: '50%',
                    right: 44,
                    transform: 'translateY(-50%)',
                    bgcolor: '#fff',
                    border: '1px solid #ccc',
                    width: 20,
                    height: 20,
                }}
                onClick={() => removeFilter(key)}
                title={t('messages.entity.list.removeFilter', { label })}
            >
                <Close fontSize="inherit" />
            </IconButton>
        </Box>
    );

    return (
        <Box
            display="flex"
            flexWrap="wrap"
            alignItems="center"
            gap="8px 12px"
            mb={4}
        >
            <Box fontWeight="bold">{t('messages.entity.list.filters')} :</Box>

            {Object.entries(filters).map(([key, value]) => {
                const meta = availableFilters[key];
                if (!meta) return null;

                // Date simple
                if (meta.type === 'Date') {
                    return renderDateFilter(
                        key,
                        meta.label,
                        <DatePicker
                            label={meta.label}
                            value={value ? dayjs(value as string) : null}
                            onChange={(newVal) =>
                                setValue(key, newVal ? newVal.format('YYYY-MM-DD') : '')
                            }
                            slotProps={{
                                textField: { size: 'small', variant: 'outlined', sx: { width: '100%' } },
                                popper: { sx: { zIndex: 2000 } },
                            }}
                        />
                    );
                }

                // DateTime avec fuseau horaire (UTC)
                if (meta.type === 'DateTimeTZ') {
                    return renderDateFilter(
                        key,
                        meta.label,
                        <DateTimePicker
                            label={meta.label}
                            value={value ? dayjs(value as string) : null}
                            onChange={(newVal) =>
                                setValue(key, newVal ? dayjs(newVal).utc().format('YYYY-MM-DDTHH:mm:ss.SSS[Z]') : '')
                            }
                            slotProps={{
                                textField: { size: 'small', variant: 'outlined', sx: { width: '100%' } },
                                popper: { sx: { zIndex: 2000 } },
                            }}
                        />
                    );
                }

                // DateTime sans fuseau horaire (affichage local, stockage UTC)
                // DateTime sans fuseau horaire
                if (meta.type === 'DateTime') {
                    return renderDateFilter(
                        key,
                        meta.label,
                        <DateTimePicker
                            label={meta.label}
                            value={value ? dayjs(String(value).replace('Z', '')) : null}
                            onChange={(newVal) =>
                                setValue(
                                    key,
                                    newVal ? newVal.format('YYYY-MM-DDTHH:mm:ss.SSS[Z]') : ''
                                )
                            }
                            slotProps={{
                                textField: { size: 'small', variant: 'outlined', sx: { width: '100%' } },
                                popper:   { sx: { zIndex: 2000 } },
                            }}
                        />
                    );
                }

                // TimePicker
                if (meta.type === 'time') {
                    return renderTimeFilter(
                        key,
                        meta.label,
                        <TimePicker
                            label={meta.label}
                            value={value ? dayjs(`1970-01-01T${value}`) : null}
                            onChange={(newVal) =>
                                setValue(
                                    key,
                                    newVal ? newVal.format('HH:mm:ss') : ''
                                )
                            }
                            slotProps={{
                                textField: {
                                    size: 'small',
                                    variant: 'outlined',
                                    sx: { width: '100%' },
                                },
                                popper: { sx: { zIndex: 2000 } },
                            }}
                        />
                    );
                }

                if (meta.type === 'timeTz') {
                    return renderTimeTzFilter(
                        key,
                        meta.label,
                        <TimePicker
                            label={meta.label}
                            value={value ? parseTimeTz(value as string) : null}
                            onChange={(newVal) =>
                                setValue(key, newVal ? formatTimeTz(newVal) : '')
                            }
                            timezone="UTC"
                            ampm={false}
                            slotProps={{
                                textField: {
                                    size: 'small',
                                    variant: 'outlined',
                                    sx: { width: '100%' },
                                },
                                popper: { sx: { zIndex: 2000 } },
                            }}
                        />
                    );
                }

                if (meta.type === 'interval') {
                    return (
                        <DurationFilter
                            key={key}
                            label={meta.label}
                            value={String(value)}
                            onChange={(iso) => setValue(key, iso)}
                            onRemove={() => removeFilter(key)}
                        />
                    );
                }

                // fallback TextField pour text / number
                const adornment = (
                    <InputAdornment position="end">
                        <IconButton
                            size="small"
                            onClick={() => removeFilter(key)}
                            edge="end"
                            title={t('messages.entity.list.removeFilter', { label: meta.label })}
                        >
                            <Close fontSize="inherit" />
                        </IconButton>
                    </InputAdornment>
                );

                return (
                    <TextField
                        key={key}
                        type={meta.type}
                        label={meta.label}
                        value={value ?? ''}
                        size="small"
                        variant="outlined"
                        sx={{ width: 220, flexShrink: 0 }}
                        onChange={(e) =>
                            setValue(
                                key,
                                meta.type === 'number' && e.target.value !== ''
                                    ? Number(e.target.value)
                                    : e.target.value
                            )
                        }
                        InputProps={{ endAdornment: adornment }}
                    />
                );
            })}

            <Box display="flex" alignItems="center" gap={2}>
                <Box ref={menuRef} position="relative">
                    <Button
                        size="small"
                        variant="text"
                        title={t('messages.entity.list.addFilter')}
                        onClick={() => setOpenMenu((o) => !o)}
                    >
                        <Add />
                    </Button>
                    {openMenu && (
                        <Box
                            position="absolute"
                            top="100%"
                            left={0}
                            mt={1}
                            bgcolor={(theme) => theme.palette.background.paper}
                            color={(theme) => theme.palette.text.primary}
                            boxShadow={3}
                            borderRadius={1}
                            p={1}
                            width={180}
                            maxHeight={200}
                            overflow="auto"
                            zIndex={10}
                        >
                            {Object.entries(availableFilters)
                                .filter(([key]) => !usedKeys.has(key))
                                .map(([key, { label }]) => (
                                    <Box
                                        key={key}
                                        onClick={() => addFilter(key)}
                                        sx={{
                                            p: 1,
                                            cursor: 'pointer',
                                            borderRadius: 1,
                                            '&:hover': { bgcolor: 'action.hover' },
                                        }}
                                    >
                                        {label}
                                    </Box>
                                ))}
                        </Box>
                    )}
                </Box>

                <Button variant="contained" onClick={onSearch}>
                    {t('messages.button.applySearch')}
                </Button>
            </Box>
        </Box>
    );
}