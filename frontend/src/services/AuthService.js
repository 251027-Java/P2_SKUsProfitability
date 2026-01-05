export const login = async (email, password) => {
    try {
        const response = await fetch('/auth/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email, password }),
        });

        const responseText = await response.text();
        
        if (!response.ok) {
            let errorMessage = 'Login failed';
            let errorData = null;
            
            try {
                errorData = JSON.parse(responseText);
                errorMessage = errorData.message || errorData.error || errorMessage;
            } catch {
                errorMessage = responseText || errorMessage;
            }
            
            if (response.status === 404) {
                errorMessage = 'User not found';
            } else if (response.status === 401) {
                errorMessage = 'Invalid email or password';
            } else if (response.status === 500) {
                if (errorData?.message) {
                    errorMessage = `Server error: ${errorData.message}`;
                } else if (responseText?.trim()) {
                    errorMessage = `Server error: ${responseText}`;
                } else {
                    errorMessage = 'Server error. Auth-service (port 8081) may not be running.';
                }
            }
            
            throw new Error(errorMessage);
        }

        const data = JSON.parse(responseText);
        if (!data || !data.token) {
            throw new Error('Invalid response from server');
        }
        return data.token;
    } catch (error) {
        if (error.message) {
            throw error;
        }
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure auth-service is running on port 8081.');
        }
        throw new Error('Network error. Please try again.');
    }
};

export const register = async (registerData) => {
    try {
        const response = await fetch('/auth/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(registerData),
        });

        const responseText = await response.text();
        
        if (!response.ok) {
            let errorMessage = 'Registration failed';
            let errorData = null;
            
            try {
                errorData = JSON.parse(responseText);
                errorMessage = errorData.message || errorData.error || errorMessage;
            } catch {
                errorMessage = responseText || errorMessage;
            }
            
            if (response.status === 400) {
                errorMessage = errorMessage || 'Invalid registration data';
            } else if (response.status === 409) {
                errorMessage = 'Email already in use';
            } else if (response.status === 500) {
                if (errorData?.message) {
                    errorMessage = `Server error: ${errorData.message}`;
                } else if (responseText?.trim()) {
                    errorMessage = `Server error: ${responseText}`;
                } else {
                    errorMessage = 'Server error. Auth-service (port 8081) may not be running.';
                }
            }
            
            throw new Error(errorMessage);
        }

        const data = JSON.parse(responseText);
        if (!data || !data.token) {
            throw new Error('Invalid response from server');
        }
        return data.token;
    } catch (error) {
        if (error.message) {
            throw error;
        }
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Cannot connect to server. Please ensure auth-service is running on port 8081.');
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

