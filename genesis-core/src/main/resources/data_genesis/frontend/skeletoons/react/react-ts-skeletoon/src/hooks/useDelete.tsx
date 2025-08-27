import { useSnackbar } from 'notistack';
import { useCallback } from 'react';
import {ApiResponse} from "@/services/api";

export function useDelete<T>(
    deleteFn: (id: string | number) => Promise<ApiResponse<void>>,
    onSuccess?: () => void
) {
    const { enqueueSnackbar } = useSnackbar();

    const handleDelete = useCallback(
        async (id: string | number) => {
            if (!confirm('Confirmer la suppression ?')) return;

            try {
                await deleteFn(id);
                enqueueSnackbar('Supprimé avec succès', { variant: 'success' });
                onSuccess?.();
            } catch {
                enqueueSnackbar('Erreur lors de la suppression', { variant: 'error' });
            }
        },
        [deleteFn, enqueueSnackbar, onSuccess]
    );

    return handleDelete;
}