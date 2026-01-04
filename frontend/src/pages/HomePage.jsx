import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllSKUs, createSKU, deleteSKU, importSKUsFromCSV, getAllLists, addSKUToList } from '../services/ProductService';
import SKUForm from '../components/SKUForm';
import Calculator from '../components/Calculator';
import Sidebar from '../components/Sidebar';
import ListsPage from './ListsPage';

function HomePage() {
    const navigate = useNavigate();
    const [skus, setSkus] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [activeSection, setActiveSection] = useState('dashboard');


    useEffect(() => {
        const handleDashboardNavigate = () => {
            setActiveSection('dashboard');
        };
        window.addEventListener('dashboard-navigate', handleDashboardNavigate);
        return () => window.removeEventListener('dashboard-navigate', handleDashboardNavigate);
    }, []);

    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [lists, setLists] = useState([]);
    const [showAddToListMenu, setShowAddToListMenu] = useState(null);

    useEffect(() => {
        loadSKUs();
        loadLists();
    }, []);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (showAddToListMenu && !event.target.closest('.relative')) {
                setShowAddToListMenu(null);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [showAddToListMenu]);

    const loadLists = async () => {
        try {
            const data = await getAllLists();
            setLists(data);
        } catch (err) {
        }
    };

    const handleAddToList = async (listId, skuId) => {
        try {
            setError('');
            await addSKUToList(listId, skuId);
            setSuccess('SKU added to list successfully!');
            setShowAddToListMenu(null);
            await loadLists();
        } catch (err) {
            setError(err.message || 'Failed to add SKU to list');
        }
    };

    const loadSKUs = async () => {
        try {
            setLoading(true);
            const token = localStorage.getItem('token');
            if (!token) {
                setError('Please log in to view your SKUs.');
                window.location.href = '/login';
                return;
            }
            const data = await getAllSKUs();
            setSkus(data);
            setError('');
        } catch (err) {
            if (err.message && err.message.includes('Unauthorized')) {
                setError('Your session has expired. Please log in again.');
                localStorage.removeItem('token');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 2000);
            } else {
                setError('Failed to load SKUs. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    const handleCreateSKU = async (skuData) => {
        try {
            setError('');
            setSuccess('');
            await createSKU(skuData);
            setSuccess('SKU created successfully!');
            setShowForm(false);
            await loadSKUs();
        } catch (err) {
            setError(err.message || 'Failed to create SKU');
        }
    };

    const handleDeleteSKU = async (skuId) => {
        if (!window.confirm('Are you sure you want to delete this SKU?')) {
            return;
        }
        try {
            setError('');
            await deleteSKU(skuId);
            setSuccess('SKU deleted successfully!');
            await loadSKUs();
        } catch (err) {
            setError(err.message || 'Failed to delete SKU');
        }
    };

    const handleCSVImport = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        try {
            setError('');
            setSuccess('');
            const result = await importSKUsFromCSV(file);
            setSuccess(`Successfully imported ${result.count} SKU(s)!`);
            await loadSKUs();
        } catch (err) {
            setError(err.message || 'Failed to import SKUs');
        } finally {
            e.target.value = '';
        }
    };

    const dashboardSKUs = skus;
    const totalSKUs = dashboardSKUs.length;
    
    const skusWithPrice = dashboardSKUs.filter(sku => sku.sellingPrice && parseFloat(sku.sellingPrice) > 0);
    const avgAmazonPrice = skusWithPrice.length > 0
        ? skusWithPrice.reduce((sum, sku) => sum + parseFloat(sku.sellingPrice), 0) / skusWithPrice.length
        : 0;
    
    const skusWithFees = dashboardSKUs.filter(sku => sku.totalFees != null);
    const avgTotalFees = skusWithFees.length > 0
        ? skusWithFees.reduce((sum, sku) => sum + parseFloat(sku.totalFees || 0), 0) / skusWithFees.length
        : 0;
    
    const uniqueCategories = [...new Set(dashboardSKUs.map(sku => sku.category).filter(cat => cat))].length;
    
    const negativeProfitCount = dashboardSKUs.filter(sku => {
        const profit = parseFloat(sku.netProfit || 0);
        return profit < 0;
    }).length;
    
    const categoryDistribution = dashboardSKUs.reduce((acc, sku) => {
        const category = sku.category || 'Uncategorized';
        acc[category] = (acc[category] || 0) + 1;
        return acc;
    }, {});
    
    const sizeDistribution = dashboardSKUs.reduce((acc, sku) => {
        const sizeTier = sku.sizeClassification || 'Unknown';
        acc[sizeTier] = (acc[sizeTier] || 0) + 1;
        return acc;
    }, {});

    const renderContent = () => {
        switch (activeSection) {
            case 'calculator':
                return (
                    <div>
                        <Calculator />
                    </div>
                );
            case 'lists':
                return <ListsPage />;
            case 'skus':
                return (
                    <>
                        <div className="mb-8">
                            <div className="flex justify-between items-start mb-6">
                                <div>
                                    <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">My SKUs</h1>
                                    <p className="text-gray-600 dark:text-gray-300 text-lg">Manage your product inventory</p>
                                </div>
                                <div className="flex gap-3">
                                    <label className="inline-flex items-center gap-2 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 px-5 py-2.5 rounded-lg font-medium hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-200 shadow-sm border border-gray-200 dark:border-gray-700 cursor-pointer">
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                                        </svg>
                                        <input
                                            type="file"
                                            accept=".csv"
                                            onChange={handleCSVImport}
                                            className="hidden"
                                        />
                                        Import CSV
                                    </label>
                                    <button
                                        onClick={() => setShowForm(!showForm)}
                                        className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-5 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg"
                                    >
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                        </svg>
                                        {showForm ? 'Cancel' : 'Add SKU'}
                                    </button>
                                </div>
                            </div>

                            {showForm && (
                                <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-8 mb-8 transition-colors duration-200">
                                    <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">Add New SKU</h2>
                                    <SKUForm
                                        onSave={handleCreateSKU}
                                        onCancel={() => setShowForm(false)}
                                    />
                                </div>
                            )}
                        </div>

                        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden transition-colors duration-200">
                            <div className="px-6 py-4 bg-gray-50 dark:bg-gray-700 border-b border-gray-200 dark:border-gray-600">
                                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Your SKUs</h2>
                            </div>
                            
                            {loading ? (
                                <div className="p-16 text-center">
                                    <div className="inline-block animate-spin rounded-full h-10 w-10 border-2 border-indigo-600 dark:border-indigo-400 border-t-transparent"></div>
                                    <p className="mt-4 text-gray-600 dark:text-gray-300 font-medium">Loading SKUs...</p>
                                </div>
                            ) : skus.length === 0 ? (
                                <div className="p-16 text-center">
                                    <div className="inline-flex items-center justify-center w-20 h-20 bg-gray-100 dark:bg-gray-700 rounded-full mb-4">
                                        <svg className="w-10 h-10 text-gray-400 dark:text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                        </svg>
                                    </div>
                                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">No SKUs yet</h3>
                                    <p className="text-gray-600 dark:text-gray-300 mb-6">Get started by adding your first product</p>
                                    <button
                                        onClick={() => setShowForm(true)}
                                        className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-5 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg"
                                    >
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                        </svg>
                                        Add Your First SKU
                                    </button>
                                </div>
                            ) : (
                                <div className="overflow-x-auto">
                                    <table className="w-full">
                                        <thead className="bg-gray-50 dark:bg-gray-700">
                                            <tr>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">SKU</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Product</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Price</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Fees</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Profit</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Margin</th>
                                                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 dark:text-gray-300 uppercase tracking-wider">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-100 dark:divide-gray-700">
                                            {skus.map((sku) => (
                                                <tr key={sku.skuId} className="hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-150">
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <div className="text-sm font-semibold text-gray-900 dark:text-white">{sku.sku}</div>
                                                    </td>
                                                    <td className="px-6 py-4">
                                                        <div className="text-sm font-medium text-gray-900 dark:text-white">{sku.productName || '-'}</div>
                                                        <div className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{sku.category || 'No category'}</div>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <span className="text-sm font-medium text-gray-900 dark:text-white">${parseFloat(sku.sellingPrice || 0).toFixed(2)}</span>
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <span className="text-sm text-gray-600 dark:text-gray-300">${parseFloat(sku.totalFees || 0).toFixed(2)}</span>
                                                    </td>
                                                    <td className={`px-6 py-4 whitespace-nowrap text-sm font-semibold ${
                                                        parseFloat(sku.netProfit || 0) >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'
                                                    }`}>
                                                        ${parseFloat(sku.netProfit || 0).toFixed(2)}
                                                    </td>
                                                    <td className={`px-6 py-4 whitespace-nowrap text-sm font-semibold ${
                                                        sku.sellingPrice && sku.sellingPrice > 0 && sku.netProfit >= 0 ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'
                                                    }`}>
                                                        {sku.sellingPrice && sku.sellingPrice > 0 
                                                            ? ((parseFloat(sku.netProfit || 0) / parseFloat(sku.sellingPrice)) * 100).toFixed(1) + '%'
                                                            : '0.0%'}
                                                    </td>
                                                    <td className="px-6 py-4 whitespace-nowrap">
                                                        <div className="flex items-center gap-4">
                                                            <button
                                                                onClick={() => navigate(`/sku/${sku.skuId}`)}
                                                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-indigo-700 dark:text-indigo-300 bg-indigo-50 dark:bg-indigo-900/30 border border-indigo-200 dark:border-indigo-700 rounded-md hover:bg-indigo-100 dark:hover:bg-indigo-900/50 transition-colors duration-150"
                                                            >
                                                                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                                                </svg>
                                                                View
                                                            </button>
                                                            <div className="relative">
                                                                <button
                                                                    onClick={() => setShowAddToListMenu(showAddToListMenu === sku.skuId ? null : sku.skuId)}
                                                                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-blue-700 dark:text-blue-300 bg-blue-50 dark:bg-blue-900/30 border border-blue-200 dark:border-blue-700 rounded-md hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-colors duration-150"
                                                                >
                                                                    <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                                                    </svg>
                                                                    Add to List
                                                                </button>
                                                                {showAddToListMenu === sku.skuId && (
                                                                    <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-10 overflow-hidden transition-colors duration-200">
                                                                        <div className="py-1">
                                                                            {lists.length === 0 ? (
                                                                                <div className="px-4 py-3 text-sm text-gray-500 dark:text-gray-400">
                                                                                    No lists yet. Create one in My Lists.
                                                                                </div>
                                                                            ) : (
                                                                                lists.map((list) => (
                                                                                    <button
                                                                                        key={list.listId}
                                                                                        onClick={() => handleAddToList(list.listId, sku.skuId)}
                                                                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors duration-150"
                                                                                    >
                                                                                        {list.name}
                                                                                    </button>
                                                                                ))
                                                                            )}
                                                                        </div>
                                                                    </div>
                                                                )}
                                                            </div>
                                                            <button
                                                                onClick={() => handleDeleteSKU(sku.skuId)}
                                                                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-red-700 dark:text-red-300 bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 rounded-md hover:bg-red-100 dark:hover:bg-red-900/50 transition-colors duration-150"
                                                            >
                                                                <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                                                </svg>
                                                                Delete
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </>
                );
            case 'dashboard':
            default:
                const skuIdsInLists = new Set();
                lists.forEach(list => {
                    if (list.items) {
                        list.items.forEach(item => skuIdsInLists.add(item.skuId));
                    }
                });
                
                const unlistedSKUs = skus.filter(sku => !skuIdsInLists.has(sku.skuId));
                
                const productsNeedingAttention = skus.filter(sku => {
                    const profit = parseFloat(sku.netProfit || 0);
                    return profit < 0;
                });
                
                const listsWithMetrics = lists.map(list => {
                    const listSKUs = list.items || [];
                    const listSKUsWithPrice = listSKUs.filter(sku => sku.sellingPrice && parseFloat(sku.sellingPrice) > 0);
                    const avgPrice = listSKUsWithPrice.length > 0
                        ? listSKUsWithPrice.reduce((sum, sku) => sum + parseFloat(sku.sellingPrice), 0) / listSKUsWithPrice.length
                        : 0;
                    const negativeCount = listSKUs.filter(sku => parseFloat(sku.netProfit || 0) < 0).length;
                    
                    return {
                        ...list,
                        avgPrice,
                        negativeCount,
                        itemCount: list.itemCount || listSKUs.length
                    };
                });
                
                return (
                    <>
                        <div className="mb-10">
                            <div className="flex justify-between items-start mb-6">
                                <div>
                                    <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">
                                        Dashboard
                                    </h1>
                                    <p className="text-gray-600 dark:text-gray-300 text-lg">
                                        Overview of your product portfolio and lists
                                    </p>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-4 gap-5 mb-8">
                                <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 hover:shadow-md transition-all duration-200">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="p-3 bg-indigo-100 rounded-lg">
                                            <svg className="w-6 h-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                                            </svg>
                                        </div>
                                    </div>
                                    <p className="text-sm font-medium text-gray-600 dark:text-gray-300 mb-1">Total Products</p>
                                    <p className="text-3xl font-bold text-gray-900 dark:text-white">{skus.length}</p>
                                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Imported SKUs</p>
                                </div>
                                <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 hover:shadow-md transition-all duration-200">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="p-3 bg-blue-100 dark:bg-blue-900/30 rounded-lg">
                                            <svg className="w-6 h-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                                            </svg>
                                        </div>
                                    </div>
                                    <p className="text-sm font-medium text-gray-600 dark:text-gray-300 mb-1">Active Lists</p>
                                    <p className="text-3xl font-bold text-gray-900 dark:text-white">{lists.length}</p>
                                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Organized lists</p>
                                </div>
                                <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 hover:shadow-md transition-all duration-200">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className={`p-3 rounded-lg ${productsNeedingAttention.length > 0 ? 'bg-red-100' : 'bg-green-100'}`}>
                                            <svg className={`w-6 h-6 ${productsNeedingAttention.length > 0 ? 'text-red-600' : 'text-green-600'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                                            </svg>
                                        </div>
                                    </div>
                                    <p className="text-sm font-medium text-gray-600 dark:text-gray-300 mb-1">Need Attention</p>
                                    <p className={`text-3xl font-bold ${productsNeedingAttention.length > 0 ? 'text-red-600 dark:text-red-400' : 'text-green-600 dark:text-green-400'}`}>
                                        {productsNeedingAttention.length}
                                    </p>
                                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Negative profit potential</p>
                                </div>
                                <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 hover:shadow-md transition-all duration-200">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className={`p-3 rounded-lg ${unlistedSKUs.length > 0 ? 'bg-amber-100' : 'bg-gray-100'}`}>
                                            <svg className={`w-6 h-6 ${unlistedSKUs.length > 0 ? 'text-amber-600' : 'text-gray-600'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                            </svg>
                                        </div>
                                    </div>
                                    <p className="text-sm font-medium text-gray-600 dark:text-gray-300 mb-1">Unlisted Products</p>
                                    <p className={`text-3xl font-bold ${unlistedSKUs.length > 0 ? 'text-amber-600 dark:text-amber-400' : 'text-gray-600 dark:text-gray-400'}`}>
                                        {unlistedSKUs.length}
                                    </p>
                                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Not in any list</p>
                                </div>
                            </div>

                            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 mb-8 transition-colors duration-200">
                                <div className="flex justify-between items-center mb-6">
                                    <h2 className="text-xl font-bold text-gray-900 dark:text-white">Your Lists Overview</h2>
                                    <button
                                        onClick={() => setActiveSection('lists')}
                                        className="text-sm text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 font-medium"
                                    >
                                        Manage Lists →
                                    </button>
                                </div>
                                {lists.length === 0 ? (
                                    <div className="text-center py-8">
                                        <p className="text-gray-500 dark:text-gray-400 mb-4">No lists created yet</p>
                                        <button
                                            onClick={() => setActiveSection('lists')}
                                            className="text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 font-medium"
                                        >
                                            Create your first list →
                                        </button>
                                    </div>
                                ) : (
                                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                        {listsWithMetrics.map((list) => (
                                            <div
                                                key={list.listId}
                                                onClick={() => setActiveSection('lists')}
                                                className="p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:border-indigo-300 dark:hover:border-indigo-600 hover:shadow-md transition-all duration-200 cursor-pointer bg-white dark:bg-gray-700"
                                            >
                                                <div className="flex justify-between items-start mb-2">
                                                    <h3 className="font-semibold text-gray-900 dark:text-white">{list.name}</h3>
                                                    <span className="text-xs bg-indigo-100 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-300 px-2 py-1 rounded">
                                                        {list.itemCount || 0} items
                                                    </span>
                                                </div>
                                                {list.description && (
                                                    <p className="text-sm text-gray-600 dark:text-gray-300 mb-3 line-clamp-2">{list.description}</p>
                                                )}
                                                <div className="space-y-1 text-xs">
                                                    <div className="flex justify-between">
                                                        <span className="text-gray-500 dark:text-gray-400">Avg. Price:</span>
                                                        <span className="font-medium text-gray-900 dark:text-white">
                                                            ${list.avgPrice > 0 ? list.avgPrice.toFixed(2) : '0.00'}
                                                        </span>
                                                    </div>
                                                    {list.negativeCount > 0 && (
                                                        <div className="flex justify-between">
                                                            <span className="text-gray-500 dark:text-gray-400">Need Attention:</span>
                                                            <span className="font-medium text-red-600 dark:text-red-400">{list.negativeCount}</span>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>

                            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                                <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 transition-colors duration-200">
                                    <div className="flex justify-between items-center mb-6">
                                        <h2 className="text-xl font-bold text-gray-900 dark:text-white">Unlisted Products</h2>
                                        <button
                                            onClick={() => setActiveSection('skus')}
                                            className="text-sm text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 font-medium"
                                        >
                                            View All →
                                        </button>
                                    </div>
                                    {unlistedSKUs.length === 0 ? (
                                        <div className="text-center py-8">
                                            <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 dark:bg-green-900/30 rounded-full mb-4">
                                                <svg className="w-8 h-8 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                                </svg>
                                            </div>
                                            <p className="text-gray-600 dark:text-gray-300 font-medium">All products are organized!</p>
                                            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">Every product is in at least one list</p>
                                        </div>
                                    ) : (
                                        <div className="space-y-3">
                                            {unlistedSKUs.slice(0, 5).map((sku) => (
                                                <div key={sku.skuId} className="flex items-center justify-between p-3 bg-amber-50 dark:bg-amber-900/20 rounded-lg border border-amber-200 dark:border-amber-800">
                                                    <div className="flex-1">
                                                        <p className="font-semibold text-gray-900 dark:text-white">{sku.sku}</p>
                                                        <p className="text-sm text-gray-600 dark:text-gray-300">{sku.productName || 'No name'}</p>
                                                    </div>
                                                    <button
                                                        onClick={() => setActiveSection('skus')}
                                                        className="text-xs text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 font-medium"
                                                    >
                                                        Add to List →
                                                    </button>
                                                </div>
                                            ))}
                                            {unlistedSKUs.length > 5 && (
                                                <p className="text-sm text-gray-500 dark:text-gray-400 text-center pt-2">
                                                    +{unlistedSKUs.length - 5} more unlisted products
                                                </p>
                                            )}
                                        </div>
                                    )}
                                </div>

                                <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 transition-colors duration-200">
                                    <div className="flex justify-between items-center mb-6">
                                        <h2 className="text-xl font-bold text-gray-900 dark:text-white">Products Needing Attention</h2>
                                        <button
                                            onClick={() => setActiveSection('skus')}
                                            className="text-sm text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 font-medium"
                                        >
                                            View All →
                                        </button>
                                    </div>
                                    {productsNeedingAttention.length === 0 ? (
                                        <div className="text-center py-8">
                                            <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 dark:bg-green-900/30 rounded-full mb-4">
                                                <svg className="w-8 h-8 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                                </svg>
                                            </div>
                                            <p className="text-gray-600 dark:text-gray-300 font-medium">All products look good!</p>
                                            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">No products with negative profit potential</p>
                                        </div>
                                    ) : (
                                        <div className="space-y-3">
                                            {productsNeedingAttention.slice(0, 5).map((sku) => (
                                                <div key={sku.skuId} className="flex items-center justify-between p-3 bg-red-50 dark:bg-red-900/20 rounded-lg border border-red-200 dark:border-red-800">
                                                    <div className="flex-1">
                                                        <p className="font-semibold text-gray-900 dark:text-white">{sku.sku}</p>
                                                        <p className="text-sm text-gray-600 dark:text-gray-300">{sku.productName || 'No name'}</p>
                                                        <p className="text-xs text-red-600 dark:text-red-400 mt-1">
                                                            Loss: ${Math.abs(parseFloat(sku.netProfit || 0)).toFixed(2)}
                                                        </p>
                                                    </div>
                                                    <button
                                                        onClick={() => setActiveSection('skus')}
                                                        className="text-xs text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 font-medium"
                                                    >
                                                        Review →
                                                    </button>
                                                </div>
                                            ))}
                                            {productsNeedingAttention.length > 5 && (
                                                <p className="text-sm text-gray-500 dark:text-gray-400 text-center pt-2">
                                                    +{productsNeedingAttention.length - 5} more products need attention
                                                </p>
                                            )}
                                        </div>
                                    )}
                                </div>
                            </div>

                            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 transition-colors duration-200">
                                <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-6">Quick Actions</h2>
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                                    <button
                                        onClick={() => setActiveSection('calculator')}
                                        className="flex items-center gap-4 p-4 bg-gradient-to-r from-indigo-50 to-blue-50 dark:from-indigo-900/20 dark:to-blue-900/20 rounded-lg border border-indigo-200 dark:border-indigo-800 hover:from-indigo-100 hover:to-blue-100 dark:hover:from-indigo-900/30 dark:hover:to-blue-900/30 transition-all duration-200 text-left"
                                    >
                                        <div className="p-3 bg-indigo-600 dark:bg-indigo-500 rounded-lg">
                                            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p className="font-semibold text-gray-900 dark:text-white">Calculator</p>
                                            <p className="text-sm text-gray-600 dark:text-gray-300">Calculate fees & ROI</p>
                                        </div>
                                    </button>
                                    <button
                                        onClick={() => setActiveSection('skus')}
                                        className="flex items-center gap-4 p-4 bg-gradient-to-r from-green-50 to-emerald-50 dark:from-green-900/20 dark:to-emerald-900/20 rounded-lg border border-green-200 dark:border-green-800 hover:from-green-100 hover:to-emerald-100 dark:hover:from-green-900/30 dark:hover:to-emerald-900/30 transition-all duration-200 text-left"
                                    >
                                        <div className="p-3 bg-green-600 dark:bg-green-500 rounded-lg">
                                            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p className="font-semibold text-gray-900 dark:text-white">Add SKU</p>
                                            <p className="text-sm text-gray-600 dark:text-gray-300">Add new product</p>
                                        </div>
                                    </button>
                                    <label className="flex items-center gap-4 p-4 bg-gradient-to-r from-purple-50 to-pink-50 dark:from-purple-900/20 dark:to-pink-900/20 rounded-lg border border-purple-200 dark:border-purple-800 hover:from-purple-100 hover:to-pink-100 dark:hover:from-purple-900/30 dark:hover:to-pink-900/30 transition-all duration-200 text-left cursor-pointer">
                                        <div className="p-3 bg-purple-600 dark:bg-purple-500 rounded-lg">
                                            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                                            </svg>
                                        </div>
                                        <div>
                                            <p className="font-semibold text-gray-900 dark:text-white">Import CSV</p>
                                            <p className="text-sm text-gray-600 dark:text-gray-300">Bulk import products</p>
                                        </div>
                                        <input
                                            type="file"
                                            accept=".csv"
                                            onChange={handleCSVImport}
                                            className="hidden"
                                        />
                                    </label>
                                </div>
                            </div>
                        </div>
                    </>
                );
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 dark:bg-gray-900 transition-colors duration-200">
            <Sidebar activeSection={activeSection} onSectionChange={setActiveSection} />
            <div className="ml-64 pt-20">
                <div className="max-w-7xl mx-auto px-8 py-10">
                    {error && (
                        <div className="mb-6 bg-red-50 dark:bg-red-900/20 border-l-4 border-red-500 dark:border-red-600 text-red-800 dark:text-red-300 px-5 py-4 rounded-r-lg shadow-sm">
                            <div className="flex items-center">
                                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                                </svg>
                                <span className="font-medium">{error}</span>
                            </div>
                        </div>
                    )}
                    {success && (
                        <div className="mb-6 bg-green-50 dark:bg-green-900/20 border-l-4 border-green-500 dark:border-green-600 text-green-800 dark:text-green-300 px-5 py-4 rounded-r-lg shadow-sm">
                            <div className="flex items-center">
                                <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                </svg>
                                <span className="font-medium">{success}</span>
                            </div>
                        </div>
                    )}

                    {renderContent()}
                </div>
            </div>
        </div>
    );
}

export default HomePage;
