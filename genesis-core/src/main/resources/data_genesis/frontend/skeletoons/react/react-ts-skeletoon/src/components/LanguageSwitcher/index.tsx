// src/components/LanguageSwitcher/index.tsx
import { ToggleButton, ToggleButtonGroup, Box } from '@mui/material';
import { useTranslation } from 'react-i18next';

const languageFlags: Record<string, string> = {
    en: '🇬🇧',       // ou '🇺🇸' si tu préfères
    fr: '🇫🇷',
    es: '🇪🇸',
    de: '🇩🇪',
    it: '🇮🇹',
    pt: '🇵🇹',
    'pt-BR': '🇧🇷',
    zh: '🇨🇳',
    ja: '🇯🇵',
};

function getFlagEmoji(langCode: string): string {
    return languageFlags[langCode] || languageFlags[langCode.split('-')[0]] || '🏳️';
}

export default function LanguageSwitcher() {
    const { i18n } = useTranslation();

    const supportedLngs = Array.isArray(i18n.options.supportedLngs)
        ? i18n.options.supportedLngs.filter((l) => l !== 'cimode')
        : Object.keys(i18n.services.resourceStore.data);

    return (
        <Box>
            <ToggleButtonGroup
                value={i18n.language}
                exclusive
                onChange={(_, lang) => lang && i18n.changeLanguage(lang)}
                size="small"
            >
                {supportedLngs.map((lang) => (
                    <ToggleButton key={lang} value={lang} aria-label={lang}>
                        {getFlagEmoji(lang)}{' '}
                        <Box component="span" sx={{ ml: 1, textTransform: 'uppercase' }}>
                            {lang}
                        </Box>
                    </ToggleButton>
                ))}
            </ToggleButtonGroup>
        </Box>
    );
}