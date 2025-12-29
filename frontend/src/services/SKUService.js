import { getAuthHeaders } from '../utils/api';

const API_BASE_URL = '/api/skus';

export const getAllSKUs = async () => {
    const response = await fetch(API_BASE_URL, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        if (response.status === 401) {
            throw new Error('Unauthorized');
        }
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to fetch SKUs');
    }

    return await response.json();
};

export const createSKU = async (skuData) => {
    const response = await fetch(API_BASE_URL, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(skuData),
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to create SKU');
    }

    return await response.json();
};

export const deleteSKU = async (skuId) => {
    const response = await fetch(`${API_BASE_URL}/${skuId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to delete SKU');
    }
};

export const importSKUsFromCSV = async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const headers = getAuthHeaders();
    delete headers['Content-Type'];

    const response = await fetch(`${API_BASE_URL}/import`, {
        method: 'POST',
        headers: headers,
        body: formData,
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to import SKUs');
    }

    return await response.json();
};

export const getSKUById = async (skuId) => {
    const response = await fetch(`${API_BASE_URL}/${skuId}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        if (response.status === 401) {
            throw new Error('Unauthorized');
        }
        if (response.status === 404) {
            return null;
        }
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to fetch SKU');
    }

    return await response.json();
};

export const searchSKU = async (searchTerm) => {
    const response = await fetch(`${API_BASE_URL}/search?q=${encodeURIComponent(searchTerm)}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        if (response.status === 404) {
            return null;
        }
        throw new Error('Failed to search SKU');
    }

    return await response.json();
};

