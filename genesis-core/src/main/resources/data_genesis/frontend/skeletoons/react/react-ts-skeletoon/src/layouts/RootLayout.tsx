// src/layouts/RootLayout.tsx
import Sidebar from '@/components/Sidebar/Sidebar';
import { Outlet } from 'react-router-dom';

interface Props {
    layout?: 'vertical' | 'horizontal';
}

export default function RootLayout({ layout = 'vertical' }: Props) {
    return (
        <>
            {layout === 'vertical' ? (
                <>
                    <Sidebar layout="horizontal" />
                    <Outlet />
                </>
            ) : (
                <div style={{ display: 'flex', minHeight: '100vh' }}>
                    <Sidebar layout="vertical" />
                    <main style={{ flex: 1, padding: '1rem' }}>
                        <Outlet />
                    </main>
                </div>
            )}
        </>
    );
}