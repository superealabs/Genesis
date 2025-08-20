// src/components/GenericListPage/ListFilterBuilder.tsx
import { useState, useRef } from 'react';
import { useClickOutside } from '@/hooks/useClickOutside';
import { Add, Close } from '@mui/icons-material';
import type { FilterState, FilterType, FilterValue } from "@/types/filter";
import { Button, TextField, InputAdornment, IconButton, Box } from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs from 'dayjs';

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

    return (
        <Box
            display="flex"
            flexWrap="wrap"
            alignItems="center"
            gap="8px 12px"
            mb={4}
        >
            <Box fontWeight="bold">Filters :</Box>

            {Object.entries(filters).map(([key, value]) => {
                const meta = availableFilters[key];
                if (!meta) return null;

                const adornment = (
                    <InputAdornment position="end">
                        <IconButton
                            size="small"
                            onClick={() => removeFilter(key)}
                            edge="end"
                            title={`Remove ${meta.label} filter`}
                        >
                            <Close fontSize="inherit" />
                        </IconButton>
                    </InputAdornment>
                );

                if (meta.type === 'Date') {
                    return (
                        <Box key={key} position="relative" sx={{ width: 220, flexShrink: 0 }}>
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

                            {/* Croix flottant décalé de la largeur de l’icône calendrier */}
                            <IconButton
                                size="small"
                                sx={{
                                    position: 'absolute',
                                    top: '50%',
                                    right: 44,          // ≃ largeur calendrier (24 px) + petite marge
                                    transform: 'translateY(-50%)',
                                    bgcolor: '#fff',
                                    border: '1px solid #ccc',
                                    width: 20,
                                    height: 20,
                                }}
                                onClick={() => removeFilter(key)}
                                title={`Remove ${meta.label} filter`}
                            >
                                <Close fontSize="inherit" />
                            </IconButton>
                        </Box>
                    );
                }

                // fallback TextField pour text / number
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
                        title="Ajouter un filtre"
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
                    Apply
                </Button>
            </Box>
        </Box>
    );
}