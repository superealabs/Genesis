// src/contexts/ThemeContext.tsx
import React, { createContext, useContext, useEffect, useState } from 'react';
import { ThemeProvider as MuiThemeProvider } from '@mui/material/styles';
import { createTheme } from '@mui/material';

type ThemeMode = 'light' | 'dark';

interface ThemeContextType {
    mode: ThemeMode;
    toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType>({
    mode: 'light',
    toggleTheme: () => {},
});
export const useThemeMode = () => useContext(ThemeContext);

/* ---------- couleurs sidebar ---------- */
const sidebarColors = {
    light: { bg: '#2c3e50', text: '#ffffff' },
    dark:  { bg: '#111827', text: '#e2e8f0' },
};

/* ---------- tokens ---------- */
const baseTokens = {
    typography: {
        fontFamily: '"Inter", "Roboto", "Helvetica Neue", sans-serif',
        h1: { fontSize: '2.25rem', fontWeight: 700 },
        h2: { fontSize: '1.875rem', fontWeight: 600 },
        h3: { fontSize: '1.5rem', fontWeight: 600 },
        body1: { fontSize: '1rem', lineHeight: 1.5 },
        body2: { fontSize: '0.875rem', lineHeight: 1.43 },
    },
    spacing: 4,
    shape: { borderRadius: 6 },
    grey: {
        50: '#f9fafb', 100: '#f3f4f6', 200: '#e5e7eb', 300: '#d1d5db',
        400: '#9ca3af', 500: '#6b7280', 600: '#4b5563',
        700: '#374151', 800: '#1f2937', 900: '#111827',
    },
};

/* ---------- thèmes ---------- */
const lightTheme = createTheme({
    palette: {
        mode: 'light',
        primary:   { main: '#3b82f6' },
        secondary: { main: '#ec4899' },
        error:     { main: '#ef4444' },
        warning:   { main: '#f59e0b' },
        info:      { main: '#3b82f6' },
        success:   { main: '#10b981' },
        background: { default: '#ffffff', paper: '#ffffff' },
        text:       { primary: '#111827', secondary: '#6b7280' },
        ...baseTokens.grey,
    },
    ...baseTokens,
    components: {
        MuiDrawer: {
            styleOverrides: {
                paper: { backgroundColor: sidebarColors.light.bg, color: sidebarColors.light.text },
            },
        },
        MuiAppBar: {
            styleOverrides: {
                root: { backgroundColor: sidebarColors.light.bg, color: sidebarColors.light.text },
            },
        },
    },
});

const darkTheme = createTheme({
    palette: {
        mode: 'dark',
        primary:   { main: '#90caf9' },
        secondary: { main: '#f48fb1' },
        error:     { main: '#ff5252' },
        warning:   { main: '#ffa726' },
        info:      { main: '#64b5f6' },
        success:   { main: '#4caf50' },
        background: { default: '#121212', paper: '#1e1e1e' },
        text:       { primary: '#ffffff', secondary: '#b0bec5' },
        ...baseTokens.grey,
    },
    ...baseTokens,
    components: {
        MuiDrawer: {
            styleOverrides: {
                paper: { backgroundColor: sidebarColors.dark.bg, color: sidebarColors.dark.text },
            },
        },
        MuiAppBar: {
            styleOverrides: {
                root: { backgroundColor: sidebarColors.dark.bg, color: sidebarColors.dark.text },
            },
        },
    },
});

/* ---------- provider ---------- */
export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [mode, setMode] = useState<ThemeMode>(() => {
        const saved = localStorage.getItem('theme');
        return (saved === 'dark' ? 'dark' : 'light') as ThemeMode;
    });

    useEffect(() => {
        localStorage.setItem('theme', mode);
    }, [mode]);

    const toggleTheme = () => setMode((prev) => (prev === 'light' ? 'dark' : 'light'));
    const theme = mode === 'light' ? lightTheme : darkTheme;

    return (
        <ThemeContext.Provider value={{ mode, toggleTheme }}>
            <MuiThemeProvider theme={theme}>{children}</MuiThemeProvider>
        </ThemeContext.Provider>
    );
};