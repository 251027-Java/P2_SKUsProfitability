const API_BASE_URL = '/api/lists';

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const getAllLists = async () => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'GET',
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            throw new Error('Failed to fetch lists');
        }

        return await response.json();
    } catch (error) {
        console.error('Error fetching lists:', error);
        throw error;
    }
};

export const createList = async (listData) => {
    try {
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
    } catch (error) {
        console.error('Error creating list:', error);
        throw error;
    }
};

export const updateList = async (listId, listData) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${listId}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(listData),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to update list');
        }

        return await response.json();
    } catch (error) {
        console.error('Error updating list:', error);
        throw error;
    }
};

export const deleteList = async (listId) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${listId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            throw new Error('Failed to delete list');
        }
    } catch (error) {
        console.error('Error deleting list:', error);
        throw error;
    }
};

export const addSKUToList = async (listId, skuId) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${listId}/skus/${skuId}`, {
            method: 'POST',
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to add SKU to list');
        }

        return await response.json();
    } catch (error) {
        console.error('Error adding SKU to list:', error);
        throw error;
    }
};

export const removeSKUFromList = async (listId, skuId) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${listId}/skus/${skuId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            throw new Error('Failed to remove SKU from list');
        }

        return await response.json();
    } catch (error) {
        console.error('Error removing SKU from list:', error);
        throw error;
    }
};

