import { useSnackbar } from 'notistack';
import { useCallback } from 'react';
import {ApiResponse} from "@/services/api";
import {useTranslation} from "react-i18next";

export function useDelete<T>(
    deleteFn: (id: string | number) => Promise<ApiResponse<void>>,
    onSuccess?: () => void
) {
    const { t } = useTranslation();
    const { enqueueSnackbar } = useSnackbar();

    const handleDelete = useCallback(
        async (id: string | number) => {
            if (!confirm(t('actions.confirmDelete'))) return;

            try {
                await deleteFn(id);
                enqueueSnackbar(t('state.success'), { variant: 'success' });
                onSuccess?.();
            } catch {
                enqueueSnackbar(t('state.error'), { variant: 'error' });
            }
        },
        [deleteFn, enqueueSnackbar, onSuccess]
    );

    return handleDelete;
}
