// src/index.tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { router } from './router';
import './index.css';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { SnackbarProvider } from 'notistack';
import { ThemeProvider } from './contexts/ThemeContext';
import { CssBaseline } from '@mui/material';
import 'dayjs/locale/fr';
import dayjs from 'dayjs';
dayjs.locale('fr');

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <ThemeProvider>
            <CssBaseline />
            <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale="fr">
                <SnackbarProvider maxSnack={3} autoHideDuration={1500}>
                    <RouterProvider router={router} />
                </SnackbarProvider>
            </LocalizationProvider>
        </ThemeProvider>
    </React.StrictMode>
);
