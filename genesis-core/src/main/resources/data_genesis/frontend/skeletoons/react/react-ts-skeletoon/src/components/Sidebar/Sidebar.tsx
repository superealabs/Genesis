// src/components/Sidebar/Sidebar.tsx
import { useState } from 'react';
import { AppBar, Drawer, Toolbar, IconButton, Box } from '@mui/material';
import { ChevronLeft, ChevronRight } from '@mui/icons-material';
import { navItems } from '../../config/navItems';
import { drawerWidth, closedWidth } from '@/styles/sidebar';
import HorizontalDropdown from './HorizontalDropdown';
import VerticalDropdown from './VerticalDropdown';
import { SidebarTitle } from './SidebarTitle';
import SettingsMenu from '@/components/SettingsMenu';
import { useTranslation } from 'react-i18next';

interface Props {
    layout?: 'vertical' | 'horizontal';
    onLayoutChange?: (layout: 'vertical' | 'horizontal') => void;
}

export default function Sidebar({ layout = 'vertical', onLayoutChange }: Props) {
    const [open, setOpen] = useState(true);
    const toggle = () => setOpen((prev) => !prev);
    const { t } = useTranslation();

    const handleLayoutChange = (newLayout: 'vertical' | 'horizontal') => {
        onLayoutChange?.(newLayout);
    };

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

                    {/* Menu des paramètres */}
                    <SettingsMenu
                        layout={layout}
                        onLayoutChange={handleLayoutChange}
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
                        display: 'flex',
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
                        justifyContent: 'space-between',
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

                {/* Menu des paramètres en bas */}
                <Box
                    sx={{
                        mt: 'auto',
                        p: 1,
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                    }}
                >
                    <SettingsMenu
                        layout={layout}
                        onLayoutChange={handleLayoutChange}
                        compact={!open}
                    />
                </Box>
            </Drawer>
        </>
    );
}