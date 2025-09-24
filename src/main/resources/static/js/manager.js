// Manager Dashboard JavaScript
let currentUser = null;
let authToken = localStorage.getItem('authToken');

// Initialize dashboard when page loads
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    loadDashboard();
    updateLastUpdatedTime();
    
    // Set up tab change handlers
    document.querySelectorAll('[data-bs-toggle="tab"]').forEach(tab => {
        tab.addEventListener('shown.bs.tab', function(e) {
            const target = e.target.getAttribute('href').substring(1);
            updatePageTitle(target);
            loadTabContent(target);
        });
    });
});

// Authentication check
function checkAuthentication() {
    if (!authToken) {
        window.location.href = 'login.html';
        return;
    }
    
    // Get user info from localStorage
    const userInfo = localStorage.getItem('userInfo');
    if (!userInfo) {
        window.location.href = 'login.html';
        return;
    }
    
    try {
        currentUser = JSON.parse(userInfo);
        if (currentUser.role !== 'MANAGER') {
            showAlert('Access denied. Manager role required.', 'danger');
            setTimeout(() => window.location.href = 'login.html', 2000);
            return;
        }
    } catch (error) {
        console.error('Error parsing user info:', error);
        window.location.href = 'login.html';
    }
}

// Load main dashboard data
function loadDashboard() {
    fetch('/api/manager/dashboard', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        updateDashboardStats(data);
    })
    .catch(error => {
        console.error('Error loading dashboard:', error);
        showAlert('Error loading dashboard data', 'danger');
    });
}

// Update dashboard statistics
function updateDashboardStats(data) {
    document.getElementById('occupancyRate').textContent = Math.round(data.occupancyRate) + '%';
    document.getElementById('todayRevenue').textContent = '$' + (data.todayRevenue || 0);
    document.getElementById('todayCheckIns').textContent = data.todayCheckIns || 0;
    document.getElementById('pendingBookings').textContent = data.pendingBookings || 0;
    
    document.getElementById('availableRooms').textContent = data.availableRooms || 0;
    document.getElementById('occupiedRooms').textContent = data.occupiedRooms || 0;
    document.getElementById('maintenanceRooms').textContent = data.maintenanceRooms || 0;
    document.getElementById('totalRooms').textContent = data.totalRooms || 0;
    
    document.getElementById('frontDeskStaff').textContent = data.frontDeskStaff || 0;
    document.getElementById('paymentOfficers').textContent = data.paymentOfficers || 0;
}

// Update page title based on active tab
function updatePageTitle(tabName) {
    const titles = {
        'dashboard': 'Operations Overview',
        'bookings': 'Booking Management',
        'staff': 'Staff Management',
        'rooms': 'Room Operations',
        'analytics': 'Analytics & Reports'
    };
    document.getElementById('pageTitle').textContent = titles[tabName] || 'Manager Dashboard';
}

// Load content for specific tabs
function loadTabContent(tabName) {
    switch(tabName) {
        case 'bookings':
            loadAllBookings();
            break;
        case 'staff':
            loadStaff();
            break;
        case 'rooms':
            loadRooms();
            break;
        case 'analytics':
            loadAnalytics();
            break;
    }
}

// ==================== BOOKING MANAGEMENT ====================

function loadAllBookings() {
    fetch('/api/manager/bookings', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(bookings => {
        displayBookings(bookings);
    })
    .catch(error => {
        console.error('Error loading bookings:', error);
        showAlert('Error loading bookings', 'danger');
    });
}

function loadPendingBookings() {
    fetch('/api/manager/bookings/pending', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(bookings => {
        displayBookings(bookings);
    })
    .catch(error => {
        console.error('Error loading pending bookings:', error);
        showAlert('Error loading pending bookings', 'danger');
    });
}

function displayBookings(bookings) {
    const tbody = document.getElementById('bookingsTableBody');
    tbody.innerHTML = '';
    
    bookings.forEach(booking => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${booking.bookingReference}</td>
            <td>${booking.guestName}</td>
            <td>${booking.roomNumber}</td>
            <td>${booking.checkInDate}</td>
            <td>${booking.checkOutDate}</td>
            <td>$${booking.totalAmount}</td>
            <td><span class="badge badge-${booking.status.toLowerCase()}">${booking.status}</span></td>
            <td>
                ${booking.status === 'PENDING' ? `
                    <button class="btn btn-sm btn-success me-1" onclick="approveBooking(${booking.id})">
                        <i class="fas fa-check"></i> Approve
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="cancelBooking(${booking.id})">
                        <i class="fas fa-times"></i> Cancel
                    </button>
                ` : `
                    <button class="btn btn-sm btn-outline-primary" onclick="viewBookingDetails(${booking.id})">
                        <i class="fas fa-eye"></i> View
                    </button>
                `}
            </td>
        `;
        tbody.appendChild(row);
    });
}

function approveBooking(bookingId) {
    if (!confirm('Are you sure you want to approve this booking?')) return;
    
    fetch(`/api/manager/bookings/${bookingId}/approve`, {
        method: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(result => {
        showAlert('Booking approved successfully', 'success');
        loadAllBookings();
        loadDashboard(); // Refresh dashboard stats
    })
    .catch(error => {
        console.error('Error approving booking:', error);
        showAlert('Error approving booking', 'danger');
    });
}

function cancelBooking(bookingId) {
    const reason = prompt('Please enter cancellation reason:');
    if (!reason) return;
    
    fetch(`/api/manager/bookings/${bookingId}/cancel`, {
        method: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ reason: reason })
    })
    .then(response => response.json())
    .then(result => {
        showAlert('Booking cancelled successfully', 'success');
        loadAllBookings();
        loadDashboard(); // Refresh dashboard stats
    })
    .catch(error => {
        console.error('Error cancelling booking:', error);
        showAlert('Error cancelling booking', 'danger');
    });
}

// ==================== STAFF MANAGEMENT ====================

function loadStaff() {
    fetch('/api/manager/staff', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(staff => {
        displayStaff(staff);
    })
    .catch(error => {
        console.error('Error loading staff:', error);
        showAlert('Error loading staff', 'danger');
    });
}

function displayStaff(staff) {
    const tbody = document.getElementById('staffTableBody');
    tbody.innerHTML = '';
    
    staff.forEach(member => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${member.firstName} ${member.lastName}</td>
            <td>${member.username}</td>
            <td>${member.email}</td>
            <td><span class="badge bg-primary">${member.role}</span></td>
            <td><span class="badge ${member.isActive ? 'bg-success' : 'bg-danger'}">${member.isActive ? 'Active' : 'Inactive'}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary me-1" onclick="editStaffMember(${member.id})">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-sm btn-outline-${member.isActive ? 'danger' : 'success'}" onclick="toggleStaffStatus(${member.id}, ${member.isActive})">
                    <i class="fas fa-${member.isActive ? 'ban' : 'check'}"></i> ${member.isActive ? 'Deactivate' : 'Activate'}
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function addStaffMember() {
    const form = document.getElementById('addStaffForm');
    const formData = new FormData(form);
    const staffData = Object.fromEntries(formData.entries());
    
    fetch('/api/manager/staff', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(staffData)
    })
    .then(response => response.json())
    .then(result => {
        if (result.message) {
            showAlert(result.message, 'danger');
        } else {
            showAlert('Staff member added successfully', 'success');
            form.reset();
            bootstrap.Modal.getInstance(document.getElementById('addStaffModal')).hide();
            loadStaff();
        }
    })
    .catch(error => {
        console.error('Error adding staff member:', error);
        showAlert('Error adding staff member', 'danger');
    });
}

function toggleStaffStatus(staffId, currentStatus) {
    const action = currentStatus ? 'deactivate' : 'activate';
    if (!confirm(`Are you sure you want to ${action} this staff member?`)) return;
    
    fetch(`/api/manager/staff/${staffId}`, {
        method: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ isActive: !currentStatus })
    })
    .then(response => response.json())
    .then(result => {
        showAlert(`Staff member ${action}d successfully`, 'success');
        loadStaff();
    })
    .catch(error => {
        console.error('Error updating staff status:', error);
        showAlert('Error updating staff status', 'danger');
    });
}

// ==================== ROOM OPERATIONS ====================

function loadRooms() {
    fetch('/api/manager/rooms', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(rooms => {
        displayRooms(rooms);
    })
    .catch(error => {
        console.error('Error loading rooms:', error);
        showAlert('Error loading rooms', 'danger');
    });
}

function displayRooms(rooms) {
    const tbody = document.getElementById('roomsTableBody');
    tbody.innerHTML = '';
    
    rooms.forEach(room => {
        const row = document.createElement('tr');
        const statusColor = {
            'AVAILABLE': 'success',
            'OCCUPIED': 'danger',
            'MAINTENANCE': 'warning',
            'OUT_OF_ORDER': 'dark'
        };
        
        row.innerHTML = `
            <td>${room.roomNumber}</td>
            <td>${room.roomType}</td>
            <td>${room.floorNumber}</td>
            <td>${room.capacity}</td>
            <td>$${room.basePrice}</td>
            <td><span class="badge bg-${statusColor[room.status]}">${room.status}</span></td>
            <td>
                <select class="form-select form-select-sm" onchange="updateRoomStatus(${room.id}, this.value)">
                    <option value="${room.status}" selected>${room.status}</option>
                    <option value="AVAILABLE">AVAILABLE</option>
                    <option value="OCCUPIED">OCCUPIED</option>
                    <option value="MAINTENANCE">MAINTENANCE</option>
                    <option value="OUT_OF_ORDER">OUT_OF_ORDER</option>
                </select>
            </td>
        `;
        tbody.appendChild(row);
    });
}

function updateRoomStatus(roomId, newStatus) {
    fetch(`/api/manager/rooms/${roomId}/status`, {
        method: 'PUT',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: newStatus })
    })
    .then(response => response.json())
    .then(result => {
        showAlert('Room status updated successfully', 'success');
        loadDashboard(); // Refresh dashboard stats
    })
    .catch(error => {
        console.error('Error updating room status:', error);
        showAlert('Error updating room status', 'danger');
    });
}

// ==================== ANALYTICS ====================

function loadAnalytics() {
    // Load revenue analytics
    fetch('/api/manager/analytics/revenue', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('analyticsWeeklyRevenue').textContent = '$' + (data.weeklyRevenue || 0);
        document.getElementById('analyticsMonthlyRevenue').textContent = '$' + (data.monthlyRevenue || 0);
    })
    .catch(error => console.error('Error loading revenue analytics:', error));
    
    // Load occupancy analytics
    fetch('/api/manager/analytics/occupancy', {
        method: 'GET',
        headers: {
            'Authorization': 'Bearer ' + authToken,
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('analyticsOccupancyRate').textContent = Math.round(data.currentOccupancyRate || 0) + '%';
        document.getElementById('analyticsAvgStay').textContent = data.averageStayDuration || 0;
    })
    .catch(error => console.error('Error loading occupancy analytics:', error));
}

// ==================== QUICK ACTIONS ====================

function showPendingBookings() {
    // Switch to bookings tab and load pending bookings
    const bookingsTab = document.querySelector('[href="#bookings"]');
    const tab = new bootstrap.Tab(bookingsTab);
    tab.show();
    setTimeout(() => loadPendingBookings(), 100);
}

function viewTodayCheckIns() {
    showAlert('Today\'s check-ins feature coming soon!', 'info');
}

function viewMaintenanceRooms() {
    // Switch to rooms tab
    const roomsTab = document.querySelector('[href="#rooms"]');
    const tab = new bootstrap.Tab(roomsTab);
    tab.show();
}

function generateReport() {
    showAlert('Report generation feature coming soon!', 'info');
}

// ==================== UTILITY FUNCTIONS ====================

function showAlert(message, type) {
    const alertContainer = document.getElementById('alertContainer');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    alertContainer.appendChild(alert);
    
    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        if (alert.parentNode) {
            alert.remove();
        }
    }, 5000);
}

function updateLastUpdatedTime() {
    const now = new Date();
    document.getElementById('lastUpdated').textContent = now.toLocaleTimeString();
    
    // Update every minute
    setInterval(() => {
        const now = new Date();
        document.getElementById('lastUpdated').textContent = now.toLocaleTimeString();
    }, 60000);
}

function logout() {
    localStorage.removeItem('authToken');
    window.location.href = 'login.html';
}
