// src/components/LanguageSwitcher/index.tsx
import { ToggleButton, ToggleButtonGroup, Box } from '@mui/material';
import { useTranslation } from 'react-i18next';

const flags = { en: '🇬🇧', fr: '🇫🇷' };

export default function LanguageSwitcher() {
    const { i18n } = useTranslation();

    return (
        <Box>
            <ToggleButtonGroup
                value={i18n.language}
                exclusive
                onChange={(_, lang) => lang && i18n.changeLanguage(lang)}
                size="small"
            >
                {Object.entries(flags).map(([lang, flag]) => (
                    <ToggleButton key={lang} value={lang} aria-label={lang}>
                        {flag} <Box component="span" sx={{ ml: 1, textTransform: 'uppercase' }}>{lang}</Box>
                    </ToggleButton>
                ))}
            </ToggleButtonGroup>
        </Box>
    );
}