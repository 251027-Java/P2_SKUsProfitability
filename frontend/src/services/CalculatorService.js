const CALCULATOR_BASE_URL = '/calculation';

export const calculateFees = async (calculationData) => {
    const response = await fetch(CALCULATOR_BASE_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(calculationData),
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Calculation failed');
    }

    return await response.json();
};

