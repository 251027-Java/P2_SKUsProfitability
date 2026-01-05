import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SKUDetailPage from './pages/SKUDetailPage';
import { isAuthenticated } from './services/AuthService';

function ProtectedRoute({ children }) {
    const [authChecked, setAuthChecked] = useState(false);
    const [authenticated, setAuthenticated] = useState(false);

    useEffect(() => {
        setAuthenticated(isAuthenticated());
        setAuthChecked(true);
    }, []);

    if (!authChecked) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900">
                <div className="text-center">
                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 dark:border-indigo-400"></div>
                    <p className="mt-4 text-gray-600 dark:text-gray-300">Loading...</p>
                </div>
            </div>
        );
    }

    return authenticated ? children : <Navigate to="/api/auth/login" replace />;
}

function App() {
    return (
        <Router>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 transition-colors duration-200">
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/api/auth/login" element={<LoginPage />} />
                    <Route path="/api/auth/register" element={<LoginPage />} />
                    <Route 
                        path="/" 
                        element={<Navigate to="/api/auth/login" replace />}
                    />
                    <Route 
                        path="/dashboard" 
                        element={
                            <ProtectedRoute>
                                <>
                                    <Navbar />
                                    <HomePage />
                                </>
                            </ProtectedRoute>
                        } 
                    />
                    <Route 
                        path="/sku/:skuId" 
                        element={
                            <ProtectedRoute>
                                <>
                                    <Navbar />
                                    <SKUDetailPage />
                                </>
                            </ProtectedRoute>
                        } 
                    />
                </Routes>
            </div>
        </Router>
    );
}

export default App;
