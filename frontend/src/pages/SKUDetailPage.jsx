import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getSKUById } from '../services/SKUService';

function SKUDetailPage() {
    const { skuId } = useParams();
    const navigate = useNavigate();
    const [sku, setSku] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        loadSKU();
    }, [skuId]);

    const loadSKU = async () => {
        try {
            setLoading(true);
            setError('');
            const data = await getSKUById(skuId);
            setSku(data);
        } catch (err) {
            if (err.message && err.message.includes('Unauthorized')) {
                setError('Your session has expired. Please log in again.');
                localStorage.removeItem('token');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 2000);
            } else {
                setError('Failed to load SKU details. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 pt-20 ml-64">
                <div className="flex items-center justify-center h-screen">
                    <div className="text-center">
                        <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 dark:border-indigo-400"></div>
                        <p className="mt-4 text-gray-600 dark:text-gray-300">Loading SKU details...</p>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 pt-20 ml-64">
                <div className="p-8">
                    <div className="bg-red-50 dark:bg-red-900/20 border-l-4 border-red-500 dark:border-red-600 text-red-800 dark:text-red-300 px-5 py-4 rounded-r-lg">
                        <div className="flex items-center">
                            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                            <span className="font-medium">{error}</span>
                        </div>
                    </div>
                    <button
                        onClick={() => navigate('/')}
                        className="mt-4 px-4 py-2 bg-indigo-600 dark:bg-indigo-700 text-white rounded-lg hover:bg-indigo-700 dark:hover:bg-indigo-800 transition-colors"
                    >
                        Back to Dashboard
                    </button>
                </div>
            </div>
        );
    }

    if (!sku) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 pt-20 ml-64">
                <div className="p-8">
                    <div className="bg-yellow-50 dark:bg-yellow-900/20 border-l-4 border-yellow-500 dark:border-yellow-600 text-yellow-800 dark:text-yellow-300 px-5 py-4 rounded-r-lg">
                        <p className="font-medium">SKU not found</p>
                    </div>
                    <button
                        onClick={() => navigate('/')}
                        className="mt-4 px-4 py-2 bg-indigo-600 dark:bg-indigo-700 text-white rounded-lg hover:bg-indigo-700 dark:hover:bg-indigo-800 transition-colors"
                    >
                        Back to Dashboard
                    </button>
                </div>
            </div>
        );
    }

    const profitMargin = sku.sellingPrice && sku.sellingPrice > 0
        ? ((parseFloat(sku.netProfit || 0) / parseFloat(sku.sellingPrice)) * 100).toFixed(2)
        : '0.00';

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 pt-20 ml-64">
            <div className="p-8">
                <div className="mb-6 flex items-center justify-between">
                    <button
                        onClick={() => navigate('/')}
                        className="flex items-center gap-2 px-4 py-2 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 rounded-lg border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                    >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                        </svg>
                        Back to Dashboard
                    </button>
                </div>

                <div className="bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 p-8">
                    <div className="mb-8">
                        <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">{sku.productName || 'Unnamed Product'}</h1>
                        <div className="flex items-center gap-4 text-sm text-gray-600 dark:text-gray-400">
                            <span className="font-semibold">SKU:</span>
                            <span className="font-mono bg-gray-100 dark:bg-gray-700 px-3 py-1 rounded">{sku.sku}</span>
                            <span className="font-semibold">Category:</span>
                            <span>{sku.category || 'Uncategorized'}</span>
                        </div>
                    </div>

                    {sku.description && (
                        <div className="mb-8">
                            <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Description</h2>
                            <p className="text-gray-700 dark:text-gray-300">{sku.description}</p>
                        </div>
                    )}

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
                        <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-6">
                            <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Product Dimensions</h2>
                            <div className="space-y-3">
                                <div className="flex justify-between">
                                    <span className="text-gray-600 dark:text-gray-400">Length:</span>
                                    <span className="font-medium text-gray-900 dark:text-white">
                                        {sku.length ? `${sku.length}"` : 'Not provided'}
                                    </span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-600 dark:text-gray-400">Width:</span>
                                    <span className="font-medium text-gray-900 dark:text-white">
                                        {sku.width ? `${sku.width}"` : 'Not provided'}
                                    </span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-600 dark:text-gray-400">Height:</span>
                                    <span className="font-medium text-gray-900 dark:text-white">
                                        {sku.height ? `${sku.height}"` : 'Not provided'}
                                    </span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-600 dark:text-gray-400">Weight:</span>
                                    <span className="font-medium text-gray-900 dark:text-white">
                                        {sku.weight ? `${sku.weight} lbs` : 'Not provided'}
                                    </span>
                                </div>
                                <div className="flex justify-between border-t border-gray-200 dark:border-gray-600 pt-3 mt-3">
                                    <span className="text-gray-600 dark:text-gray-400 font-semibold">Size Classification:</span>
                                    <span className="font-bold text-indigo-600 dark:text-indigo-400">
                                        {sku.sizeClassification || 'Not calculated'}
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-6">
                            <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Pricing Information</h2>
                            <div className="space-y-3">
                                <div className="flex justify-between">
                                    <span className="text-gray-600 dark:text-gray-400">Selling Price:</span>
                                    <span className="font-medium text-gray-900 dark:text-white">
                                        ${parseFloat(sku.sellingPrice || 0).toFixed(2)}
                                    </span>
                                </div>
                                <div className="flex justify-between border-t border-gray-200 dark:border-gray-600 pt-3 mt-3">
                                    <span className="text-gray-600 dark:text-gray-400">Net Profit:</span>
                                    <span className={`font-bold ${
                                        parseFloat(sku.netProfit || 0) >= 0 
                                            ? 'text-green-600 dark:text-green-400' 
                                            : 'text-red-600 dark:text-red-400'
                                    }`}>
                                        ${parseFloat(sku.netProfit || 0).toFixed(2)}
                                    </span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-600 dark:text-gray-400">Profit Margin:</span>
                                    <span className={`font-bold ${
                                        parseFloat(profitMargin) >= 0 
                                            ? 'text-green-600 dark:text-green-400' 
                                            : 'text-red-600 dark:text-red-400'
                                    }`}>
                                        {profitMargin}%
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="bg-gradient-to-r from-blue-50 to-indigo-50 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-lg p-6 mb-8">
                        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-6">Fee Breakdown</h2>
                        <div className="space-y-4">
                            <div className="flex justify-between items-center p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
                                <div>
                                    <div className="font-semibold text-gray-900 dark:text-white">FBA Fulfillment Fee</div>
                                    <div className="text-sm text-gray-600 dark:text-gray-400">Based on size tier and weight</div>
                                </div>
                                <div className="text-lg font-bold text-gray-900 dark:text-white">
                                    ${parseFloat(sku.fbaFulfillmentFee || 0).toFixed(2)}
                                </div>
                            </div>

                            <div className="flex justify-between items-center p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
                                <div>
                                    <div className="font-semibold text-gray-900 dark:text-white">Amazon Referral Fee (15%)</div>
                                    <div className="text-sm text-gray-600 dark:text-gray-400">15% of selling price (minimum $0.30)</div>
                                </div>
                                <div className="text-lg font-bold text-gray-900 dark:text-white">
                                    ${parseFloat(sku.referralFee || 0).toFixed(2)}
                                </div>
                            </div>

                            <div className="flex justify-between items-center p-4 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
                                <div>
                                    <div className="font-semibold text-gray-900 dark:text-white">Storage Fee</div>
                                    <div className="text-sm text-gray-600 dark:text-gray-400">Per cubic foot per month</div>
                                </div>
                                <div className="text-lg font-bold text-gray-900 dark:text-white">
                                    ${parseFloat(sku.storageFee || 0).toFixed(2)}
                                </div>
                            </div>

                            <div className="flex justify-between items-center p-4 bg-indigo-100 dark:bg-indigo-900/30 rounded-lg border-2 border-indigo-300 dark:border-indigo-700 mt-4">
                                <div>
                                    <div className="font-bold text-gray-900 dark:text-white">Total Fees</div>
                                    <div className="text-sm text-gray-600 dark:text-gray-400">Sum of all Amazon fees</div>
                                </div>
                                <div className="text-2xl font-bold text-indigo-700 dark:text-indigo-300">
                                    ${parseFloat(sku.totalFees || 0).toFixed(2)}
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="bg-green-50 dark:bg-green-900/20 rounded-lg p-6 border border-green-200 dark:border-green-800">
                            <div className="text-sm text-green-700 dark:text-green-400 font-semibold mb-2">Selling Price</div>
                            <div className="text-2xl font-bold text-green-800 dark:text-green-300">
                                ${parseFloat(sku.sellingPrice || 0).toFixed(2)}
                            </div>
                        </div>

                        <div className={`rounded-lg p-6 border ${
                            parseFloat(sku.totalFees || 0) > 0
                                ? 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800'
                                : 'bg-gray-50 dark:bg-gray-700/50 border-gray-200 dark:border-gray-700'
                        }`}>
                            <div className={`text-sm font-semibold mb-2 ${
                                parseFloat(sku.totalFees || 0) > 0
                                    ? 'text-red-700 dark:text-red-400'
                                    : 'text-gray-700 dark:text-gray-400'
                            }`}>
                                Total Fees
                            </div>
                            <div className={`text-2xl font-bold ${
                                parseFloat(sku.totalFees || 0) > 0
                                    ? 'text-red-800 dark:text-red-300'
                                    : 'text-gray-800 dark:text-gray-300'
                            }`}>
                                ${parseFloat(sku.totalFees || 0).toFixed(2)}
                            </div>
                        </div>

                        <div className={`rounded-lg p-6 border ${
                            parseFloat(sku.netProfit || 0) >= 0
                                ? 'bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800'
                                : 'bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800'
                        }`}>
                            <div className={`text-sm font-semibold mb-2 ${
                                parseFloat(sku.netProfit || 0) >= 0
                                    ? 'text-green-700 dark:text-green-400'
                                    : 'text-red-700 dark:text-red-400'
                            }`}>
                                Net Profit
                            </div>
                            <div className={`text-2xl font-bold ${
                                parseFloat(sku.netProfit || 0) >= 0
                                    ? 'text-green-800 dark:text-green-300'
                                    : 'text-red-800 dark:text-red-300'
                            }`}>
                                ${parseFloat(sku.netProfit || 0).toFixed(2)}
                            </div>
                        </div>
                    </div>

                    <div className="mt-8 pt-6 border-t border-gray-200 dark:border-gray-700">
                        <div className="text-sm text-gray-600 dark:text-gray-400">
                            <div className="flex justify-between mb-2">
                                <span>Created:</span>
                                <span>{sku.createdAt ? new Date(sku.createdAt).toLocaleString() : 'N/A'}</span>
                            </div>
                            <div className="flex justify-between">
                                <span>Last Updated:</span>
                                <span>{sku.updatedAt ? new Date(sku.updatedAt).toLocaleString() : 'N/A'}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default SKUDetailPage;

