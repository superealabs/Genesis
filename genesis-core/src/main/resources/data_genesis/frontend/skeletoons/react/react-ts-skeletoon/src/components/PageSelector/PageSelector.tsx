// src/components/PageSelector/PageSelector.tsx
import { Select as MuiSelect, MenuItem, FormControl, Box } from '@mui/material';
import { smallSelectSx,smallFormControlSx, smallMenuItemSx, inlineLabelSx} from '@/styles/mui-patterns';
import { useTranslation } from 'react-i18next';

interface Props {
    totalPages: number;
    currentPage: number;
    onChangePage: (page: number) => void;
}

export default function PageSelector({ totalPages, currentPage, onChangePage }: Props) {
    const { t } = useTranslation();
    const options = Array.from({ length: totalPages }, (_, i) => ({
        value: i,
        label: (i + 1).toString(),
    }));

    return (
        <Box display="flex" alignItems="center" gap={2}>
            <label style={inlineLabelSx}>{t('list.goToPage')} :</label>
            <FormControl sx={smallFormControlSx}>
                <MuiSelect
                    value={currentPage}
                    onChange={(e) => onChangePage(Number(e.target.value))}
                    sx={smallSelectSx}
                >
                    {options.map((opt) => (
                        <MenuItem key={opt.value} value={opt.value} sx={smallMenuItemSx}>
                            {opt.label}
                        </MenuItem>
                    ))}
                </MuiSelect>
            </FormControl>
        </Box>
    );
}