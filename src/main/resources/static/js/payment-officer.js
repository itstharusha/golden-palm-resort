// Global variables
let allPayments = [];
let currentPaymentId = null;
let currentRefundRequestId = null;

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
    loadRefundRequests();
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

function loadRefundRequests() {
    console.log('Loading refund requests...');
    fetch('/api/refund-requests/pending', { headers: authHeaders() })
        .then(response => response.json())
        .then(data => {
            console.log('Refund requests data:', data);
            displayRefundRequests(data);
        })
        .catch(error => {
            console.error('Error loading refund requests:', error);
            showAlert('Error loading refund requests', 'error');
        });
}

function displayRefundRequests(requests) {
    const tbody = document.getElementById('refundRequestsTable');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!requests || requests.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted">No pending refund requests</td></tr>';
        return;
    }
    
    requests.forEach(request => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>#${request.id}</td>
            <td>${request.userName || 'N/A'}</td>
            <td>${request.bookingReference || 'N/A'}</td>
            <td><span class="badge ${request.bookingType === 'ROOM' ? 'bg-primary' : 'bg-info'}">${request.bookingType || 'N/A'}</span></td>
            <td>LKR ${(request.refundAmount || 0).toLocaleString()}</td>
            <td>
                <small>${request.bankName || 'N/A'}<br>
                Acc: ${request.bankAccountNumber || 'N/A'}</small>
            </td>
            <td><span class="status-badge status-pending">${request.status || 'PENDING'}</span></td>
            <td>${formatDate(request.createdAt)}</td>
            <td>
                <button class="btn btn-sm btn-info" onclick="viewRefundRequest(${request.id})">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function viewRefundRequest(requestId) {
    console.log('Viewing refund request:', requestId);
    currentRefundRequestId = requestId;
    
    fetch(`/api/refund-requests/${requestId}`, { headers: authHeaders() })
        .then(response => response.json())
        .then(request => {
            console.log('Refund request details:', request);
            displayRefundRequestDetails(request);
            const modal = new bootstrap.Modal(document.getElementById('refundRequestModal'));
            modal.show();
        })
        .catch(error => {
            console.error('Error loading refund request details:', error);
            showAlert('Error loading refund request details', 'error');
        });
}

function displayRefundRequestDetails(request) {
    const detailsDiv = document.getElementById('refundRequestDetails');
    if (!detailsDiv) return;
    
    let bookingDetails = '';
    if (request.bookingType === 'ROOM') {
        bookingDetails = `
            <p><strong>Room Number:</strong> ${request.roomNumber || 'N/A'}</p>
            <p><strong>Room Type:</strong> ${request.roomType || 'N/A'}</p>
            <p><strong>Check-in:</strong> ${request.checkInDate || 'N/A'}</p>
            <p><strong>Check-out:</strong> ${request.checkOutDate || 'N/A'}</p>
        `;
    } else if (request.bookingType === 'EVENT') {
        bookingDetails = `
            <p><strong>Event Space:</strong> ${request.eventSpaceName || 'N/A'}</p>
            <p><strong>Event Date:</strong> ${request.eventDate || 'N/A'}</p>
            <p><strong>Event Type:</strong> ${request.eventType || 'N/A'}</p>
        `;
    }
    
    detailsDiv.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <h6>Guest Information</h6>
                <p><strong>Name:</strong> ${request.userName || 'N/A'}</p>
                <p><strong>Email:</strong> ${request.userEmail || 'N/A'}</p>
            </div>
            <div class="col-md-6">
                <h6>Booking Information</h6>
                <p><strong>Reference:</strong> ${request.bookingReference || 'N/A'}</p>
                <p><strong>Type:</strong> <span class="badge ${request.bookingType === 'ROOM' ? 'bg-primary' : 'bg-info'}">${request.bookingType || 'N/A'}</span></p>
                ${bookingDetails}
            </div>
        </div>
        <hr>
        <div class="row">
            <div class="col-md-6">
                <h6>Refund Details</h6>
                <p><strong>Refund Amount:</strong> <span class="text-success">LKR ${(request.refundAmount || 0).toLocaleString()}</span></p>
                <p><strong>Reason:</strong> ${request.reason || 'No reason provided'}</p>
                <p><strong>Status:</strong> <span class="status-badge status-${request.status?.toLowerCase()}">${request.status || 'PENDING'}</span></p>
            </div>
            <div class="col-md-6">
                <h6>Bank Account Details</h6>
                <p><strong>Account Holder:</strong> ${request.accountHolderName || 'N/A'}</p>
                <p><strong>Account Number:</strong> ${request.bankAccountNumber || 'N/A'}</p>
                <p><strong>Bank Name:</strong> ${request.bankName || 'N/A'}</p>
                <p><strong>Branch:</strong> ${request.bankBranch || 'N/A'}</p>
            </div>
        </div>
        <hr>
        <p><strong>Request Date:</strong> ${formatDate(request.createdAt)}</p>
    `;
}

function approveRefundRequest() {
    if (!currentRefundRequestId) {
        showAlert('No refund request selected', 'error');
        return;
    }
    
    if (!confirm('Are you sure you want to approve this refund request? This will cancel the booking and mark it as refunded.')) {
        return;
    }
    
    console.log('Approving refund request:', currentRefundRequestId);
    
    fetch(`/api/refund-requests/${currentRefundRequestId}/approve`, {
        method: 'POST',
        headers: authHeaders()
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to approve refund request');
        }
        return response.json();
    })
    .then(data => {
        console.log('Refund request approved:', data);
        showAlert('Refund request approved successfully! Booking has been cancelled and marked as refunded.', 'success');
        
        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('refundRequestModal'));
        modal.hide();
        
        // Refresh data
        loadRefundRequests();
        loadAllPayments();
        loadRefunds();
    })
    .catch(error => {
        console.error('Error approving refund request:', error);
        showAlert('Error approving refund request', 'error');
    });
}

function rejectRefundRequest() {
    if (!currentRefundRequestId) {
        showAlert('No refund request selected', 'error');
        return;
    }
    
    const notes = prompt('Please provide a reason for rejecting this refund request:');
    if (!notes || notes.trim() === '') {
        showAlert('Rejection reason is required', 'warning');
        return;
    }
    
    console.log('Rejecting refund request:', currentRefundRequestId);
    
    fetch(`/api/refund-requests/${currentRefundRequestId}/reject`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders()
        },
        body: JSON.stringify({ notes: notes })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to reject refund request');
        }
        return response.json();
    })
    .then(data => {
        console.log('Refund request rejected:', data);
        showAlert('Refund request rejected', 'info');
        
        // Close modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('refundRequestModal'));
        modal.hide();
        
        // Refresh data
        loadRefundRequests();
    })
    .catch(error => {
        console.error('Error rejecting refund request:', error);
        showAlert('Error rejecting refund request', 'error');
    });
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
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

function setupEventListeners() {
    // Add any event listeners here
}

function loadDashboardData() {
    loadStatistics();
    loadAllPayments();
}

function showAlert(message, type) {
    // Simple alert for now - can be enhanced with Bootstrap toasts
    alert(message);
}

function updateStatisticsDisplay(data) {
    console.log('Updating statistics display:', data);
    
    // Update dashboard statistics cards
    const totalRevenue = data.totalRevenue || 0;
    const pendingPayments = data.pendingPayments || 0;
    const completedToday = data.completedPayments || 0;
    const totalRefunds = data.refundedPayments || 0;
    
    document.getElementById('totalRevenue').textContent = 'LKR ' + totalRevenue.toLocaleString();
    document.getElementById('pendingPayments').textContent = pendingPayments;
    document.getElementById('completedToday').textContent = completedToday;
    document.getElementById('totalRefunds').textContent = totalRefunds;
}

function displayAllPayments(payments) {
    console.log('Displaying all payments:', payments);
    const tbody = document.getElementById('allPaymentsTable');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!payments || payments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center text-muted">No payments found</td></tr>';
        return;
    }
    
    payments.forEach(payment => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>#${payment.id}</td>
            <td>${payment.guestName || 'N/A'}</td>
            <td>${payment.bookingReference || 'N/A'}</td>
            <td><span class="badge ${payment.paymentType === 'ROOM' ? 'bg-primary' : 'bg-info'}">${payment.paymentType || 'N/A'}</span></td>
            <td>LKR ${(payment.amount || 0).toLocaleString()}</td>
            <td><span class="payment-method-badge">${payment.paymentMethod || 'N/A'}</span></td>
            <td><span class="status-badge status-${payment.paymentStatus?.toLowerCase()}">${payment.paymentStatus || 'N/A'}</span></td>
            <td>${formatDate(payment.paymentDate)}</td>
            <td>
                <button class="btn btn-sm btn-info" onclick="viewPaymentDetails(${payment.id})">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function displayRecentPayments(payments) {
    console.log('Displaying recent payments:', payments);
    const tbody = document.getElementById('recentPaymentsTable');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!payments || payments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No recent payments</td></tr>';
        return;
    }
    
    payments.forEach(payment => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>#${payment.id}</td>
            <td>${payment.guestName || 'N/A'}</td>
            <td>${payment.bookingReference || 'N/A'}</td>
            <td>LKR ${(payment.amount || 0).toLocaleString()}</td>
            <td><span class="payment-method-badge">${payment.paymentMethod || 'N/A'}</span></td>
            <td><span class="status-badge status-${payment.paymentStatus?.toLowerCase()}">${payment.paymentStatus || 'N/A'}</span></td>
            <td>${formatDate(payment.paymentDate)}</td>
            <td>
                <button class="btn btn-sm btn-info" onclick="viewPaymentDetails(${payment.id})">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function displayPendingPayments(payments) {
    console.log('Displaying pending payments:', payments);
    const tbody = document.getElementById('pendingPaymentsTable');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!payments || payments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No pending payments</td></tr>';
        return;
    }
    
    payments.forEach(payment => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>#${payment.id}</td>
            <td>${payment.guestName || 'N/A'}</td>
            <td>${payment.bookingReference || 'N/A'}</td>
            <td>LKR ${(payment.amount || 0).toLocaleString()}</td>
            <td><span class="payment-method-badge">${payment.paymentMethod || 'N/A'}</span></td>
            <td>${payment.notes || 'N/A'}</td>
            <td>
                <button class="btn btn-sm btn-success" onclick="updatePaymentStatus(${payment.id}, 'COMPLETED')">
                    <i class="fas fa-check"></i> Complete
                </button>
                <button class="btn btn-sm btn-danger" onclick="updatePaymentStatus(${payment.id}, 'FAILED')">
                    <i class="fas fa-times"></i> Fail
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function displayRefunds(refunds) {
    console.log('Displaying refunds:', refunds);
    const tbody = document.getElementById('refundsTable');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (!refunds || refunds.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No refunds found</td></tr>';
        return;
    }
    
    refunds.forEach(payment => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>#${payment.id}</td>
            <td>${payment.guestName || 'N/A'}</td>
            <td>LKR ${(payment.amount || 0).toLocaleString()}</td>
            <td>LKR ${(payment.refundAmount || 0).toLocaleString()}</td>
            <td>${payment.refundReason || 'N/A'}</td>
            <td>${formatDate(payment.refundDate)}</td>
            <td><span class="status-badge status-refunded">${payment.paymentStatus || 'REFUNDED'}</span></td>
        `;
        tbody.appendChild(row);
    });
}

function displayPaymentDetails(payment) {
    console.log('Displaying payment details:', payment);
    const detailsDiv = document.getElementById('paymentDetailsContent');
    if (!detailsDiv) return;
    
    detailsDiv.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <h6>Payment Information</h6>
                <p><strong>Payment ID:</strong> #${payment.id}</p>
                <p><strong>Transaction ID:</strong> ${payment.transactionId || 'N/A'}</p>
                <p><strong>Amount:</strong> <span class="text-success">LKR ${(payment.amount || 0).toLocaleString()}</span></p>
                <p><strong>Payment Method:</strong> ${payment.paymentMethod || 'N/A'}</p>
                <p><strong>Status:</strong> <span class="status-badge status-${payment.paymentStatus?.toLowerCase()}">${payment.paymentStatus || 'N/A'}</span></p>
                <p><strong>Payment Date:</strong> ${formatDate(payment.paymentDate)}</p>
            </div>
            <div class="col-md-6">
                <h6>Booking Information</h6>
                <p><strong>Booking Reference:</strong> ${payment.bookingReference || 'N/A'}</p>
                <p><strong>Guest Name:</strong> ${payment.guestName || 'N/A'}</p>
                <p><strong>Type:</strong> <span class="badge ${payment.paymentType === 'ROOM' ? 'bg-primary' : 'bg-info'}">${payment.paymentType || 'N/A'}</span></p>
                <p><strong>Room/Event:</strong> ${payment.roomOrEventName || 'N/A'}</p>
            </div>
        </div>
        ${payment.refundAmount ? `
        <hr>
        <div class="row">
            <div class="col-12">
                <h6>Refund Information</h6>
                <p><strong>Refund Amount:</strong> <span class="text-danger">LKR ${(payment.refundAmount || 0).toLocaleString()}</span></p>
                <p><strong>Refund Reason:</strong> ${payment.refundReason || 'N/A'}</p>
                <p><strong>Refund Date:</strong> ${formatDate(payment.refundDate)}</p>
            </div>
        </div>
        ` : ''}
        <hr>
        <p><strong>Processed By:</strong> ${payment.processedBy || 'N/A'}</p>
        <p><strong>Notes:</strong> ${payment.notes || 'No notes'}</p>
    `;
}

function displayDailyReport(data) {
    console.log('Displaying daily report:', data);
    const contentDiv = document.getElementById('dailyReportContent');
    if (!contentDiv) return;
    
    contentDiv.innerHTML = `
        <div class="card">
            <div class="card-body">
                <h6>Daily Report - ${data.date}</h6>
                <hr>
                <div class="row">
                    <div class="col-md-6">
                        <p><strong>Total Payments:</strong> ${data.totalPayments || 0}</p>
                        <p><strong>Completed:</strong> ${data.completedPayments || 0}</p>
                        <p><strong>Pending:</strong> ${data.pendingPayments || 0}</p>
                    </div>
                    <div class="col-md-6">
                        <p><strong>Failed:</strong> ${data.failedPayments || 0}</p>
                        <p><strong>Refunded:</strong> ${data.refundedPayments || 0}</p>
                        <p><strong>Total Amount:</strong> <span class="text-success">LKR ${(data.totalAmount || 0).toLocaleString()}</span></p>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function displayMonthlyReport(data) {
    console.log('Displaying monthly report:', data);
    const contentDiv = document.getElementById('monthlyReportContent');
    if (!contentDiv) return;
    
    contentDiv.innerHTML = `
        <div class="card">
            <div class="card-body">
                <h6>Monthly Report - ${data.month}</h6>
                <hr>
                <div class="row">
                    <div class="col-md-6">
                        <p><strong>Total Payments:</strong> ${data.totalPayments || 0}</p>
                        <p><strong>Total Revenue:</strong> <span class="text-success">LKR ${(data.totalRevenue || 0).toLocaleString()}</span></p>
                    </div>
                    <div class="col-md-6">
                        <p><strong>Average Payment:</strong> LKR ${(data.averagePayment || 0).toLocaleString()}</p>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
    window.location.href = '/login.html';
}