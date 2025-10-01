// src/components/Sidebar/Sidebar.tsx
import { useState } from 'react';
import {AppBar, Drawer, Toolbar, IconButton, Box, FormControlLabel, Switch} from '@mui/material';
import { ChevronLeft, ChevronRight } from '@mui/icons-material';
import { navItems } from '../../config/navItems';   // new file
import { drawerWidth, closedWidth } from '@/styles/sidebar';
import HorizontalDropdown from './HorizontalDropdown';
import VerticalDropdown from './VerticalDropdown';
import { useThemeMode } from '@/contexts/ThemeContext';   // 👈 nouveau
import { Brightness4, Brightness7 } from '@mui/icons-material';
import { SidebarTitle } from './SidebarTitle';
import LanguageSwitcher from "@/components/LanguageSwitcher";

interface Props {
    layout?: 'vertical' | 'horizontal';
}

export default function Sidebar({ layout = 'vertical' }: Props) {
    const [open, setOpen] = useState(true);
    const { mode, toggleTheme } = useThemeMode();
    const toggle = () => setOpen((prev) => !prev);

    if (layout === 'horizontal') {
        return (
            <AppBar position="sticky">
                <Toolbar sx={{ gap: 2, justifyContent: 'space-between' }}>
                    <Box display="flex" gap={2} alignItems="center">
                        <SidebarTitle />
                        {navItems.map((item) => (
                            <HorizontalDropdown key={item.label} item={item} />
                        ))}
                    </Box>

                    {/* Switch stylisé */}
                    <FormControlLabel
                        control={
                            <Switch
                                checked={mode === 'dark'}
                                onChange={toggleTheme}
                                icon={<Brightness4 sx={{ fontSize: 20 }} />}
                                checkedIcon={<Brightness7 sx={{ fontSize: 20 }} />}
                                color="default"
                            />
                        }
                        label=""
                        sx={{ mr: 2 }}
                    />
                </Toolbar>
            </AppBar>
        );
    }

    /* ---------- Vertical (default) ---------- */
    return (
        <>
            <Drawer
                variant="permanent"
                sx={{
                    width: open ? drawerWidth : closedWidth,
                    flexShrink: 0,
                    '& .MuiDrawer-paper': {
                        width: open ? drawerWidth : closedWidth,
                        transition: (t) => t.transitions.create('width'),
                        overflowX: 'hidden',
                        display: 'flex',               // ← permet de pousser le switch en bas
                        flexDirection: 'column',
                    },
                }}
            >
                {/* Header */}
                <Toolbar
                    sx={{
                        display: 'flex',
                        alignItems: 'center',
                        px: 1,
                        justifyContent: 'space-between', // ← pousse le chevron à droite
                    }}
                >
                    <SidebarTitle compact={!open} />
                    <IconButton onClick={toggle} color="inherit">
                        {open ? <ChevronLeft /> : <ChevronRight />}
                    </IconButton>
                </Toolbar>

                {/* Contenu du menu */}
                {navItems.map((i, idx) => (
                    <Box key={i.label} sx={{ mb: idx < navItems.length - 1 ? 0.75 : 0 }}>
                        <VerticalDropdown item={i} compact={!open} />
                    </Box>
                ))}

                {/* Switch en bas */}
                <Box
                    sx={{
                        mt: 'auto',
                        p: 1,
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                    }}
                >
                    <FormControlLabel
                        control={
                            <Switch
                                checked={mode === 'dark'}
                                onChange={toggleTheme}
                                icon={<Brightness4 sx={{ fontSize: 18 }} />}
                                checkedIcon={<Brightness7 sx={{ fontSize: 18 }} />}
                                color="default"
                            />
                        }
                        label={open ? t(mode === 'dark' ? 'messages.theme.dark' : 'messages.theme.light') : ''}
                        labelPlacement="bottom"
                        sx={{
                            color: 'inherit',
                            m: 0,
                            // si le drawer est fermé, on cache le label et on centre l’icône
                            ...(open
                                ? {}
                                : {
                                    '& .MuiFormControlLabel-label': { display: 'none' },
                                    '& .MuiSwitch-root': { m: 0 },
                                }),
                        }}
                    />
                    <LanguageSwitcher />
                </Box>
            </Drawer>
        </>
    );
}