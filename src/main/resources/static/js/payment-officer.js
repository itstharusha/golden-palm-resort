// Global variables
let allPayments = [];
let currentPaymentId = null;

// Initialize the dashboard
document.addEventListener('DOMContentLoaded', function() {
    console.log('Payment Officer Dashboard initialized');
    // Auth guard: require login and proper role
    const token = localStorage.getItem('authToken');
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null');
    if (!token || !userInfo) {
        showAlert('Please login as Payment Officer to access this page.', 'error');
        setTimeout(() => window.location.href = '/login.html', 1200);
        return;
    }
    if (!(userInfo.role === 'PAYMENT_OFFICER' || userInfo.role === 'ADMIN')) {
        showAlert('You are not authorized to access the Payment Officer dashboard.', 'error');
        setTimeout(() => window.location.href = '/', 1200);
        return;
    }

    setupEventListeners();
    loadDashboardData();
});

function authHeaders() {
    const token = localStorage.getItem('authToken');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
}

// ... rest of the code remains the same ...

function loadStatistics() {
    console.log('Loading payment statistics...');
    fetch('/api/payment-officer/statistics', { headers: authHeaders() })
        .then(response => {
            console.log('Statistics response status:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('Statistics data:', data);
            updateStatisticsDisplay(data);
        })
        .catch(error => {
            console.error('Error loading statistics:', error);
            showAlert('Error loading statistics', 'error');
        });
}

function loadAllPayments() {
    console.log('Loading all payments...');
    fetch('/api/payment-officer/payments', { headers: authHeaders() })
        .then(response => {
            console.log('Payments response status:', response.status);
            return response.json();
        })
        .then(data => {
            console.log('Payments data:', data);
            allPayments = data;
            displayAllPayments(data);
            displayRecentPayments(data.slice(0, 5)); // Show first 5 as recent
            loadPendingPayments();
            loadRefunds();
        })
        .catch(error => {
            console.error('Error loading payments:', error);
            showAlert('Error loading payments', 'error');
        });
}

function loadPendingPayments() {
    console.log('Loading pending payments...');
    fetch('/api/payment-officer/payments/status/PENDING', { headers: authHeaders() })
        .then(response => response.json())
        .then(data => {
            console.log('Pending payments data:', data);
            displayPendingPayments(data);
        })
        .catch(error => {
            console.error('Error loading pending payments:', error);
        });
}

function loadRefunds() {
    console.log('Loading refunds...');
    fetch('/api/payment-officer/payments/status/REFUNDED', { headers: authHeaders() })
        .then(response => response.json())
        .then(data => {
            console.log('Refunds data:', data);
            displayRefunds(data);
        })
        .catch(error => {
            console.error('Error loading refunds:', error);
        });
}

function viewPaymentDetails(paymentId) {
    console.log('Viewing payment details for ID:', paymentId);
    fetch(`/api/payment-officer/payments/${paymentId}`, { headers: authHeaders() })
        .then(response => response.json())
        .then(payment => {
            console.log('Payment details:', payment);
            displayPaymentDetails(payment);
            const modal = new bootstrap.Modal(document.getElementById('paymentDetailsModal'));
            modal.show();
        })
        .catch(error => {
            console.error('Error loading payment details:', error);
            showAlert('Error loading payment details', 'error');
        });
}

function updatePaymentStatus(paymentId, newStatus) {
    console.log('Updating payment status:', paymentId, newStatus);
    
    const requestData = {
        status: newStatus,
        processedBy: 'payment',
        notes: `Status updated to ${newStatus}`
    };
    
    fetch(`/api/payment-officer/payments/${paymentId}/status`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders()
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        console.log('Status update response:', data);
        showAlert('Payment status updated successfully', 'success');
        loadAllPayments();
        loadStatistics();
    })
    .catch(error => {
        console.error('Error updating payment status:', error);
        showAlert('Error updating payment status', 'error');
    });
}

function processRefund() {
    const refundAmount = parseFloat(document.getElementById('refundAmount').value);
    const refundReason = document.getElementById('refundReason').value;
    const processedBy = document.getElementById('processedBy').value;
    
    if (!refundAmount || refundAmount <= 0) {
        showAlert('Please enter a valid refund amount', 'error');
        return;
    }
    
    if (!refundReason.trim()) {
        showAlert('Please provide a refund reason', 'error');
        return;
    }
    
    console.log('Processing refund for payment:', currentPaymentId);
    
    const requestData = {
        refundAmount: refundAmount,
        refundReason: refundReason,
        processedBy: processedBy
    };
    
    fetch(`/api/payment-officer/payments/${currentPaymentId}/refund`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders()
        },
        body: JSON.stringify(requestData)
    })
    .then(response => response.json())
    .then(data => {
        console.log('Refund response:', data);
        showAlert('Refund processed successfully', 'success');
        
        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('refundModal'));
        modal.hide();
        
        // Refresh data
        loadAllPayments();
        loadStatistics();
    })
    .catch(error => {
        console.error('Error processing refund:', error);
        showAlert('Error processing refund', 'error');
    });
}

function generateDailyReport() {
    const date = document.getElementById('dailyReportDate').value;
    console.log('Generating daily report for date:', date);
    
    fetch(`/api/payment-officer/reports/daily?date=${date}`, { headers: authHeaders() })
        .then(response => response.json())
        .then(data => {
            console.log('Daily report data:', data);
            displayDailyReport(data);
        })
        .catch(error => {
            console.error('Error generating daily report:', error);
            showAlert('Error generating daily report', 'error');
        });
}

function generateMonthlyReport() {
    const month = document.getElementById('monthlyReportDate').value;
    console.log('Generating monthly report for month:', month);
    
    fetch(`/api/payment-officer/reports/monthly?month=${month}`, { headers: authHeaders() })
        .then(response => response.json())
        .then(data => {
            console.log('Monthly report data:', data);
            displayMonthlyReport(data);
        })
        .catch(error => {
            console.error('Error generating monthly report:', error);
            showAlert('Error generating monthly report', 'error');
        });
}

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
    window.location.href = '/login.html';
}