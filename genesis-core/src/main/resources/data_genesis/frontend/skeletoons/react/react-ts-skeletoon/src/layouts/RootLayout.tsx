// src/layouts/RootLayout.tsx
import Sidebar from '@/components/Sidebar/Sidebar';
import { Outlet } from 'react-router-dom';
import { useState } from 'react';

export default function RootLayout() {
    const [layout, setLayout] = useState<'vertical' | 'horizontal'>('vertical');

    return (
        <>
            {layout === 'horizontal' ? (
                <>
                    <Sidebar layout="horizontal" onLayoutChange={setLayout} />
                    <Outlet />
                </>
            ) : (
                <div style={{ display: 'flex', minHeight: '100vh' }}>
                    <Sidebar layout="vertical" onLayoutChange={setLayout} />
                    <main style={{ flex: 1, padding: '1rem' }}>
                        <Outlet />
                    </main>
                </div>
            )}
        </>
    );
}