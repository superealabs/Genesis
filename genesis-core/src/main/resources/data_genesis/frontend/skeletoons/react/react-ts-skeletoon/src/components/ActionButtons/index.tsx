// src/components/ActionButtons/index.tsx
import { Box, IconButton } from '@mui/material';

interface IconButton {
    icon: React.ReactNode;
    onClick: () => void;
    size?: 'sm' | 'md' | 'lg';
    title?: string;
}

interface Props {
    actions: IconButton[];
}

export default function ActionButtons({ actions }: Props) {
    return (
        <Box display="flex" gap={1} alignItems="center">
            {actions.map((a, idx) => (
                <IconButton
                    key={idx}
                    onClick={a.onClick}
                    color="primary"
                    size={a.size === 'lg' ? 'large' : a.size === 'md' ? 'medium' : 'small'}
                    title={a.title}
                >
                    {a.icon}
                </IconButton>
            ))}
        </Box>
    );
}