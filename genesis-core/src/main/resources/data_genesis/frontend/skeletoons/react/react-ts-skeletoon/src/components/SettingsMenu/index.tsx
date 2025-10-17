// src/components/SettingsMenu/index.tsx
import { useState } from 'react';
import {
    IconButton,
    Menu,
    MenuItem,
    ListItemIcon,
    ListItemText,
    Divider,
    FormControl,
    InputLabel,
    Select,
    SelectChangeEvent,
    Box,
} from '@mui/material';
import { Settings, Brightness4, Brightness7, ViewSidebar, ViewAgenda, Logout } from '@mui/icons-material';
import { useThemeMode } from '@/contexts/ThemeContext';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

interface SettingsMenuProps {
    layout: 'vertical' | 'horizontal';
    onLayoutChange: (layout: 'vertical' | 'horizontal') => void;
    compact?: boolean;
}

export default function SettingsMenu({ layout, onLayoutChange, compact = false }: SettingsMenuProps) {
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const { mode, toggleTheme } = useThemeMode();
    const { i18n, t } = useTranslation();
    const navigate = useNavigate();

    const open = Boolean(anchorEl);

    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleLayoutChange = (newLayout: 'vertical' | 'horizontal') => {
        onLayoutChange(newLayout);
        handleClose();
    };

    const handleLanguageChange = (event: SelectChangeEvent<string>) => {
        i18n.changeLanguage(event.target.value);
    };

    // Fonction sécurisée pour obtenir les langues supportées
    const getSupportedLanguages = (): string[] => {
        if (Array.isArray(i18n.options.supportedLngs)) {
            return i18n.options.supportedLngs.filter((l) => l !== 'cimode');
        }

        // Alternative: utiliser les ressources chargées
        if (i18n.services.resourceStore.data) {
            return Object.keys(i18n.services.resourceStore.data).filter((l) => l !== 'cimode');
        }

        // Fallback: utiliser la langue actuelle
        return [i18n.language || 'en'];
    };

    const supportedLanguages = getSupportedLanguages();

    return (
        <>
            <IconButton onClick={handleClick} color="inherit" size={compact ? 'small' : 'medium'}>
                <Settings />
            </IconButton>

            <Menu
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
                onClick={handleClose}
                PaperProps={{
                    elevation: 0,
                    sx: {
                        overflow: 'visible',
                        filter: 'drop-shadow(0px 2px 8px rgba(0,0,0,0.32))',
                        mt: 1.5,
                        '& .MuiAvatar-root': {
                            width: 32,
                            height: 32,
                            ml: -0.5,
                            mr: 1,
                        },
                    },
                }}
                transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
            >
                {/* Mode disposition */}
                <MenuItem onClick={() => handleLayoutChange(layout === 'vertical' ? 'horizontal' : 'vertical')}>
                    <ListItemIcon>
                        {layout === 'vertical' ? <ViewAgenda fontSize="small" /> : <ViewSidebar fontSize="small" />}
                    </ListItemIcon>
                    <ListItemText>
                        {layout === 'vertical'
                            ? t('messages.layout.switchToHorizontal')
                            : t('messages.layout.switchToVertical')
                        }
                    </ListItemText>
                </MenuItem>

                <Divider />

                {/* Mode clair/sombre */}
                <MenuItem onClick={toggleTheme}>
                    <ListItemIcon>
                        {mode === 'dark' ? <Brightness7 fontSize="small" /> : <Brightness4 fontSize="small" />}
                    </ListItemIcon>
                    <ListItemText>
                        {mode === 'dark'
                            ? t('messages.theme.switchToLight')
                            : t('messages.theme.switchToDark')
                        }
                    </ListItemText>
                </MenuItem>

                <Divider />

                {/* Choix de langue */}
                <MenuItem>
                    <FormControl fullWidth size="small" sx={{ minWidth: 120 }}>
                        <InputLabel>{t('messages.language.choose')}</InputLabel>
                        <Select
                            value={i18n.language}
                            label={t('messages.language.choose')}
                            onChange={handleLanguageChange}
                        >
                            {supportedLanguages.map((lang) => (
                                <MenuItem key={lang} value={lang}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                        <span>{getFlagEmoji(lang)}</span>
                                        <span>{t(`messages.language.${lang}`)}</span>
                                    </Box>
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </MenuItem>
            </Menu>
        </>
    );
}

// Helper function pour les emojis de drapeaux
function getFlagEmoji(langCode: string): string {
    const languageFlags: Record<string, string> = {
        en: '🇬🇧',
        fr: '🇫🇷',
        es: '🇪🇸',
        de: '🇩🇪',
        it: '🇮🇹',
        pt: '🇵🇹',
        'pt-BR': '🇧🇷',
        zh: '🇨🇳',
        ja: '🇯🇵',
    };
    return languageFlags[langCode] || languageFlags[langCode.split('-')[0]] || '🏳️';
}