const API_BASE_URL = '/api/auth';

export const login = async (email, password) => {
    try {
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) {
            const errorText = await response.text();
            let errorMessage = 'Login failed';
            
            if (response.status === 404) {
                errorMessage = 'User not found';
            } else if (response.status === 401) {
                errorMessage = 'Invalid password';
            } else {
                errorMessage = errorText || 'Login failed';
            }
            
            throw new Error(errorMessage);
        }

        const data = await response.json();
        return data.token;
    } catch (error) {
        if (error.message) {
            throw error;
        }
        throw new Error('Network error. Please try again.');
    }
};

export const register = async (registerData) => {
    try {
        const response = await fetch(`${API_BASE_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(registerData),
        });

        if (!response.ok) {
            const errorText = await response.text();
            let errorMessage = 'Registration failed';
            
            if (response.status === 400) {
                errorMessage = errorText || 'Invalid registration data';
            } else if (response.status === 409) {
                errorMessage = 'Email already in use';
            } else {
                errorMessage = errorText || 'Registration failed';
            }
            
            throw new Error(errorMessage);
        }

        const data = await response.json();
        return data.token;
    } catch (error) {
        if (error.message) {
            throw error;
        }
        throw new Error('Network error. Please try again.');
    }
};

export const getToken = () => {
    return localStorage.getItem('token');
};

export const isAuthenticated = () => {
    return !!getToken();
};

export const logout = () => {
    localStorage.removeItem('token');
};

