// Global variables
let currentEventBookingId = null;
let currentRefundRequestId = null;

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('authToken');
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    
    if (!token || userInfo.role !== 'BACK_OFFICE_STAFF') {
        showAlert('Access denied. Back Office Staff privileges required.', 'danger');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
        return;
    }
    
    loadDashboardData();
    setupEventListeners();
});

// Setup event listeners
function setupEventListeners() {
    // Tab change listeners
    document.querySelectorAll('a[data-bs-toggle="tab"]').forEach(tab => {
        tab.addEventListener('shown.bs.tab', function(e) {
            const target = e.target.getAttribute('href');
            if (target === '#payment-reminders') {
                loadPendingPayments();
            } else if (target === '#refund-notifications') {
                loadApprovedRefunds();
            } else if (target === '#sent-notifications') {
                loadSentNotifications();
            }
        });
    });
}

// Load all dashboard data
async function loadDashboardData() {
    await Promise.all([
        loadStatistics(),
        loadRecentNotifications()
    ]);
}

// Load statistics
async function loadStatistics() {
    try {
        const response = await fetch('/api/back-office/statistics', {
            headers: authHeaders()
        });
        
        if (response.ok) {
            const stats = await response.json();
            document.getElementById('pendingPaymentsCount').textContent = stats.pendingPaymentReminders || 0;
            document.getElementById('approvedRefundsCount').textContent = stats.approvedRefunds || 0;
            document.getElementById('totalEventBookingsCount').textContent = stats.totalEventBookings || 0;
        }
    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

// Load recent notifications
async function loadRecentNotifications() {
    try {
        const response = await fetch('/api/back-office/notifications/sent', {
            headers: authHeaders()
        });
        
        if (response.ok) {
            const notifications = await response.json();
            displayRecentNotifications(notifications.slice(0, 5));
        }
    } catch (error) {
        console.error('Error loading recent notifications:', error);
    }
}

// Display recent notifications
function displayRecentNotifications(notifications) {
    const container = document.getElementById('recentNotifications');
    
    if (notifications.length === 0) {
        container.innerHTML = '<p class="text-muted">No notifications sent yet</p>';
        return;
    }
    
    container.innerHTML = notifications.map(notif => `
        <div class="notification-item">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <h6 class="mb-1">${notif.title}</h6>
                    <p class="mb-1 text-muted small">${notif.message.substring(0, 100)}${notif.message.length > 100 ? '...' : ''}</p>
                    <small class="text-muted">
                        <i class="fas fa-user me-1"></i>${notif.recipientName} 
                        <i class="fas fa-clock ms-2 me-1"></i>${formatDate(notif.createdAt)}
                    </small>
                </div>
                <span class="status-badge ${notif.isRead ? 'status-read' : 'status-unread'}">
                    ${notif.isRead ? 'Read' : 'Unread'}
                </span>
            </div>
        </div>
    `).join('');
}

// Load pending payments for reminders
async function loadPendingPayments() {
    try {
        const response = await fetch('/api/back-office/event-bookings/pending-payment', {
            headers: authHeaders()
        });
        
        if (response.ok) {
            const bookings = await response.json();
            displayPendingPayments(bookings);
        }
    } catch (error) {
        console.error('Error loading pending payments:', error);
        showAlert('Failed to load pending payments', 'danger');
    }
}

// Display pending payments
function displayPendingPayments(bookings) {
    const tbody = document.getElementById('paymentRemindersTable');
    
    if (bookings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No pending payments found</td></tr>';
        return;
    }
    
    tbody.innerHTML = bookings.map(booking => `
        <tr>
            <td><strong>${booking.bookingReference}</strong></td>
            <td>${booking.guestName}</td>
            <td>${booking.eventType}</td>
            <td>${formatDate(booking.eventDate)}</td>
            <td>${formatCurrency(booking.totalAmount)}</td>
            <td><span class="status-badge status-pending">${booking.status}</span></td>
            <td>
                <button class="btn btn-sm btn-send" onclick="showPaymentReminderModal(${booking.id}, '${booking.bookingReference}', '${booking.guestName}', ${booking.totalAmount})">
                    <i class="fas fa-bell me-1"></i>Send Reminder
                </button>
            </td>
        </tr>
    `).join('');
}

// Show payment reminder modal
function showPaymentReminderModal(eventBookingId, bookingRef, guestName, amount) {
    currentEventBookingId = eventBookingId;
    document.getElementById('reminderBookingRef').textContent = bookingRef;
    document.getElementById('reminderGuestName').textContent = guestName;
    document.getElementById('reminderAmount').textContent = formatCurrency(amount);
    document.getElementById('reminderMessage').value = '';
    
    const modal = new bootstrap.Modal(document.getElementById('paymentReminderModal'));
    modal.show();
}

// Send payment reminder
async function sendPaymentReminder() {
    const message = document.getElementById('reminderMessage').value.trim();
    
    try {
        const response = await fetch('/api/back-office/notifications/payment-reminder', {
            method: 'POST',
            headers: {
                ...authHeaders(),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                eventBookingId: currentEventBookingId,
                message: message || null
            })
        });
        
        if (response.ok) {
            showAlert('Payment reminder sent successfully!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('paymentReminderModal')).hide();
            loadDashboardData();
            loadPendingPayments();
        } else {
            const error = await response.json();
            showAlert(error.error || 'Failed to send payment reminder', 'danger');
        }
    } catch (error) {
        console.error('Error sending payment reminder:', error);
        showAlert('Failed to send payment reminder', 'danger');
    }
}

// Load approved refunds
async function loadApprovedRefunds() {
    try {
        const response = await fetch('/api/back-office/refunds/approved', {
            headers: authHeaders()
        });
        
        if (response.ok) {
            const refunds = await response.json();
            displayApprovedRefunds(refunds);
        }
    } catch (error) {
        console.error('Error loading approved refunds:', error);
        showAlert('Failed to load approved refunds', 'danger');
    }
}

// Display approved refunds
function displayApprovedRefunds(refunds) {
    const tbody = document.getElementById('refundNotificationsTable');
    
    if (refunds.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">No approved refunds found</td></tr>';
        return;
    }
    
    tbody.innerHTML = refunds.map(refund => `
        <tr>
            <td><strong>${refund.bookingReference}</strong></td>
            <td>${refund.guestName}</td>
            <td>${formatCurrency(refund.refundAmount)}</td>
            <td>${refund.bankName}</td>
            <td>${formatDate(refund.processedAt)}</td>
            <td>
                <button class="btn btn-sm btn-send" onclick="showRefundNotificationModal(${refund.id}, '${refund.bookingReference}', '${refund.guestName}', ${refund.refundAmount}, '${refund.bankName}')">
                    <i class="fas fa-bell me-1"></i>Send Notification
                </button>
            </td>
        </tr>
    `).join('');
}

// Show refund notification modal
function showRefundNotificationModal(refundId, bookingRef, guestName, amount, bankName) {
    currentRefundRequestId = refundId;
    document.getElementById('refundBookingRef').textContent = bookingRef;
    document.getElementById('refundGuestName').textContent = guestName;
    document.getElementById('refundAmount').textContent = formatCurrency(amount);
    document.getElementById('refundBank').textContent = bankName;
    document.getElementById('refundMessage').value = '';
    
    const modal = new bootstrap.Modal(document.getElementById('refundNotificationModal'));
    modal.show();
}

// Send refund notification
async function sendRefundNotification() {
    const message = document.getElementById('refundMessage').value.trim();
    
    try {
        const response = await fetch('/api/back-office/notifications/refund-approved', {
            method: 'POST',
            headers: {
                ...authHeaders(),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                refundRequestId: currentRefundRequestId,
                message: message || null
            })
        });
        
        if (response.ok) {
            showAlert('Refund approval notification sent successfully!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('refundNotificationModal')).hide();
            loadDashboardData();
            loadApprovedRefunds();
        } else {
            const error = await response.json();
            showAlert(error.error || 'Failed to send refund notification', 'danger');
        }
    } catch (error) {
        console.error('Error sending refund notification:', error);
        showAlert('Failed to send refund notification', 'danger');
    }
}

// Load sent notifications
async function loadSentNotifications() {
    try {
        const response = await fetch('/api/back-office/notifications/sent', {
            headers: authHeaders()
        });
        
        if (response.ok) {
            const notifications = await response.json();
            displaySentNotifications(notifications);
        }
    } catch (error) {
        console.error('Error loading sent notifications:', error);
        showAlert('Failed to load sent notifications', 'danger');
    }
}

// Display sent notifications
function displaySentNotifications(notifications) {
    const container = document.getElementById('sentNotificationsList');
    
    if (notifications.length === 0) {
        container.innerHTML = '<p class="text-muted">No notifications sent yet</p>';
        return;
    }
    
    container.innerHTML = notifications.map(notif => `
        <div class="notification-item">
            <div class="d-flex justify-content-between align-items-start">
                <div class="flex-grow-1">
                    <div class="d-flex align-items-center mb-2">
                        <span class="badge bg-primary me-2">${notif.type.replace('_', ' ')}</span>
                        <h6 class="mb-0">${notif.title}</h6>
                    </div>
                    <p class="mb-2 text-muted">${notif.message}</p>
                    <div class="d-flex justify-content-between align-items-center">
                        <small class="text-muted">
                            <i class="fas fa-user me-1"></i>${notif.recipientName} (${notif.recipientEmail})
                        </small>
                        <small class="text-muted">
                            <i class="fas fa-clock me-1"></i>${formatDate(notif.createdAt)}
                        </small>
                    </div>
                </div>
                <div class="ms-3">
                    <span class="status-badge ${notif.isRead ? 'status-read' : 'status-unread'}">
                        ${notif.isRead ? 'Read' : 'Unread'}
                    </span>
                    ${notif.readAt ? `<br><small class="text-muted">Read: ${formatDate(notif.readAt)}</small>` : ''}
                </div>
            </div>
        </div>
    `).join('');
}

// Utility functions
function authHeaders() {
    return {
        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        'Content-Type': 'application/json'
    };
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatCurrency(amount) {
    return `LKR ${parseFloat(amount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3`;
    alertDiv.style.zIndex = '9999';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.appendChild(alertDiv);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
    window.location.href = 'login.html';
}
