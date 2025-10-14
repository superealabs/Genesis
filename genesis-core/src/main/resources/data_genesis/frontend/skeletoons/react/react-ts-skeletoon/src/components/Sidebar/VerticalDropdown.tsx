// src/components/Sidebar/VerticalDropdown.tsx
import { Collapse, List, ListItemButton, ListItemText, Box } from '@mui/material';
import { ExpandMore, ExpandLess } from '@mui/icons-material';
import { useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { NavItem } from '@/types/navigation';
import {useTranslation} from "react-i18next";

interface Props {
    item: NavItem;
    level?: number;
    compact?: boolean;
}

export default function VerticalDropdown({ item, level = 0, compact = false }: Props) {
    const { pathname } = useLocation();
    const [open, setOpen] = useState(false);
    const { t } = useTranslation();

    const isActive = pathname === item.path;

    const toggle = () => setOpen((o) => !o);

    const Wrapper = ({ children }: { children: React.ReactNode }) =>
        item.path ? (
            <NavLink to={item.path} style={{ textDecoration: 'none', color: 'inherit' }}>
                {children}
            </NavLink>
        ) : (
            <>{children}</>
        );

    return (
        <>
            <Wrapper>
                <ListItemButton
                    selected={isActive}
                    sx={(theme) => ({
                        pl: 2 + level * 3,
                        justifyContent: 'space-between',
                        bgcolor: isActive
                            ? theme.palette.mode === 'light'
                                ? theme.palette.primary.light   // éclatant en light
                                : theme.palette.primary.dark    // éclatant en dark
                            : 'transparent',
                        color: isActive
                            ? theme.palette.getContrastText(
                                theme.palette.mode === 'light'
                                    ? theme.palette.primary.light
                                    : theme.palette.primary.dark
                            )
                            : 'inherit',
                        '&:hover': {
                            bgcolor: theme.palette.action.hover,
                        },
                    })}
                    onClick={item.children ? toggle : undefined}
                >
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        {item.icon && <Box sx={{ fontSize: 20 }}>{item.icon}</Box>}
                        {!compact && (
                            <>
                                <ListItemText
                                    primary={t(item.label)}
                                    sx={{ ml: 1, whiteSpace: 'nowrap' }}
                                />
                                {item.children && (open ? <ExpandLess /> : <ExpandMore />)}
                            </>
                        )}
                    </Box>
                </ListItemButton>
            </Wrapper>

            {item.children && !compact && (
                <Collapse in={open} timeout="auto" unmountOnExit>
                    <List dense disablePadding>
                        {item.children.map((child) => (
                            <VerticalDropdown key={child.label} item={child} level={level + 1} />
                        ))}
                    </List>
                </Collapse>
            )}
        </>
    );
}