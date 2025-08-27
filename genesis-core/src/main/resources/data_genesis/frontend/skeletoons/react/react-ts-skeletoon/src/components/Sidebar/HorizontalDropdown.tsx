// src/components/Sidebar/HorizontalDropdown.tsx
import { Popover, List, ListItemButton, ListItemText, Box } from '@mui/material';
import { useState, useRef } from 'react';
import { NavLink } from 'react-router-dom';
import { NavItem } from '@/types/navigation';
import { useClickOutside } from '@/hooks/useClickOutside';
import ChevronRight from '@mui/icons-material/ChevronRight';

interface Props {
    item: NavItem;
    level?: number;
}

export default function HorizontalDropdown({ item, level = 0 }: Props) {
    const [open, setOpen] = useState(false);
    const anchorRef = useRef<HTMLDivElement>(null);
    const containerRef = useRef<HTMLDivElement>(null);
    useClickOutside(containerRef, () => setOpen(false));

    const handleEnter = () => setOpen(true);
    const handleLeave = () => setOpen(false);

    /* 1. FEUILLE : même rendu que l’ancienne branche */
    if (!item.children) {
        return (
            <NavLink to={item.path!} style={{ textDecoration: 'none', color: 'inherit' }}>
                <ListItemButton sx={{ gap: 1, py: 0.75, display: 'flex', alignItems: 'center' }}>
                    {item.icon && <Box sx={{ display: 'flex', mr: 1 }}>{item.icon}</Box>}
                    <ListItemText primary={item.label} />
                </ListItemButton>
            </NavLink>
        );
    }

    /* 2. PARENT : popover avec icônes et chevrons */
    const popoverStyles = level === 0
        ? { anchorOrigin: { vertical: 'bottom' as const, horizontal: 'left' as const },
            transformOrigin: { vertical: 'top' as const, horizontal: 'left' as const },
            mt: 1,
            ml: 0 }
        : { anchorOrigin: { vertical: 'top' as const, horizontal: 'right' as const },
            transformOrigin: { vertical: 'top' as const, horizontal: 'left' as const },
            mt: 0,
            ml: 1 };

    return (
        <Box ref={containerRef} onMouseEnter={handleEnter} onMouseLeave={handleLeave} sx={{ position: 'relative' }}>
            <Box ref={anchorRef}>
                <ListItemButton sx={{ gap: 1, py: 0.75, display: 'flex', alignItems: 'center' }}>
                    {item.icon && <Box sx={{ display: 'flex', mr: 1 }}>{item.icon}</Box>}
                    <ListItemText primary={item.label} sx={{ flexGrow: 1 }} />
                    {level >= 1 && <ChevronRight fontSize="small" />}
                </ListItemButton>
            </Box>

            <Popover
                open={open}
                anchorEl={anchorRef.current}
                anchorOrigin={{
                    vertical: popoverStyles.anchorOrigin.vertical,
                    horizontal: popoverStyles.anchorOrigin.horizontal,
                }}
                transformOrigin={{
                    vertical: popoverStyles.transformOrigin.vertical,
                    horizontal: popoverStyles.transformOrigin.horizontal,
                }}
                PaperProps={{
                    onMouseEnter: handleEnter,
                    onMouseLeave: handleLeave,
                    sx: { minWidth: anchorRef.current?.offsetWidth, mt: popoverStyles.mt, ml: popoverStyles.ml },
                }}
                disableRestoreFocus
            >
                <List dense>
                    {item.children.map((child) => (
                        <HorizontalDropdown key={child.label} item={child} level={level + 1} />
                    ))}
                </List>
            </Popover>
        </Box>
    );
}