import { getAuthHeaders, parseResponse } from '../utils/api';

export const getAllSKUs = async () => {
    try {
        const response = await fetch('/product/api/skus', {
            method: 'GET',
            headers: getAuthHeaders(),
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            let errorMessage = 'Failed to fetch SKUs';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            } else if (responseData) {
                errorMessage = responseData.message || responseData.error || (typeof responseData === 'string' ? responseData : errorMessage);
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const createSKU = async (skuData) => {
    try {
        const response = await fetch('/product/api/skus', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(skuData),
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            let errorMessage = 'Failed to create SKU';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 400) {
                errorMessage = 'Invalid SKU data. Please check your input.';
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            } else if (responseData) {
                errorMessage = responseData.message || responseData.error || (typeof responseData === 'string' ? responseData : errorMessage);
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const deleteSKU = async (skuId) => {
    try {
        const response = await fetch(`/product/api/skus/${skuId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            let errorMessage = 'Failed to delete SKU';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 404) {
                errorMessage = 'SKU not found';
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            }
            
            throw new Error(errorMessage);
        }
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const importSKUsFromCSV = async (file) => {
    try {
        const formData = new FormData();
        formData.append('file', file);
        const headers = getAuthHeaders();
        delete headers['Content-Type'];

        const response = await fetch('/product/api/skus/import', {
            method: 'POST',
            headers: headers,
            body: formData,
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            let errorMessage = 'Failed to import SKUs';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 400) {
                if (typeof responseData === 'object' && responseData !== null) {
                    errorMessage = responseData.error || responseData.message || errorMessage;
                } else {
                    errorMessage = 'Invalid file format. Please upload a valid CSV file.';
                }
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            } else {
                if (typeof responseData === 'object' && responseData !== null) {
                    errorMessage = responseData.error || responseData.message || errorMessage;
                } else if (typeof responseData === 'string') {
                    errorMessage = responseData || errorMessage;
                }
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const getSKUById = async (skuId) => {
    try {
        const response = await fetch(`/product/api/skus/${skuId}`, {
            method: 'GET',
            headers: getAuthHeaders(),
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            if (response.status === 401) {
                localStorage.removeItem('token');
                throw new Error('Unauthorized. Please log in again.');
            }
            if (response.status === 404) {
                return null;
            }
            
            let errorMessage = 'Failed to fetch SKU';
            if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            } else if (responseData) {
                errorMessage = responseData.message || responseData.error || (typeof responseData === 'string' ? responseData : errorMessage);
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const searchSKU = async (searchTerm) => {
    try {
        const response = await fetch(`/product/api/skus/search?q=${encodeURIComponent(searchTerm)}`, {
            method: 'GET',
            headers: getAuthHeaders(),
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            if (response.status === 404) {
                return null;
            }
            
            let errorMessage = 'Failed to search SKU';
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            } else if (responseData) {
                errorMessage = responseData.message || responseData.error || (typeof responseData === 'string' ? responseData : errorMessage);
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const getAllLists = async () => {
    try {
        const response = await fetch('/product/api/lists', {
            method: 'GET',
            headers: getAuthHeaders(),
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            let errorMessage = 'Failed to fetch lists';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            } else if (responseData) {
                errorMessage = responseData.message || responseData.error || (typeof responseData === 'string' ? responseData : errorMessage);
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const createList = async (listData) => {
    try {
        const response = await fetch('/product/api/lists', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(listData),
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            let errorMessage = 'Failed to create list';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            } else if (responseData) {
                errorMessage = responseData.message || responseData.error || (typeof responseData === 'string' ? responseData : errorMessage);
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const deleteList = async (listId) => {
    try {
        const response = await fetch(`/product/api/lists/${listId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });

        if (!response.ok) {
            let errorMessage = 'Failed to delete list';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 404) {
                errorMessage = 'List not found';
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            }
            
            throw new Error(errorMessage);
        }
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const addSKUToList = async (listId, skuId) => {
    try {
        const response = await fetch(`/product/api/lists/${listId}/skus/${skuId}`, {
            method: 'POST',
            headers: getAuthHeaders(),
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            let errorMessage = 'Failed to add SKU to list';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 400) {
                errorMessage = 'Invalid request. The SKU may already be in the list.';
            } else if (response.status === 404) {
                errorMessage = 'List or SKU not found';
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            } else if (responseData) {
                errorMessage = responseData.message || responseData.error || (typeof responseData === 'string' ? responseData : errorMessage);
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const removeSKUFromList = async (listId, skuId) => {
    try {
        const response = await fetch(`/product/api/lists/${listId}/skus/${skuId}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            let errorMessage = 'Failed to remove SKU from list';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 404) {
                errorMessage = 'List or SKU not found';
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const getListById = async (listId) => {
    try {
        const response = await fetch(`/product/api/lists/${listId}`, {
            method: 'GET',
            headers: getAuthHeaders(),
        });

        const responseData = await parseResponse(response);
        
        if (!response.ok) {
            let errorMessage = 'Failed to fetch list';
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 404) {
                errorMessage = 'List not found';
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            } else if (responseData) {
                errorMessage = responseData.message || responseData.error || (typeof responseData === 'string' ? responseData : errorMessage);
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

