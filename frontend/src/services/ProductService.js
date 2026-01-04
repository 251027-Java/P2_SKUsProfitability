import { getAuthHeaders } from '../utils/api';

// Product Service base URL (handles both SKUs and Lists)
const PRODUCT_BASE_URL = '/product';

export const getAllSKUs = async () => {
    const response = await fetch(`${PRODUCT_BASE_URL}/skus`, {
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
    const response = await fetch(`${PRODUCT_BASE_URL}/skus`, {
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
    const response = await fetch(`${PRODUCT_BASE_URL}/skus/${skuId}`, {
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

    const response = await fetch(`${PRODUCT_BASE_URL}/skus/import`, {
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
    const response = await fetch(`${PRODUCT_BASE_URL}/skus/${skuId}`, {
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
    const response = await fetch(`${PRODUCT_BASE_URL}/skus/search?q=${encodeURIComponent(searchTerm)}`, {
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

export const getAllLists = async () => {
    const response = await fetch(`${PRODUCT_BASE_URL}/lists`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to fetch lists');
    }

    return await response.json();
};

export const createList = async (listData) => {
    const response = await fetch(`${PRODUCT_BASE_URL}/lists`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(listData),
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to create list');
    }

    return await response.json();
};

export const deleteList = async (listId) => {
    const response = await fetch(`${PRODUCT_BASE_URL}/lists/${listId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to delete list');
    }
};

export const addSKUToList = async (listId, skuId) => {
    const response = await fetch(`${PRODUCT_BASE_URL}/lists/${listId}/skus/${skuId}`, {
        method: 'POST',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to add SKU to list');
    }

    return await response.json();
};

export const removeSKUFromList = async (listId, skuId) => {
    const response = await fetch(`${PRODUCT_BASE_URL}/lists/${listId}/skus/${skuId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to remove SKU from list');
    }

    return await response.json();
};

export const getListById = async (listId) => {
    const response = await fetch(`${PRODUCT_BASE_URL}/lists/${listId}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to fetch list');
    }

    return await response.json();
};

