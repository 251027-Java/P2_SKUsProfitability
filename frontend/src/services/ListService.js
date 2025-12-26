import { getAuthHeaders } from '../utils/api';

const API_BASE_URL = '/api/lists';

export const getAllLists = async () => {
    const response = await fetch(API_BASE_URL, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to fetch lists');
    }

    return await response.json();
};

export const createList = async (listData) => {
    const response = await fetch(API_BASE_URL, {
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
    const response = await fetch(`${API_BASE_URL}/${listId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to delete list');
    }
};

export const addSKUToList = async (listId, skuId) => {
    const response = await fetch(`${API_BASE_URL}/${listId}/skus/${skuId}`, {
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
    const response = await fetch(`${API_BASE_URL}/${listId}/skus/${skuId}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to remove SKU from list');
    }

    return await response.json();
};

export const getListById = async (listId) => {
    const response = await fetch(`${API_BASE_URL}/${listId}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error('Failed to fetch list');
    }

    return await response.json();
};

