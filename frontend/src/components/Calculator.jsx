import { useState } from 'react';
import { searchSKU } from '../services/SKUService';

function Calculator() {
    const [formData, setFormData] = useState({
        length: '',
        width: '',
        height: '',
        weight: '',
        category: '',
        sellingPrice: '',
        cost: '',
        targetROI: '',
    });

    const [results, setResults] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');
    const [searching, setSearching] = useState(false);
    const [searchError, setSearchError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSearch = async () => {
        if (!searchTerm.trim()) {
            setSearchError('Please enter a SKU or ASIN to search');
            return;
        }

        setSearching(true);
        setSearchError('');

        try {
            const foundSKU = await searchSKU(searchTerm.trim());
            if (foundSKU) {
                setFormData({
                    length: foundSKU.length || '',
                    width: foundSKU.width || '',
                    height: foundSKU.height || '',
                    weight: foundSKU.weight || '',
                    category: foundSKU.category || '',
                    sellingPrice: foundSKU.sellingPrice || '',
                    cost: foundSKU.cost || '',
                    targetROI: foundSKU.targetROI || '',
                });
                setSearchTerm('');
                setSearchError('');
            } else {
                setSearchError('Product not found. Please check the SKU or ASIN.');
            }
        } catch (err) {
            setSearchError('Failed to search. Please try again.');
        } finally {
            setSearching(false);
        }
    };

    const handleSearchKeyPress = (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleSearch();
        }
    };

    const handleCalculate = async (e) => {
        e.preventDefault();
        setError('');
        setResults(null);
        setLoading(true);

        try {
            const requestBody = {
                length: parseFloat(formData.length),
                width: parseFloat(formData.width),
                height: parseFloat(formData.height),
                weight: parseFloat(formData.weight),
                category: formData.category || null,
                sellingPrice: parseFloat(formData.sellingPrice),
                cost: formData.cost ? parseFloat(formData.cost) : null,
                targetROI: formData.targetROI ? parseFloat(formData.targetROI) : null,
            };

            const response = await fetch('/api/calculator/calculate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestBody),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Calculation failed');
            }

            const data = await response.json();
            setResults(data);
        } catch (err) {
            setError(err.message || 'Failed to calculate fees');
        } finally {
            setLoading(false);
        }
    };

    const handleReset = () => {
        setFormData({
            length: '',
            width: '',
            height: '',
            weight: '',
            category: '',
            sellingPrice: '',
            cost: '',
            targetROI: '',
        });
        setResults(null);
        setError('');
    };

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h2 className="text-2xl font-bold text-gray-900">Quick Calculator</h2>
                    <p className="text-gray-600 mt-1">Calculate fees and ROI without saving</p>
                </div>
                {results && (
                    <button
                        onClick={handleReset}
                        className="inline-flex items-center gap-2 text-sm text-gray-700 hover:text-gray-900 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200"
                    >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                        </svg>
                        Reset
                    </button>
                )}
            </div>

            <form onSubmit={handleCalculate} className="space-y-6">
                <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg p-5 border border-blue-200">
                    <label className="block text-sm font-semibold text-gray-700 mb-3">
                        Search by SKU or ASIN
                    </label>
                    <div className="flex gap-3">
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            onKeyPress={handleSearchKeyPress}
                            placeholder="Enter SKU or ASIN to auto-fill fields..."
                            className="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                        />
                        <button
                            type="button"
                            onClick={handleSearch}
                            disabled={searching}
                            className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-6 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                        >
                            {searching ? (
                                <>
                                    <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Searching...
                                </>
                            ) : (
                                <>
                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                                    </svg>
                                    Search
                                </>
                            )}
                        </button>
                    </div>
                    {searchError && (
                        <p className="text-red-600 text-sm mt-2 font-medium">{searchError}</p>
                    )}
                    <p className="text-xs text-gray-600 mt-2">
                        Search your saved products to auto-fill all fields
                    </p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Length (inches) <span className="text-red-500">*</span>
                        </label>
                        <input
                            type="number"
                            step="0.01"
                            name="length"
                            value={formData.length}
                            onChange={handleChange}
                            required
                            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                            placeholder="10.5"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Width (inches) <span className="text-red-500">*</span>
                        </label>
                        <input
                            type="number"
                            step="0.01"
                            name="width"
                            value={formData.width}
                            onChange={handleChange}
                            required
                            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                            placeholder="8.0"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Height (inches) <span className="text-red-500">*</span>
                        </label>
                        <input
                            type="number"
                            step="0.01"
                            name="height"
                            value={formData.height}
                            onChange={handleChange}
                            required
                            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                            placeholder="2.5"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Weight (pounds) <span className="text-red-500">*</span>
                        </label>
                        <input
                            type="number"
                            step="0.01"
                            name="weight"
                            value={formData.weight}
                            onChange={handleChange}
                            required
                            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                            placeholder="1.2"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Category
                        </label>
                        <input
                            type="text"
                            name="category"
                            value={formData.category}
                            onChange={handleChange}
                            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                            placeholder="Electronics"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Selling Price ($) <span className="text-red-500">*</span>
                        </label>
                        <input
                            type="number"
                            step="0.01"
                            name="sellingPrice"
                            value={formData.sellingPrice}
                            onChange={handleChange}
                            required
                            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                            placeholder="29.99"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Your Cost ($) <span className="text-gray-500 text-xs font-normal">(optional)</span>
                        </label>
                        <input
                            type="number"
                            step="0.01"
                            name="cost"
                            value={formData.cost}
                            onChange={handleChange}
                            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                            placeholder="15.00"
                        />
                        <p className="text-xs text-gray-500 mt-1.5">Shows actual ROI</p>
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Target ROI (%) <span className="text-gray-500 text-xs font-normal">(optional)</span>
                        </label>
                        <input
                            type="number"
                            step="0.01"
                            name="targetROI"
                            value={formData.targetROI}
                            onChange={handleChange}
                            className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                            placeholder="30.00"
                        />
                        <p className="text-xs text-gray-500 mt-1.5">Shows max purchase cost</p>
                    </div>
                </div>

                {error && (
                    <div className="bg-red-50 border-l-4 border-red-500 text-red-800 px-5 py-4 rounded-r-lg">
                        <div className="flex items-center">
                            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                            <span className="font-medium">{error}</span>
                        </div>
                    </div>
                )}

                <div className="flex gap-4 pt-2">
                    <button
                        type="submit"
                        disabled={loading}
                        className="flex-1 inline-flex items-center justify-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {loading ? (
                            <>
                                <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                                Calculating...
                            </>
                        ) : (
                            <>
                                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-5m-6 5h.01M19 10a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                                Calculate
                            </>
                        )}
                    </button>
                </div>
            </form>

            {results && (
                <div className="mt-10 border-t border-gray-200 pt-8">
                    <h3 className="text-xl font-bold text-gray-900 mb-6">Calculation Results</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                        <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
                            <p className="text-sm text-gray-600 mb-1">FBA Fulfillment Fee</p>
                            <p className="text-2xl font-bold text-blue-600">${parseFloat(results.fbaFulfillmentFee || 0).toFixed(2)}</p>
                        </div>
                        <div className="bg-purple-50 rounded-lg p-4 border border-purple-200">
                            <p className="text-sm text-gray-600 mb-1">Referral Fee (15%)</p>
                            <p className="text-2xl font-bold text-purple-600">${parseFloat(results.referralFee || 0).toFixed(2)}</p>
                        </div>
                        <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
                            <p className="text-sm text-gray-600 mb-1">Storage Fee</p>
                            <p className="text-2xl font-bold text-gray-600">${parseFloat(results.storageFee || 0).toFixed(2)}</p>
                        </div>
                        <div className="bg-indigo-50 rounded-lg p-4 border border-indigo-200">
                            <p className="text-sm text-gray-600 mb-1">Total Fees</p>
                            <p className="text-2xl font-bold text-indigo-600">${parseFloat(results.totalFees || 0).toFixed(2)}</p>
                        </div>
                        {results.cost && (
                            <>
                                <div className="bg-green-50 rounded-lg p-4 border border-green-200">
                                    <p className="text-sm text-gray-600 mb-1">Net Profit</p>
                                    <p className={`text-2xl font-bold ${parseFloat(results.netProfit || 0) >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                                        ${parseFloat(results.netProfit || 0).toFixed(2)}
                                    </p>
                                </div>
                                <div className="bg-orange-50 rounded-lg p-4 border border-orange-200">
                                    <p className="text-sm text-gray-600 mb-1">ROI</p>
                                    <p className={`text-2xl font-bold ${parseFloat(results.roi || 0) >= 0 ? 'text-orange-600' : 'text-red-600'}`}>
                                        {parseFloat(results.roi || 0).toFixed(1)}%
                                    </p>
                                </div>
                            </>
                        )}
                        {results.maxCost && (
                            <div className="bg-teal-50 rounded-lg p-4 border border-teal-200 md:col-span-2">
                                <p className="text-sm text-gray-600 mb-1">Maximum Purchase Cost (for {results.targetROI}% ROI)</p>
                                <p className="text-3xl font-bold text-teal-600">${parseFloat(results.maxCost).toFixed(2)}</p>
                                <p className="text-xs text-gray-500 mt-2">
                                    To achieve {results.targetROI}% ROI, you should buy this product for ${parseFloat(results.maxCost).toFixed(2)} or less.
                                </p>
                            </div>
                        )}
                        {!results.cost && !results.maxCost && (
                            <div className="bg-yellow-50 rounded-lg p-4 border border-yellow-200 md:col-span-2">
                                <p className="text-sm text-yellow-800">
                                    Enter your Cost to see ROI, or enter Target ROI to see maximum purchase cost.
                                </p>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

export default Calculator;

