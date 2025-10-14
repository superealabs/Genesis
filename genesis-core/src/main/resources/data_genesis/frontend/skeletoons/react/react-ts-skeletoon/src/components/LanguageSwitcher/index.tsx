// src/components/LanguageSwitcher/index.tsx
import { IconButton, Menu, MenuItem, Box } from '@mui/material';
import { Language } from '@mui/icons-material';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';

export default function LanguageSwitcher() {
    const { i18n, t } = useTranslation();
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);

    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleLanguageChange = (lang: string) => {
        i18n.changeLanguage(lang);
        handleClose();
    };

    const getSupportedLanguages = (): string[] => {
        if (Array.isArray(i18n.options.supportedLngs)) {
            return i18n.options.supportedLngs.filter(l => l !== 'cimode');
        }
        if (i18n.services.resourceStore.data) {
            return Object.keys(i18n.services.resourceStore.data).filter(l => l !== 'cimode');
        }
        return [i18n.language || 'en'];
    };

    const getFlagEmoji = (langCode: string): string => {
        const languageFlags: Record<string, string> = {
            en: '🇬🇧',
            fr: '🇫🇷',
        };
        return languageFlags[langCode] || languageFlags[langCode.split('-')[0]] || '🌐';
    };

    const supportedLanguages = getSupportedLanguages();

    return (
        <>
            <IconButton onClick={handleClick} color="inherit" size="small" title={t('messages.language.choose')}>
                <Language />
            </IconButton>
            <Menu
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
                transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
            >
                {supportedLanguages.map(lang => (
                    <MenuItem key={lang} onClick={() => handleLanguageChange(lang)}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <span>{getFlagEmoji(lang)}</span>
                            <span>{t(`messages.language.${lang}`)}</span>
                        </Box>
                    </MenuItem>
                ))}
            </Menu>
        </>
    );
}
