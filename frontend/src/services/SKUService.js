const API_BASE_URL = '/api/skus';

const getAuthHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
};

export const getAllSKUs = async () => {
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'GET',
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            throw new Error('Failed to fetch SKUs');
        }

        return await response.json();
    } catch (error) {
        console.error('Error fetching SKUs:', error);
        throw error;
    }
};

export const createSKU = async (skuData) => {
    try {
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
    } catch (error) {
        console.error('Error creating SKU:', error);
        throw error;
    }
};

export const updateSKU = async (skuId, skuData) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${skuId}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(skuData),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to update SKU');
        }

        return await response.json();
    } catch (error) {
        console.error('Error updating SKU:', error);
        throw error;
    }
};

export const deleteSKU = async (skuId) => {
    try {
        const response = await fetch(`${API_BASE_URL}/${skuId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            throw new Error('Failed to delete SKU');
        }
    } catch (error) {
        console.error('Error deleting SKU:', error);
        throw error;
    }
};

export const importSKUsFromCSV = async (file) => {
    try {
        const token = localStorage.getItem('token');
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/import`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData,
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Failed to import SKUs');
        }

        return await response.json();
    } catch (error) {
        console.error('Error importing SKUs:', error);
        throw error;
    }
};

export const searchSKU = async (searchTerm) => {
    try {
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
    } catch (error) {
        console.error('Error searching SKU:', error);
        throw error;
    }
};

