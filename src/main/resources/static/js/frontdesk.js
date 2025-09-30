// Front Desk Dashboard JavaScript
let currentUser = null;

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    checkAuth();
    loadDashboardData();
    setupEventListeners();
});

// Authentication check
function checkAuth() {
    const token = localStorage.getItem('authToken');
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    
    console.log('Auth check - Token:', token ? 'Present' : 'Missing');
    console.log('Auth check - User info:', userInfo);
    console.log('Auth check - User role:', userInfo.role);
    
    if (!token || (userInfo.role !== 'FRONT_DESK' && userInfo.role !== 'ADMIN')) {
        console.log('Auth check failed - redirecting to login');
        showAlert('Access denied. Front Desk privileges required.', 'danger');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
        return;
    }
    
    currentUser = userInfo;
    document.getElementById('pageTitle').textContent = 'Dashboard';
    console.log('Auth check passed - user authenticated');
}

// Setup event listeners
function setupEventListeners() {
    // Tab navigation
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remove active class from all tabs
            document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
            document.querySelectorAll('.tab-pane').forEach(t => t.classList.remove('show', 'active'));
            
            // Add active class to clicked tab
            this.classList.add('active');
            const target = this.getAttribute('href').substring(1);
            document.getElementById(target).classList.add('show', 'active');
            
            // Update page title
            const title = this.textContent.trim();
            document.getElementById('pageTitle').textContent = title;
            
            // Load data for specific tabs
            if (target === 'checkins') {
                loadCheckIns();
            } else if (target === 'checkouts') {
                loadCheckOuts();
            } else if (target === 'pending') {
                loadPendingBookings();
            } else if (target === 'guests') {
                loadCurrentGuests();
            } else if (target === 'all-bookings') {
                loadAllBookings();
            }
        });
    });
}

// Load dashboard data
async function loadDashboardData() {
    try {
        // Load statistics
        await loadStatistics();
        
        // Load today's arrivals
        await loadTodayArrivals();
        
    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showAlert('Error loading dashboard data', 'danger');
    }
}

// Load statistics
async function loadStatistics() {
    try {
        console.log('Loading statistics...');
        const response = await fetch('/api/frontdesk/statistics', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        console.log('Statistics response status:', response.status);
        
        if (response.ok) {
            const stats = await response.json();
            console.log('Statistics data:', stats);
            document.getElementById('totalCheckins').textContent = stats.todayCheckins || 0;
            document.getElementById('totalCheckouts').textContent = stats.todayCheckouts || 0;
            document.getElementById('pendingBookings').textContent = stats.pendingBookings || 0;
            document.getElementById('currentGuests').textContent = stats.currentGuests || 0;
        } else {
            console.error('Statistics API error:', response.status, response.statusText);
            document.getElementById('totalCheckins').textContent = 'N/A';
            document.getElementById('totalCheckouts').textContent = 'N/A';
            document.getElementById('pendingBookings').textContent = 'N/A';
            document.getElementById('currentGuests').textContent = 'N/A';
            showAlert('Unable to load front desk statistics. Please check your connection.', 'warning');
        }
    } catch (error) {
        console.error('Error loading statistics:', error);
        document.getElementById('totalCheckins').textContent = 'N/A';
        document.getElementById('totalCheckouts').textContent = 'N/A';
        document.getElementById('pendingBookings').textContent = 'N/A';
        document.getElementById('currentGuests').textContent = 'N/A';
        showAlert('Unable to load front desk statistics. Please check your connection.', 'warning');
    }
}

// Load today's arrivals
async function loadTodayArrivals() {
    try {
        const response = await fetch('/api/frontdesk/today-arrivals', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const arrivals = await response.json();
            displayTodayArrivals(arrivals);
        } else {
            console.error('Today arrivals API error:', response.status, response.statusText);
            displayTodayArrivals([]);
            showAlert('Unable to load today\'s arrivals. Please check your connection.', 'warning');
        }
    } catch (error) {
        console.error('Error loading today\'s arrivals:', error);
        // Display empty state instead of sample data
        displayTodayArrivals([]);
        showAlert('Unable to load today\'s arrivals. Please check your connection.', 'warning');
    }
}

// Display today's arrivals
function displayTodayArrivals(arrivals) {
    const tbody = document.getElementById('todayArrivalsTable');
    tbody.innerHTML = '';
    
    if (arrivals.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center text-muted">
                    <i class="fas fa-info-circle me-2"></i>No arrivals scheduled for today
                </td>
            </tr>
        `;
        return;
    }
    
    arrivals.forEach(arrival => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${arrival.guestName}</td>
            <td>${arrival.roomNumber}</td>
            <td>${formatDateTime(arrival.checkInDate)}</td>
            <td><span class="badge bg-${getStatusColor(arrival.status)}">${arrival.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-success" onclick="checkInGuest('${arrival.bookingReference}')">
                    <i class="fas fa-sign-in-alt"></i> Check-in
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load check-ins
async function loadCheckIns() {
    try {
        const response = await fetch('/api/frontdesk/checkins', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const checkins = await response.json();
            displayCheckIns(checkins);
        }
    } catch (error) {
        console.error('Error loading check-ins:', error);
        showAlert('Error loading check-ins', 'danger');
    }
}

// Display check-ins with enhanced data
function displayCheckIns(checkins) {
    const tbody = document.getElementById('checkinsTable');
    tbody.innerHTML = '';
    
    if (checkins.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted">
                    <i class="fas fa-info-circle me-2"></i>No check-ins found
                </td>
            </tr>
        `;
        return;
    }
    
    checkins.forEach(checkin => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${checkin.bookingReference}</strong></td>
            <td>
                <div>
                    <strong>${checkin.guestName}</strong>
                    ${checkin.guestEmail ? `<br><small class="text-muted">${checkin.guestEmail}</small>` : ''}
                </div>
            </td>
            <td>
                <div>
                    <strong>${checkin.roomNumber}</strong>
                    ${checkin.roomType ? `<br><small class="text-muted">${checkin.roomType}</small>` : ''}
                </div>
            </td>
            <td>
                <div>
                    <strong>${formatDateTime(checkin.checkInDate)}</strong>
                    ${checkin.stayDuration ? `<br><small class="text-muted">${checkin.stayDuration} days</small>` : ''}
                </div>
            </td>
            <td>
                <span class="badge bg-${getStatusColor(checkin.status)}">${checkin.status}</span>
                ${checkin.guestCount ? `<br><small class="text-muted">${checkin.guestCount} guests</small>` : ''}
            </td>
            <td>
                <div class="btn-group btn-group-sm" role="group">
                    <button class="btn btn-outline-primary" onclick="viewCheckIn('${checkin.bookingReference}')" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-outline-success" onclick="checkOutGuest('${checkin.bookingReference}')" title="Check Out">
                        <i class="fas fa-sign-out-alt"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load check-outs
async function loadCheckOuts() {
    try {
        const response = await fetch('/api/frontdesk/checkouts', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const checkouts = await response.json();
            displayCheckOuts(checkouts);
        }
    } catch (error) {
        console.error('Error loading check-outs:', error);
        showAlert('Error loading check-outs', 'danger');
    }
}

// Display check-outs
function displayCheckOuts(checkouts) {
    const tbody = document.getElementById('checkoutsTable');
    tbody.innerHTML = '';
    
    checkouts.forEach(checkout => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${checkout.bookingReference}</td>
            <td>${checkout.guestName}</td>
            <td>${checkout.roomNumber}</td>
            <td>${formatDateTime(checkout.checkOutDate)}</td>
            <td><span class="badge bg-${getStatusColor(checkout.status)}">${checkout.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="viewCheckOut('${checkout.bookingReference}')">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load pending bookings
async function loadPendingBookings() {
    try {
        const response = await fetch('/api/frontdesk/pending-bookings', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const bookings = await response.json();
            displayPendingBookings(bookings);
        }
    } catch (error) {
        console.error('Error loading pending bookings:', error);
        showAlert('Error loading pending bookings', 'danger');
    }
}

// Display pending bookings
function displayPendingBookings(bookings) {
    const tbody = document.getElementById('pendingBookingsTable');
    tbody.innerHTML = '';
    
    bookings.forEach(booking => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${booking.bookingReference}</td>
            <td>${booking.guestName}</td>
            <td>${booking.roomEvent}</td>
            <td>${formatDateTime(booking.checkInDate)}</td>
            <td>$${booking.totalAmount}</td>
            <td><span class="badge bg-${getStatusColor(booking.status)}">${booking.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-success" onclick="confirmBooking('${booking.bookingReference}')">
                    <i class="fas fa-check"></i> Confirm
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="rejectBooking('${booking.bookingReference}')">
                    <i class="fas fa-times"></i> Reject
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load current guests
async function loadCurrentGuests() {
    try {
        const response = await fetch('/api/frontdesk/current-guests', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const guests = await response.json();
            displayCurrentGuests(guests);
        }
    } catch (error) {
        console.error('Error loading current guests:', error);
        showAlert('Error loading current guests', 'danger');
    }
}

// Display current guests with enhanced data
function displayCurrentGuests(guests) {
    const tbody = document.getElementById('currentGuestsTable');
    tbody.innerHTML = '';
    
    if (guests.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted">
                    <i class="fas fa-info-circle me-2"></i>No current guests found
                </td>
            </tr>
        `;
        return;
    }
    
    guests.forEach(guest => {
        // Calculate remaining days for visual indicator
        const remainingDays = guest.remainingDays || 0;
        let statusClass = 'success';
        let statusText = 'On Track';
        
        if (remainingDays <= 0) {
            statusClass = 'danger';
            statusText = 'Overdue';
        } else if (remainingDays <= 1) {
            statusClass = 'warning';
            statusText = 'Check-out Soon';
        }
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>
                <div>
                    <strong>${guest.guestName}</strong>
                    ${guest.guestEmail ? `<br><small class="text-muted">${guest.guestEmail}</small>` : ''}
                </div>
            </td>
            <td>
                <div>
                    <strong>${guest.roomNumber}</strong>
                    ${guest.roomType ? `<br><small class="text-muted">${guest.roomType}</small>` : ''}
                </div>
            </td>
            <td>
                <div>
                    <strong>${formatDateTime(guest.checkInDate)}</strong>
                    ${guest.daysElapsed ? `<br><small class="text-muted">${guest.daysElapsed} days ago</small>` : ''}
                </div>
            </td>
            <td>
                <div>
                    <strong>${formatDateTime(guest.checkOutDate)}</strong>
                    <span class="badge bg-${statusClass} ms-1">${statusText}</span>
                </div>
            </td>
            <td>
                <span class="badge bg-${getStatusColor(guest.status)}">${guest.status}</span>
                ${guest.guestCount ? `<br><small class="text-muted">${guest.guestCount} guests</small>` : ''}
            </td>
            <td>
                <div class="btn-group btn-group-sm" role="group">
                    <button class="btn btn-outline-primary" onclick="viewCheckIn('${guest.bookingReference}')" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-outline-success" onclick="checkOutGuest('${guest.bookingReference}')" title="Check Out">
                        <i class="fas fa-sign-out-alt"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Modal functions
function showCheckInModal() {
    const modal = new bootstrap.Modal(document.getElementById('checkInModal'));
    modal.show();
}

function showCheckOutModal() {
    const modal = new bootstrap.Modal(document.getElementById('checkOutModal'));
    modal.show();
}

function showPendingBookings() {
    // Switch to pending bookings tab
    document.querySelector('a[href="#pending"]').click();
}

// Process check-in
async function processCheckIn() {
    const form = document.getElementById('checkInForm');
    const formData = new FormData(form);
    const checkInData = Object.fromEntries(formData.entries());
    
    try {
        const response = await fetch('/api/frontdesk/checkin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(checkInData)
        });
        
        if (response.ok) {
            showAlert('Guest checked in successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('checkInModal')).hide();
            form.reset();
            loadDashboardData();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error checking in guest', 'danger');
        }
    } catch (error) {
        console.error('Error checking in guest:', error);
        showAlert('Error checking in guest', 'danger');
    }
}

// Process check-out
async function processCheckOut() {
    const form = document.getElementById('checkOutForm');
    const formData = new FormData(form);
    const checkOutData = Object.fromEntries(formData.entries());
    
    try {
        const response = await fetch('/api/frontdesk/checkout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(checkOutData)
        });
        
        if (response.ok) {
            showAlert('Guest checked out successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('checkOutModal')).hide();
            form.reset();
            loadDashboardData();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error checking out guest', 'danger');
        }
    } catch (error) {
        console.error('Error checking out guest:', error);
        showAlert('Error checking out guest', 'danger');
    }
}

// Confirm booking
async function confirmBooking(bookingReference) {
    try {
        const response = await fetch(`/api/frontdesk/confirm-booking/${bookingReference}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            showAlert('Booking confirmed successfully', 'success');
            loadPendingBookings();
            loadDashboardData();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error confirming booking', 'danger');
        }
    } catch (error) {
        console.error('Error confirming booking:', error);
        showAlert('Error confirming booking', 'danger');
    }
}

// Reject booking
async function rejectBooking(bookingReference) {
    if (confirm('Are you sure you want to reject this booking?')) {
        try {
            const response = await fetch(`/api/frontdesk/reject-booking/${bookingReference}`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });
            
            if (response.ok) {
                showAlert('Booking rejected successfully', 'success');
                loadPendingBookings();
                loadDashboardData();
            } else {
                const error = await response.json();
                showAlert(error.message || 'Error rejecting booking', 'danger');
            }
        } catch (error) {
            console.error('Error rejecting booking:', error);
            showAlert('Error rejecting booking', 'danger');
        }
    }
}

// Quick action functions
function checkInGuest(bookingReference) {
    // Pre-fill the check-in form
    document.querySelector('#checkInForm input[name="bookingReference"]').value = bookingReference;
    showCheckInModal();
}

// Enhanced check-out function with confirmation
async function checkOutGuest(bookingReference) {
    if (!confirm(`Are you sure you want to check out guest with booking reference: ${bookingReference}?`)) {
        return;
    }
    
    try {
        showAlert('Processing check-out...', 'info');
        
        const response = await fetch('/api/frontdesk/checkout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify({ bookingReference: bookingReference })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showAlert(result.message || 'Guest checked out successfully', 'success');
            // Refresh the current data
            if (document.querySelector('#checkins.active')) {
                loadCheckIns();
            } else if (document.querySelector('#guests.active')) {
                loadCurrentGuests();
            }
        } else {
            showAlert(result.message || 'Error processing check-out', 'danger');
        }
    } catch (error) {
        console.error('Error during check-out:', error);
        showAlert('Error processing check-out', 'danger');
    }
}

// Utility functions
function getStatusColor(status) {
    switch (status) {
        case 'CONFIRMED': return 'success';
        case 'PENDING': return 'warning';
        case 'CANCELLED': return 'danger';
        case 'CHECKED_IN': return 'info';
        case 'CHECKED_OUT': return 'secondary';
        default: return 'secondary';
    }
}

function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    return date.toLocaleString();
}

function showAlert(message, type) {
    const alertContainer = document.getElementById('alertContainer');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    alertContainer.appendChild(alert);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        alert.remove();
    }, 5000);
}

function refreshData() {
    loadDashboardData();
    showAlert('Data refreshed successfully', 'info');
}

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
    window.location.href = 'login.html';
}

// Placeholder functions for future implementation
function viewCheckIn(bookingReference) {
    showAlert(`View check-in ${bookingReference} functionality coming soon`, 'info');
}

function viewCheckOut(bookingReference) {
    showAlert(`View check-out ${bookingReference} functionality coming soon`, 'info');
}

// Edit booking from details modal
function editBooking() {
    const bookingRef = document.getElementById('detailBookingRef').textContent;
    bootstrap.Modal.getInstance(document.getElementById('bookingDetailsModal')).hide();
    editBookingFromList(bookingRef);
}

// Load all bookings
async function loadAllBookings() {
    try {
        console.log('Loading all bookings...');
        const token = localStorage.getItem('authToken');
        console.log('Auth token:', token ? 'Present' : 'Missing');
        
        const response = await fetch('/api/frontdesk/all-bookings', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        
        console.log('All bookings response status:', response.status);
        
        if (response.ok) {
            const bookings = await response.json();
            console.log('All bookings data:', bookings);
            displayAllBookings(bookings);
        } else {
            const errorText = await response.text();
            console.error('All bookings API error:', response.status, response.statusText, errorText);
            showAlert(`Error loading all bookings: ${response.status}`, 'danger');
        }
    } catch (error) {
        console.error('Error loading all bookings:', error);
        showAlert('Error loading all bookings', 'danger');
    }
}

// Display all bookings
function displayAllBookings(bookings) {
    const tbody = document.getElementById('allBookingsTable');
    tbody.innerHTML = '';
    
    bookings.forEach(booking => {
        const row = document.createElement('tr');
        const date = booking.type === 'ROOM' ? booking.checkInDate : booking.eventDate;
        row.innerHTML = `
            <td>${booking.bookingReference}</td>
            <td>${booking.guestName}</td>
            <td><span class="badge bg-${booking.type === 'ROOM' ? 'primary' : 'success'}">${booking.type}</span></td>
            <td>${booking.roomEvent}</td>
            <td>${date}</td>
            <td>${booking.guestCount}</td>
            <td>$${booking.totalAmount}</td>
            <td><span class="badge bg-${getStatusColor(booking.status)}">${booking.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-info" onclick="viewBookingDetails('${booking.bookingReference}')">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="btn btn-sm btn-outline-primary" onclick="editBookingFromList('${booking.bookingReference}')">
                    <i class="fas fa-edit"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// View booking details
async function viewBookingDetails(bookingReference) {
    try {
        const response = await fetch(`/api/frontdesk/booking/${bookingReference}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const booking = await response.json();
            populateBookingDetailsModal(booking);
            new bootstrap.Modal(document.getElementById('bookingDetailsModal')).show();
        } else {
            showAlert('Error loading booking details', 'danger');
        }
    } catch (error) {
        console.error('Error viewing booking details:', error);
        showAlert('Error viewing booking details', 'danger');
    }
}

// Populate booking details modal
function populateBookingDetailsModal(booking) {
    document.getElementById('detailGuestName').textContent = booking.guestName;
    document.getElementById('detailGuestEmail').textContent = booking.guestEmail;
    document.getElementById('detailGuestPhone').textContent = booking.guestPhone || 'N/A';
    document.getElementById('detailBookingRef').textContent = booking.bookingReference;
    document.getElementById('detailBookingType').textContent = booking.type;
    document.getElementById('detailBookingStatus').textContent = booking.status;
    document.getElementById('detailCreatedAt').textContent = new Date(booking.createdAt).toLocaleString();
    
    if (booking.type === 'ROOM') {
        document.getElementById('detailRoomEvent').textContent = `Room ${booking.roomNumber} (${booking.roomType})`;
        document.getElementById('detailDate').textContent = `${booking.checkInDate} to ${booking.checkOutDate}`;
    } else {
        document.getElementById('detailRoomEvent').textContent = booking.eventSpaceName;
        document.getElementById('detailDate').textContent = `${booking.eventDate} at ${booking.eventTime}`;
    }
    
    document.getElementById('detailGuestCount').textContent = booking.guestCount;
    document.getElementById('detailTotalAmount').textContent = `$${booking.totalAmount}`;
    document.getElementById('detailSpecialRequests').textContent = booking.specialRequests || 'No special requests';
}

// Edit booking from list
async function editBookingFromList(bookingReference) {
    try {
        const response = await fetch(`/api/frontdesk/booking/${bookingReference}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const booking = await response.json();
            populateEditBookingModal(booking);
            new bootstrap.Modal(document.getElementById('editBookingModal')).show();
        } else {
            showAlert('Error loading booking for editing', 'danger');
        }
    } catch (error) {
        console.error('Error editing booking:', error);
        showAlert('Error editing booking', 'danger');
    }
}

// Populate edit booking modal
function populateEditBookingModal(booking) {
    document.getElementById('editBookingRef').value = booking.bookingReference;
    document.getElementById('editGuestCount').value = booking.guestCount;
    document.getElementById('editStatus').value = booking.status;
    document.getElementById('editSpecialRequests').value = booking.specialRequests || '';
    
    if (booking.type === 'ROOM') {
        document.getElementById('editDate').value = booking.checkInDate;
        document.getElementById('editTime').style.display = 'none';
        document.querySelector('label[for="editTime"]').style.display = 'none';
    } else {
        document.getElementById('editDate').value = booking.eventDate;
        document.getElementById('editTime').value = booking.eventTime;
        document.getElementById('editTime').style.display = 'block';
        document.querySelector('label[for="editTime"]').style.display = 'block';
    }
}

// Save booking changes
async function saveBookingChanges() {
    try {
        const form = document.getElementById('editBookingForm');
        const formData = new FormData(form);
        const bookingData = Object.fromEntries(formData.entries());
        
        // Convert guest count to integer
        bookingData.guestCount = parseInt(bookingData.guestCount);
        
        const response = await fetch(`/api/frontdesk/booking/${bookingData.bookingReference}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(bookingData)
        });
        
        if (response.ok) {
            const result = await response.json();
            showAlert(result.message || 'Booking updated successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('editBookingModal')).hide();
            loadAllBookings(); // Refresh the list
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error updating booking', 'danger');
        }
    } catch (error) {
        console.error('Error saving booking changes:', error);
        showAlert('Error saving booking changes', 'danger');
    }
}

// Refresh all bookings
function refreshAllBookings() {
    loadAllBookings();
}

// Search bookings
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('bookingSearch');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const rows = document.querySelectorAll('#allBookingsTable tr');
            
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(searchTerm) ? '' : 'none';
            });
        });
    }
}); 