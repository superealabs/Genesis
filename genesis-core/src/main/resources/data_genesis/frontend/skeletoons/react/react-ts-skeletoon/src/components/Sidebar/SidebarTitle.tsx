// src/components/Sidebar/SidebarTitle.tsx
import { appConfig } from '@/config/app';
import { Box } from '@mui/material';

interface Props { compact?: boolean }

export const SidebarTitle = ({ compact = false }: Props) => {
    const { title, logoUrl } = appConfig;

    if (logoUrl) {
        return (
            <Box
                component="img"
                src={logoUrl}
                alt="Logo"
                sx={{
                    height: compact ? 28 : 36,
                    objectFit: 'contain',
                    transition: 'height 0.2s',
                }}
            />
        );
    }
    return (
        <Box sx={{ fontWeight: 'bold', fontSize: compact ? 12 : 16 }}>
            {compact ? title.slice(0, 1) : title}
        </Box>
    );
};