import { useState } from 'react';
import { searchSKU } from '../services/SKUService';

function Calculator() {
    const [formData, setFormData] = useState({
        productName: '',
        description: '',
        length: '',
        width: '',
        height: '',
        weight: '',
        outboundShippingWeight: '',
        category: '',
        sellingPrice: '',
        cost: '',
        targetROI: '',
        timeInStorage: '1',
        freightCost: '',
        freightCostUnit: '1',
        otherCosts: '0',
        otherCostsType: 'fixed',
        fbaFeeCategory: 'Most goods',
        referralFeePercentage: '15',
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
            setSearchError('Please enter a SKU to search');
            return;
        }

        setSearching(true);
        setSearchError('');

        try {
            const foundSKU = await searchSKU(searchTerm.trim());
            if (foundSKU) {
                setFormData(prev => ({
                    ...prev,
                    productName: foundSKU.productName || '',
                    description: foundSKU.description || '',
                    length: foundSKU.length || '',
                    width: foundSKU.width || '',
                    height: foundSKU.height || '',
                    weight: foundSKU.weight || '',
                    outboundShippingWeight: foundSKU.weight || '',
                    category: foundSKU.category || '',
                    sellingPrice: foundSKU.sellingPrice || '',
                    cost: foundSKU.cost || '',
                    targetROI: foundSKU.targetROI || '',
                }));
                setSearchTerm('');
                setSearchError('');
            } else {
                setSearchError('Product not found. Please check the SKU.');
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
                outboundShippingWeight: formData.outboundShippingWeight ? parseFloat(formData.outboundShippingWeight) : null,
                category: formData.category || null,
                sellingPrice: parseFloat(formData.sellingPrice),
                cost: formData.cost ? parseFloat(formData.cost) : null,
                targetROI: formData.targetROI ? parseFloat(formData.targetROI) : null,
                timeInStorage: formData.timeInStorage ? parseFloat(formData.timeInStorage) : 1,
                freightCost: formData.freightCost ? parseFloat(formData.freightCost) : null,
                freightCostUnit: formData.freightCostUnit ? parseFloat(formData.freightCostUnit) : 1,
                otherCosts: formData.otherCosts ? parseFloat(formData.otherCosts) : 0,
                otherCostsType: formData.otherCostsType || 'fixed',
                fbaFeeCategory: formData.fbaFeeCategory || 'Most goods',
                referralFeePercentage: formData.referralFeePercentage ? parseFloat(formData.referralFeePercentage) : null,
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
            setResults(null);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 max-w-4xl mx-auto">
            <div className="bg-gradient-to-r from-gray-50 via-slate-50 to-gray-50 py-8 border-b border-gray-200">
                <div className="text-center">
                    <h2 className="text-3xl font-bold text-gray-900">
                        Profitability Calculator
                    </h2>
                    <div className="mt-2 w-24 h-1 bg-gray-900 mx-auto rounded-full"></div>
                </div>
            </div>

            <form onSubmit={handleCalculate} className="p-8">
                <div className="mb-8 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg p-5 border border-blue-200">
                    <label className="block text-sm font-semibold text-gray-700 mb-3">
                        Search by SKU
                    </label>
                    <div className="flex gap-3">
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            onKeyPress={handleSearchKeyPress}
                            placeholder="Enter SKU to auto-fill fields..."
                            className="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                        />
                        <button
                            type="button"
                            onClick={handleSearch}
                            disabled={searching}
                            className="inline-flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-6 py-2.5 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-sm"
                        >
                            {searching ? 'Searching...' : 'Search'}
                        </button>
                    </div>
                    {searchError && (
                        <p className="text-red-600 text-sm mt-2 font-medium">{searchError}</p>
                    )}
                </div>

                <div className="flex gap-6 mb-8 pb-6 border-b border-gray-200">
                    <div className="w-32 h-32 bg-gray-100 rounded-lg border border-gray-200 flex items-center justify-center flex-shrink-0">
                        <svg className="w-16 h-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                    </div>
                    <div className="flex-1">
                        <h3 className="text-lg font-semibold text-gray-900 mb-2">
                            {formData.productName || 'Product Name'}
                        </h3>
                        <p className="text-sm text-gray-600 leading-relaxed">
                            {formData.description || 'Product description will appear here when a SKU is selected.'}
                        </p>
                    </div>
                </div>

                <div className="space-y-4 mb-8">
                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700">Dimensions:</label>
                        <div className="flex items-center gap-2">
                            <div className="w-20 px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-700 text-sm">
                                {formData.length || '-'}
                            </div>
                            <span className="text-gray-500">x</span>
                            <div className="w-20 px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-700 text-sm">
                                {formData.width || '-'}
                            </div>
                            <span className="text-gray-500">x</span>
                            <div className="w-20 px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-700 text-sm">
                                {formData.height || '-'}
                            </div>
                            <span className="text-gray-500 text-sm">in.</span>
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700">Weight:</label>
                        <div className="flex items-center gap-2">
                            <div className="w-32 px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-700 text-sm">
                                {formData.weight || '-'}
                            </div>
                            <span className="text-gray-500 text-sm">lbs</span>
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700">Outbound Shipping Weight:</label>
                        <div className="flex items-center gap-2">
                            <div className="w-32 px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-700 text-sm">
                                {formData.outboundShippingWeight || '-'}
                            </div>
                            <span className="text-gray-500 text-sm">lbs</span>
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700">Size Tier:</label>
                        <div className="text-sm font-medium text-gray-900 bg-gray-50 px-4 py-2 rounded-lg border border-gray-200">
                            {results?.sizeTier || '-'}
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700">Price:</label>
                        <div className="flex items-center gap-2">
                            <span className="text-gray-500">$</span>
                            <input
                                type="number"
                                step="0.01"
                                name="sellingPrice"
                                value={formData.sellingPrice}
                                onChange={handleChange}
                                className="w-32 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                                placeholder="22.99"
                                required
                            />
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700">Est. Time in Storage:</label>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                step="0.1"
                                name="timeInStorage"
                                value={formData.timeInStorage}
                                onChange={handleChange}
                                className="w-32 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                                placeholder="1"
                            />
                            <span className="text-gray-500 text-sm">mths</span>
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <label className="text-sm text-gray-700">Unit Manufacturing Cost (estimated):</label>
                        <div className="flex items-center gap-2">
                            <span className="text-gray-500">$</span>
                            <input
                                type="number"
                                step="0.01"
                                name="cost"
                                value={formData.cost}
                                onChange={handleChange}
                                className="w-32 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white"
                                placeholder="5.4"
                            />
                        </div>
                    </div>

                </div>

                <div className="space-y-4 mb-8 border-t border-gray-200 pt-6">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700">Storage Fee (Jan.-Sep. / Oct.-Dec.)</label>
                            <svg className="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="text-sm font-medium text-gray-900">
                            {results ? `$ ${parseFloat(results.storageFeeJanSep || 0).toFixed(2)}/${parseFloat(results.storageFeeOctDec || 0).toFixed(2)}` : '$ 0.00/0.00'}
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700">FBA Fee</label>
                            <svg className="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="flex items-center gap-2">
                            <div className="text-sm font-medium text-gray-900">
                                {results ? `$ ${parseFloat(results.fbaFulfillmentFee || 0).toFixed(2)}` : '$ 0.00'}
                            </div>
                            <select
                                name="fbaFeeCategory"
                                value={formData.fbaFeeCategory}
                                onChange={handleChange}
                                className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white text-sm"
                            >
                                <option>Most goods</option>
                                <option>Apparel</option>
                                <option>Electronics</option>
                            </select>
                        </div>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700">Other Costs</label>
                            <svg className="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="flex items-center gap-2">
                            <input
                                type="number"
                                step="0.01"
                                name="otherCosts"
                                value={formData.otherCosts}
                                onChange={handleChange}
                                className="w-20 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white text-sm"
                                placeholder="0"
                            />
                            <select
                                name="otherCostsType"
                                value={formData.otherCostsType}
                                onChange={handleChange}
                                className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 bg-white text-sm"
                            >
                                <option value="fixed">$</option>
                                <option value="percentage">%</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div className="border-t border-gray-200 pt-6 space-y-4 mb-6">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700">Net (Jan.-Sep. / Oct.-Dec.)</label>
                            <svg className="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <p className={`text-sm font-medium ${results ? (parseFloat(results.netProfitJanSep || 0) >= 0 ? 'text-green-600' : 'text-red-600') : 'text-gray-500'}`}>
                            {results ? `$ ${parseFloat(results.netProfitJanSep || 0).toFixed(2)}/${parseFloat(results.netProfitOctDec || 0).toFixed(2)}` : '$ 0.00/0.00'}
                        </p>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700">Margin (Jan.-Sep. / Oct.-Dec.)</label>
                            <svg className="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <p className={`text-sm font-medium ${results ? (parseFloat(results.profitMarginJanSep || 0) >= 0 ? 'text-green-600' : 'text-red-600') : 'text-gray-500'}`}>
                            {results ? `${parseFloat(results.profitMarginJanSep || 0).toFixed(2)}/${parseFloat(results.profitMarginOctDec || 0).toFixed(2)} %` : '0.00/0.00 %'}
                        </p>
                    </div>

                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <label className="text-sm text-gray-700">ROI (Jan.-Sep. / Oct.-Dec.)</label>
                            <svg className="w-4 h-4 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <p className={`text-sm font-medium ${results ? (parseFloat(results.roiJanSep || 0) >= 0 ? 'text-green-600' : 'text-red-600') : 'text-gray-500'}`}>
                            {results ? `${parseFloat(results.roiJanSep || 0).toFixed(1)}/${parseFloat(results.roiOctDec || 0).toFixed(2)} %` : '0.0/0.00 %'}
                        </p>
                    </div>
                </div>

                <div className="mb-6">
                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full inline-flex items-center justify-center gap-2 bg-gradient-to-r from-indigo-600 to-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:from-indigo-700 hover:to-blue-700 transition-all duration-200 shadow-md hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
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

                {error && (
                    <div className="mb-6 bg-red-50 border-l-4 border-red-500 text-red-800 px-5 py-4 rounded-r-lg">
                        <div className="flex items-center">
                            <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                            <span className="font-medium">{error}</span>
                        </div>
                    </div>
                )}
            </form>
        </div>
    );
}

export default Calculator;
