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
        return <div className="text-white text-center mt-20">Loading...</div>;
    }

    // Redirect to the UI path "/login", NOT the API path
    return authenticated ? children : <Navigate to="/login" replace />;
}

function App() {
    return (
        <Router>
            <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 transition-colors duration-200">
                <Routes>
                    {/* UI ROUTES (Clean URLs for the user) */}
                    <Route path="/login" element={<LoginPage />} />
                    
                    {/* Redirect root to login */}
                    <Route path="/" element={<Navigate to="/login" replace />} />

                    <Route 
                        path="/dashboard" 
                        element={
                            <ProtectedRoute>
                                <Navbar />
                                <HomePage />
                            </ProtectedRoute>
                        } 
                    />
                    
                    <Route 
                        path="/sku/:skuId" 
                        element={
                            <ProtectedRoute>
                                <Navbar />
                                <SKUDetailPage />
                            </ProtectedRoute>
                        } 
                    />

                    {/* Catch-all: Redirect unknown routes to login */}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;