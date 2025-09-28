// src/components/PaginationSize/PaginationSize.tsx
import { Select as MuiSelect, MenuItem, FormControl, SelectChangeEvent } from '@mui/material';
import { smallSelectSx,smallFormControlSx, smallMenuItemSx, inlineLabelSx} from '@/styles/mui-patterns';
import {useTranslation} from "react-i18next";

interface Props {
    size: number;
    onChange: (size: number) => void;
    onResetPage?: () => void;
}

export default function PaginationSize({ size, onChange, onResetPage }: Props) {
    const { t } = useTranslation();
    const options = [
        { value: 1, label: '1' },
        { value: 2, label: '2' },
        { value: 5, label: '5' },
        { value: 10, label: '10' },
        { value: 20, label: '20' },
        { value: 50, label: '50' },
    ];

    const handleChange = (event: SelectChangeEvent<number>) => {
        const newSize = event.target.value;
        onChange(newSize as number);
        onResetPage?.();
    };

    return (
        <label style={inlineLabelSx}>
            {t('messages.pagination.sizeLabel')} : &nbsp;&nbsp;
            <FormControl sx={smallFormControlSx}>
                <MuiSelect
                    value={size}
                    onChange={handleChange}
                    sx={smallSelectSx}
                >
                    {options.map((opt) => (
                        <MenuItem key={opt.value} value={opt.value} sx={smallMenuItemSx}>
                            {opt.label}
                        </MenuItem>
                    ))}
                </MuiSelect>
            </FormControl>
        </label>
    );
}