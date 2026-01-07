import { getAuthHeaders, parseResponse } from '../utils/api';

export const calculateFees = async (calculationData) => {
    try {
        const response = await fetch('/calculator/api/calculator/calculate', {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(calculationData),
        });

        const responseData = await parseResponse(response);

        if (!response.ok) {
            let errorMessage = 'Calculation failed';
            
            if (typeof responseData === 'object' && responseData !== null) {
                errorMessage = responseData.error || responseData.message || errorMessage;
            } else if (typeof responseData === 'string') {
                errorMessage = responseData || errorMessage;
            }
            
            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 400) {
                if (!errorMessage || errorMessage === 'Calculation failed') {
                    errorMessage = 'Invalid input. Please check all required fields are filled correctly.';
                }
            } else if (response.status === 404) {
                errorMessage = 'Calculator service not found. Please ensure the calculator service is running on port 8083 and registered with Eureka.';
            } else if (response.status === 503) {
                errorMessage = 'Calculator service is unavailable. The service may be starting up or experiencing issues. Please wait a moment and try again.';
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please check if the backend is running.';
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) {
            throw error;
        }
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure the backend is running on port 8080.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const checkCalculatorHealth = async () => {
    try {
        const response = await fetch('/calculator/api/calculator/health', {
            method: 'GET',
            headers: getAuthHeaders(),
        });
        return response.ok;
    } catch (error) {
        return false;
    }
};

export const calculateBySku = async (sku, requestData) => {
    try {
        const response = await fetch(`/calculator/api/calculator/calculate/sku?sku=${encodeURIComponent(sku)}`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(requestData),
        });

        const responseData = await parseResponse(response);

        if (!response.ok) {
            let errorMessage = 'SKU-based calculation failed';
            
            if (typeof responseData === 'object' && responseData !== null) {
                errorMessage = responseData.error || responseData.message || errorMessage;
            }

            if (response.status === 401) {
                errorMessage = 'Unauthorized. Please log in again.';
                localStorage.removeItem('token');
            } else if (response.status === 404) {
                errorMessage = `SKU "${sku}" not found in the calculator database.`;
            } else if (response.status === 500) {
                errorMessage = 'Server error. Please ensure the calculator service is running.';
            }
            
            throw new Error(errorMessage);
        }

        return responseData;
    } catch (error) {
        if (error.message) throw error;
        throw new Error('Network error. Please try again.');
    }
};

