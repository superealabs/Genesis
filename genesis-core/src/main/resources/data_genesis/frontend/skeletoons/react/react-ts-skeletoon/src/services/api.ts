// src/services/api.ts
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE as string;

export interface ApiResponse<T> {
    status: number;
    message: string;
    returnCode: number;
    data: T;
    timestamp: string;
}

interface SearchParams {
    endpoint: string;
    page: number;
    size: number;
    sort: `${string},${'asc' | 'desc'}`;
    filter: Record<string, unknown>;
}

export async function search<T>(
    { endpoint, page, size, sort, filter }: SearchParams
): Promise<{ content: T[]; totalPages: number }> {
    try {
        const { data } = await axios.post(
            `${API_BASE}${endpoint}`,
            filter,
            { params: { page, size, sortParam: sort } }
        );
        return {
            content: (data.data?.content as T[]) ?? [],
            totalPages: data.data?.page?.totalPages ?? 0,
        };
    } catch (err) {
        console.error('API search error', err);
        throw err;
    }
}

export async function create<T>(
    endpoint: string,
    payload: Omit<T, 'id'>
): Promise<ApiResponse<T>> {
    const { data } = await axios.post<ApiResponse<T>>(
        `${API_BASE}${endpoint}`,
        payload
    );
    return data;
}

export async function getById<T>(
    endpoint: string,
    id: number
): Promise<T> {
    const { data } = await axios.get<ApiResponse<T>>(`${API_BASE}${endpoint}/${id}`);
    return data.data;
}

export async function update<T>(
    endpoint: string,
    id: number,
    payload: Partial<T>
): Promise<ApiResponse<T>> {
    const { data } = await axios.put<ApiResponse<T>>(
        `${API_BASE}${endpoint}/${id}`,
        payload
    );
    return data;
}

export async function remove(endpoint: string, id: string | number): Promise<ApiResponse<void>> {
    const { data } = await axios.delete<ApiResponse<void>>(`${API_BASE}${endpoint}/${id}`);
    return data;
}