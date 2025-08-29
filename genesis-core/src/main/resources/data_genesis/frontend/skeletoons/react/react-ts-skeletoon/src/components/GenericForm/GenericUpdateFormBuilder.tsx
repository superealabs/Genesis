import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import GenericFormBuilder from './GenericFormBuilder';
import { ApiResponse } from '@/services/api';
import { useSnackbar } from 'notistack';

interface Props<T extends Record<string, any>> {
    entityName: string;
    fields: Record<string, any>;
    getById: (id: number) => Promise<T>;
    update: (id: number, data: Partial<T>) => Promise<ApiResponse<T>>;
    redirectTo?: string;
    title?: string;
    detailRedirect?: string | ((id: string | number) => string);
    idKey?: string;
}

export default function GenericUpdateFormBuilder<T extends Record<string, any>>({
                                                                                    entityName,
                                                                                    fields,
                                                                                    getById,
                                                                                    update,
                                                                                    redirectTo = '/',
                                                                                    title = `Edit ${entityName}`,
                                                                                    detailRedirect,
                                                                                }: Props<T>) {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { enqueueSnackbar } = useSnackbar();
    const [initialData, setInitialData] = useState<T | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            if (!id) return;
            try {
                const data = await getById(Number(id));
                setInitialData(data);
            } catch (error) {
                enqueueSnackbar(`Failed to load ${entityName}`, { variant: 'error' });
                navigate(redirectTo);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [id, getById, enqueueSnackbar, navigate, redirectTo]);

    const handleSubmit = async (data: Partial<T>) => {
        if (!id) throw new Error('ID is required');
        const res = await update(Number(id), data);

        // planifie la redirection sans modifier la valeur de retour
        setTimeout(() => {
            const target =
                typeof detailRedirect === 'function'
                    ? detailRedirect(id)
                    : (detailRedirect || `/${entityName.toLowerCase()}s/${id}`).replace(
                        ':id',
                        String(id)
                    );
            navigate(target);
        }, 1500);

        return res; // ✅ on retourne bien ApiResponse<T>
    };

    useEffect(() => {
        console.log('Initial data loaded:', initialData);
    }, [initialData]);

    if (loading) return null;

    return initialData ? (
        <GenericFormBuilder
            entityName={entityName}
            fields={fields}
            initialData={initialData}
            onSubmit={handleSubmit}
            title={title}
        />
    ) : null;
}