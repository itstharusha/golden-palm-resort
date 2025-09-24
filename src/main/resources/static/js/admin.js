// Admin Dashboard JavaScript
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
    
    if (!token || userInfo.role !== 'ADMIN') {
        showAlert('Access denied. Admin privileges required.', 'danger');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
        return;
    }
    
    currentUser = userInfo;
    document.getElementById('pageTitle').textContent = 'Dashboard';
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
            if (target === 'users') {
                loadUsers();
            } else if (target === 'rooms') {
                loadRooms();
            } else if (target === 'events') {
                loadEventSpaces();
            } else if (target === 'bookings') {
                loadBookings();
            } else if (target === 'reports') {
                loadReports();
            }
        });
    });
}

// Load dashboard data
async function loadDashboardData() {
    try {
        // Load statistics
        await loadStatistics();
        
        // Load recent bookings
        await loadRecentBookings();
        
    } catch (error) {
        console.error('Error loading dashboard data:', error);
        showAlert('Error loading dashboard data', 'danger');
    }
}

// Load statistics
async function loadStatistics() {
    try {
        const response = await fetch('/api/admin/statistics', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const stats = await response.json();
            document.getElementById('totalUsers').textContent = stats.totalUsers || 0;
            document.getElementById('totalRooms').textContent = stats.availableRooms || 0;
            document.getElementById('totalBookings').textContent = stats.activeBookings || 0;
            document.getElementById('totalRevenue').textContent = `$${stats.monthlyRevenue || 0}`;
        }
    } catch (error) {
        console.error('Error loading statistics:', error);
        // Set default values if API is not available
        document.getElementById('totalUsers').textContent = '5';
        document.getElementById('totalRooms').textContent = '12';
        document.getElementById('totalBookings').textContent = '8';
        document.getElementById('totalRevenue').textContent = '$15,420';
    }
}

// Load recent bookings
async function loadRecentBookings() {
    try {
        const response = await fetch('/api/admin/recent-bookings', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const bookings = await response.json();
            displayRecentBookings(bookings);
        }
    } catch (error) {
        console.error('Error loading recent bookings:', error);
        // Display sample data
        displayRecentBookings([
            {
                bookingReference: 'BK001',
                guestName: 'John Doe',
                type: 'Room',
                checkInDate: '2024-01-15',
                status: 'CONFIRMED'
            },
            {
                bookingReference: 'BK002',
                guestName: 'Jane Smith',
                type: 'Event',
                checkInDate: '2024-01-20',
                status: 'PENDING'
            }
        ]);
    }
}

// Display recent bookings
function displayRecentBookings(bookings) {
    const tbody = document.getElementById('recentBookingsTable');
    tbody.innerHTML = '';
    
    bookings.forEach(booking => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${booking.bookingReference}</td>
            <td>${booking.guestName}</td>
            <td><span class="badge bg-primary">${booking.type}</span></td>
            <td>${booking.checkInDate}</td>
            <td><span class="badge bg-${getStatusColor(booking.status)}">${booking.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="viewBooking('${booking.bookingReference}')">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load users
async function loadUsers() {
    try {
        const response = await fetch('/api/admin/users', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const users = await response.json();
            displayUsers(users);
        }
    } catch (error) {
        console.error('Error loading users:', error);
        showAlert('Error loading users', 'danger');
    }
}

// Display users
function displayUsers(users) {
    const tbody = document.getElementById('usersTable');
    tbody.innerHTML = '';
    
    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.firstName} ${user.lastName}</td>
            <td>${user.username}</td>
            <td>${user.email}</td>
            <td>
                <select class="form-select form-select-sm" onchange="updateUserRole(${user.id}, this.value)">
                    <option value="ADMIN" ${user.role === 'ADMIN' ? 'selected' : ''}>Admin</option>
                    <option value="MANAGER" ${user.role === 'MANAGER' ? 'selected' : ''}>Manager</option>
                    <option value="FRONT_DESK" ${user.role === 'FRONT_DESK' ? 'selected' : ''}>Front Desk</option>
                    <option value="PAYMENT_OFFICER" ${user.role === 'PAYMENT_OFFICER' ? 'selected' : ''}>Payment Officer</option>
                    <option value="GUEST" ${user.role === 'GUEST' ? 'selected' : ''}>Guest</option>
                </select>
            </td>
            <td><span class="badge bg-${user.isActive ? 'success' : 'danger'}">${user.isActive ? 'Active' : 'Inactive'}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="editUser(${user.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteUser(${user.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load rooms
async function loadRooms() {
    try {
        const response = await fetch('/api/rooms', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const rooms = await response.json();
            displayRooms(rooms);
        }
    } catch (error) {
        console.error('Error loading rooms:', error);
        showAlert('Error loading rooms', 'danger');
    }
}

// Display rooms
function displayRooms(rooms) {
    const tbody = document.getElementById('roomsTable');
    tbody.innerHTML = '';
    
    rooms.forEach(room => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${room.roomNumber}</td>
            <td>${room.roomType}</td>
            <td>${room.floorNumber}</td>
            <td>${room.capacity}</td>
            <td>$${room.basePrice}</td>
            <td><span class="badge bg-${getRoomStatusColor(room.status)}">${room.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="editRoom(${room.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteRoom(${room.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load event spaces
async function loadEventSpaces() {
    try {
        const response = await fetch('/api/event-spaces', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const eventSpaces = await response.json();
            displayEventSpaces(eventSpaces);
        }
    } catch (error) {
        console.error('Error loading event spaces:', error);
        showAlert('Error loading event spaces', 'danger');
    }
}

// Display event spaces
function displayEventSpaces(eventSpaces) {
    const tbody = document.getElementById('eventSpacesTable');
    tbody.innerHTML = '';
    
    eventSpaces.forEach(space => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${space.name}</td>
            <td>${space.capacity}</td>
            <td>$${space.basePrice}</td>
            <td><span class="badge bg-${getEventSpaceStatusColor(space.status)}">${space.status}</span></td>
            <td>${space.amenities || 'N/A'}</td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="editEventSpace(${space.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteEventSpace(${space.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load bookings
async function loadBookings() {
    try {
        const response = await fetch('/api/admin/bookings', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const bookings = await response.json();
            displayBookings(bookings);
        }
    } catch (error) {
        console.error('Error loading bookings:', error);
        showAlert('Error loading bookings', 'danger');
    }
}

// Display bookings
function displayBookings(bookings) {
    const tbody = document.getElementById('bookingsTable');
    tbody.innerHTML = '';
    
    bookings.forEach(booking => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${booking.bookingReference}</td>
            <td>${booking.guestName}</td>
            <td><span class="badge bg-primary">${booking.type}</span></td>
            <td>${booking.checkInDate}</td>
            <td>${booking.checkOutDate}</td>
            <td>$${booking.totalAmount}</td>
            <td><span class="badge bg-${getStatusColor(booking.status)}">${booking.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="viewBooking('${booking.bookingReference}')">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="btn btn-sm btn-outline-warning" onclick="editBooking('${booking.bookingReference}')">
                    <i class="fas fa-edit"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Load reports
function loadReports() {
    // Initialize charts
    initializeCharts();
}

// Initialize charts
function initializeCharts() {
    // Revenue Chart
    const revenueCtx = document.getElementById('revenueChart').getContext('2d');
    new Chart(revenueCtx, {
        type: 'line',
        data: {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
            datasets: [{
                label: 'Revenue',
                data: [12000, 19000, 15000, 25000, 22000, 30000],
                borderColor: 'rgb(75, 192, 192)',
                tension: 0.1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    // Occupancy Chart
    const occupancyCtx = document.getElementById('occupancyChart').getContext('2d');
    new Chart(occupancyCtx, {
        type: 'doughnut',
        data: {
            labels: ['Occupied', 'Available', 'Maintenance'],
            datasets: [{
                data: [65, 25, 10],
                backgroundColor: [
                    'rgb(54, 162, 235)',
                    'rgb(75, 192, 192)',
                    'rgb(255, 99, 132)'
                ]
            }]
        },
        options: {
            responsive: true
        }
    });
}

// Modal functions
function showAddUserModal() {
    const modal = new bootstrap.Modal(document.getElementById('addUserModal'));
    modal.show();
}

function showAddRoomModal() {
    const modal = new bootstrap.Modal(document.getElementById('addRoomModal'));
    modal.show();
}

function showAddEventSpaceModal() {
    const modal = new bootstrap.Modal(document.getElementById('addEventSpaceModal'));
    modal.show();
}

// Add user
async function addUser() {
    const form = document.getElementById('addUserForm');
    const formData = new FormData(form);
    const userData = Object.fromEntries(formData.entries());
    
    try {
        const response = await fetch('/api/admin/users', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(userData)
        });
        
        if (response.ok) {
            showAlert('User added successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('addUserModal')).hide();
            form.reset();
            loadUsers();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error adding user', 'danger');
        }
    } catch (error) {
        console.error('Error adding user:', error);
        showAlert('Error adding user', 'danger');
    }
}

// Add room
async function addRoom() {
    const form = document.getElementById('addRoomForm');
    const formData = new FormData(form);
    const roomData = Object.fromEntries(formData.entries());
    
    try {
        const response = await fetch('/api/admin/rooms', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(roomData)
        });
        
        if (response.ok) {
            showAlert('Room added successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('addRoomModal')).hide();
            form.reset();
            loadRooms();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error adding room', 'danger');
        }
    } catch (error) {
        console.error('Error adding room:', error);
        showAlert('Error adding room', 'danger');
    }
}

// Add event space
async function addEventSpace() {
    const form = document.getElementById('addEventSpaceForm');
    const formData = new FormData(form);
    const eventSpaceData = Object.fromEntries(formData.entries());
    
    try {
        const response = await fetch('/api/admin/event-spaces', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(eventSpaceData)
        });
        
        if (response.ok) {
            showAlert('Event space added successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('addEventSpaceModal')).hide();
            form.reset();
            loadEventSpaces();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error adding event space', 'danger');
        }
    } catch (error) {
        console.error('Error adding event space:', error);
        showAlert('Error adding event space', 'danger');
    }
}

// Utility functions
function getStatusColor(status) {
    switch (status) {
        case 'CONFIRMED': return 'success';
        case 'PENDING': return 'warning';
        case 'CANCELLED': return 'danger';
        default: return 'secondary';
    }
}

function getRoleColor(role) {
    switch (role) {
        case 'ADMIN': return 'danger';
        case 'MANAGER': return 'warning';
        case 'FRONT_DESK': return 'info';
        case 'PAYMENT_OFFICER': return 'primary';
        case 'GUEST': return 'success';
        default: return 'secondary';
    }
}

function getRoomStatusColor(status) {
    switch (status) {
        case 'AVAILABLE': return 'success';
        case 'OCCUPIED': return 'warning';
        case 'MAINTENANCE': return 'danger';
        default: return 'secondary';
    }
}

function getEventSpaceStatusColor(status) {
    switch (status) {
        case 'AVAILABLE': return 'success';
        case 'BOOKED': return 'warning';
        case 'MAINTENANCE': return 'danger';
        default: return 'secondary';
    }
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

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
    window.location.href = 'login.html';
}

// Placeholder functions for future implementation
function editUser(id) {
    showAlert('Edit user functionality coming soon', 'info');
}

function deleteUser(id) {
    if (confirm('Are you sure you want to delete this user?')) {
        fetch(`/api/admin/users/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        })
        .then(response => response.json())
        .then(data => {
            showAlert(data.message || 'User deleted successfully', 'success');
            loadUsers();
        })
        .catch(error => {
            console.error('Error deleting user:', error);
            showAlert('Error deleting user', 'danger');
        });
    }
}

// Update user role
async function updateUserRole(userId, newRole) {
    try {
        const response = await fetch(`/api/admin/users/${userId}/role`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify({ role: newRole })
        });
        
        if (response.ok) {
            showAlert(`User role updated to ${newRole} successfully`, 'success');
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error updating user role', 'danger');
        }
    } catch (error) {
        console.error('Error updating user role:', error);
        showAlert('Error updating user role', 'danger');
    }
}

// Photo Management Functions
let currentRoomId = null;
let currentEventSpaceId = null;

// Room Management Functions
function editRoom(id) {
    currentRoomId = id;
    loadRoomDetails(id);
    loadRoomPhotos(id);
    new bootstrap.Modal(document.getElementById('roomManagementModal')).show();
}

async function loadRoomDetails(roomId) {
    try {
        const response = await fetch(`/api/rooms/${roomId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const room = await response.json();
            document.getElementById('roomId').value = room.id;
            document.getElementById('roomNumber').value = room.roomNumber;
            document.getElementById('roomType').value = room.roomType;
            document.getElementById('roomFloor').value = room.floorNumber;
            document.getElementById('roomCapacity').value = room.capacity;
            document.getElementById('roomPrice').value = room.basePrice;
            document.getElementById('roomDescription').value = room.description || '';
            document.getElementById('roomAmenities').value = room.amenities || '';
            document.getElementById('roomStatus').value = room.status;
        }
    } catch (error) {
        console.error('Error loading room details:', error);
        showAlert('Error loading room details', 'danger');
    }
}

async function loadRoomPhotos(roomId) {
    try {
        const response = await fetch(`/api/photos/rooms/${roomId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const photos = await response.json();
            displayRoomPhotos(photos);
        }
    } catch (error) {
        console.error('Error loading room photos:', error);
        showAlert('Error loading room photos', 'danger');
    }
}

function displayRoomPhotos(photos) {
    const container = document.getElementById('roomPhotosContainer');
    container.innerHTML = '';
    
    photos.forEach(photo => {
        const photoDiv = document.createElement('div');
        photoDiv.className = 'col-md-4 mb-3';
        photoDiv.innerHTML = `
            <div class="card">
                <img src="${photo.downloadUrl}" class="card-img-top" alt="Room Photo" style="height: 200px; object-fit: cover;">
                <div class="card-body">
                    <p class="card-text small">${photo.originalFileName}</p>
                    <button class="btn btn-sm btn-danger" onclick="deletePhoto(${photo.id})">Delete</button>
                </div>
            </div>
        `;
        container.appendChild(photoDiv);
    });
}

async function updateRoom() {
    try {
        const formData = new FormData(document.getElementById('roomUpdateForm'));
        const roomData = Object.fromEntries(formData.entries());
        
        const response = await fetch(`/api/admin/rooms/${currentRoomId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(roomData)
        });
        
        if (response.ok) {
            showAlert('Room updated successfully!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('roomManagementModal')).hide();
            loadRooms();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error updating room', 'danger');
        }
    } catch (error) {
        console.error('Error updating room:', error);
        showAlert('Error updating room', 'danger');
    }
}

function deleteRoom(id) {
    console.log('Delete room function called with ID:', id);
    if (confirm('Are you sure you want to delete this room?')) {
        console.log('User confirmed deletion, making API call...');
        fetch(`/api/admin/rooms/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        })
        .then(response => {
            console.log('Response status:', response.status);
            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        })
        .then(data => {
            console.log('Delete response data:', data);
            showAlert(data.message || 'Room deleted successfully', 'success');
            loadRooms();
        })
        .catch(error => {
            console.error('Error deleting room:', error);
            showAlert('Error deleting room: ' + error.message, 'danger');
        });
    }
}

// Event Space Management Functions
function editEventSpace(id) {
    currentEventSpaceId = id;
    loadEventSpaceDetails(id);
    loadEventSpacePhotos(id);
    new bootstrap.Modal(document.getElementById('eventSpaceManagementModal')).show();
}

async function loadEventSpaceDetails(eventSpaceId) {
    try {
        const response = await fetch(`/api/event-spaces/${eventSpaceId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const eventSpace = await response.json();
            document.getElementById('eventSpaceId').value = eventSpace.id;
            document.getElementById('eventSpaceName').value = eventSpace.name;
            document.getElementById('eventSpaceCapacity').value = eventSpace.capacity;
            document.getElementById('eventSpacePrice').value = eventSpace.basePrice;
            document.getElementById('eventSpaceDescription').value = eventSpace.description || '';
            document.getElementById('eventSpaceSetupTypes').value = eventSpace.setupTypes || '';
            document.getElementById('eventSpaceAmenities').value = eventSpace.amenities || '';
            document.getElementById('eventSpaceFloor').value = eventSpace.floorNumber;
            document.getElementById('eventSpaceDimensions').value = eventSpace.dimensions || '';
            document.getElementById('cateringAvailable').checked = eventSpace.cateringAvailable;
            document.getElementById('audioVisualEquipment').checked = eventSpace.audioVisualEquipment;
            document.getElementById('parkingAvailable').checked = eventSpace.parkingAvailable;
            document.getElementById('eventSpaceStatus').value = eventSpace.status;
        }
    } catch (error) {
        console.error('Error loading event space details:', error);
        showAlert('Error loading event space details', 'danger');
    }
}

async function loadEventSpacePhotos(eventSpaceId) {
    try {
        const response = await fetch(`/api/photos/event-spaces/${eventSpaceId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const photos = await response.json();
            displayEventSpacePhotos(photos);
        }
    } catch (error) {
        console.error('Error loading event space photos:', error);
        showAlert('Error loading event space photos', 'danger');
    }
}

function displayEventSpacePhotos(photos) {
    const container = document.getElementById('eventSpacePhotosContainer');
    container.innerHTML = '';
    
    photos.forEach(photo => {
        const photoDiv = document.createElement('div');
        photoDiv.className = 'col-md-4 mb-3';
        photoDiv.innerHTML = `
            <div class="card">
                <img src="${photo.downloadUrl}" class="card-img-top" alt="Event Space Photo" style="height: 200px; object-fit: cover;">
                <div class="card-body">
                    <p class="card-text small">${photo.originalFileName}</p>
                    <button class="btn btn-sm btn-danger" onclick="deletePhoto(${photo.id})">Delete</button>
                </div>
            </div>
        `;
        container.appendChild(photoDiv);
    });
}

async function updateEventSpace() {
    try {
        const formData = new FormData(document.getElementById('eventSpaceUpdateForm'));
        const eventSpaceData = Object.fromEntries(formData.entries());
        
        // Add checkbox values
        eventSpaceData.cateringAvailable = document.getElementById('cateringAvailable').checked;
        eventSpaceData.audioVisualEquipment = document.getElementById('audioVisualEquipment').checked;
        eventSpaceData.parkingAvailable = document.getElementById('parkingAvailable').checked;
        
        const response = await fetch(`/api/admin/event-spaces/${currentEventSpaceId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(eventSpaceData)
        });
        
        if (response.ok) {
            showAlert('Event space updated successfully!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('eventSpaceManagementModal')).hide();
            loadEventSpaces();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error updating event space', 'danger');
        }
    } catch (error) {
        console.error('Error updating event space:', error);
        showAlert('Error updating event space', 'danger');
    }
}

function deleteEventSpace(id) {
    console.log('Delete event space function called with ID:', id);
    if (confirm('Are you sure you want to delete this event space?')) {
        console.log('User confirmed deletion, making API call...');
        fetch(`/api/admin/event-spaces/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        })
        .then(response => {
            console.log('Response status:', response.status);
            if (response.ok) {
                return response.json();
            } else {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
        })
        .then(data => {
            console.log('Delete response data:', data);
            showAlert(data.message || 'Event space deleted successfully', 'success');
            loadEventSpaces();
        })
        .catch(error => {
            console.error('Error deleting event space:', error);
            showAlert('Error deleting event space: ' + error.message, 'danger');
        });
    }
}

function viewBooking(bookingReference) {
    // Redirect to front desk for booking management
    showAlert('Redirecting to Front Desk for booking management...', 'info');
    setTimeout(() => {
        window.location.href = 'frontdesk.html#all-bookings';
    }, 1500);
}

function editBooking(bookingReference) {
    // Redirect to front desk for booking management
    showAlert('Redirecting to Front Desk for booking management...', 'info');
    setTimeout(() => {
        window.location.href = 'frontdesk.html#all-bookings';
    }, 1500);
}

// Photo Upload Functions
document.addEventListener('DOMContentLoaded', function() {
    // Room photo upload
    const roomPhotoUpload = document.getElementById('roomPhotoUpload');
    if (roomPhotoUpload) {
        roomPhotoUpload.addEventListener('change', function(e) {
            uploadRoomPhotos(e.target.files);
        });
    }
    
    // Event space photo upload
    const eventSpacePhotoUpload = document.getElementById('eventSpacePhotoUpload');
    if (eventSpacePhotoUpload) {
        eventSpacePhotoUpload.addEventListener('change', function(e) {
            uploadEventSpacePhotos(e.target.files);
        });
    }
});

async function uploadRoomPhotos(files) {
    for (let file of files) {
        try {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('uploadedBy', currentUser.username);
            
            const response = await fetch(`/api/photos/rooms/${currentRoomId}/upload`, {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                showAlert(`Photo ${file.name} uploaded successfully!`, 'success');
                loadRoomPhotos(currentRoomId);
            } else {
                const error = await response.json();
                showAlert(error.message || `Error uploading ${file.name}`, 'danger');
            }
        } catch (error) {
            console.error('Error uploading photo:', error);
            showAlert(`Error uploading ${file.name}`, 'danger');
        }
    }
}

async function uploadEventSpacePhotos(files) {
    for (let file of files) {
        try {
            const formData = new FormData();
            formData.append('file', file);
            formData.append('uploadedBy', currentUser.username);
            
            const response = await fetch(`/api/photos/event-spaces/${currentEventSpaceId}/upload`, {
                method: 'POST',
                body: formData
            });
            
            if (response.ok) {
                showAlert(`Photo ${file.name} uploaded successfully!`, 'success');
                loadEventSpacePhotos(currentEventSpaceId);
            } else {
                const error = await response.json();
                showAlert(error.message || `Error uploading ${file.name}`, 'danger');
            }
        } catch (error) {
            console.error('Error uploading photo:', error);
            showAlert(`Error uploading ${file.name}`, 'danger');
        }
    }
}

async function deletePhoto(photoId) {
    if (confirm('Are you sure you want to delete this photo?')) {
        try {
            const response = await fetch(`/api/photos/${photoId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });
            
            if (response.ok) {
                showAlert('Photo deleted successfully!', 'success');
                if (currentRoomId) {
                    loadRoomPhotos(currentRoomId);
                }
                if (currentEventSpaceId) {
                    loadEventSpacePhotos(currentEventSpaceId);
                }
            } else {
                showAlert('Error deleting photo', 'danger');
            }
        } catch (error) {
            console.error('Error deleting photo:', error);
            showAlert('Error deleting photo', 'danger');
        }
    }
}

// Photo Gallery Functions
function showPhotoGallery(photos, title) {
    const container = document.getElementById('photoGalleryContainer');
    container.innerHTML = '';
    
    photos.forEach((photo, index) => {
        const photoDiv = document.createElement('div');
        photoDiv.className = 'col-md-6 col-lg-4 mb-3';
        photoDiv.innerHTML = `
            <div class="card">
                <img src="${photo.downloadUrl}" class="card-img-top" alt="${title} Photo ${index + 1}" 
                     style="height: 250px; object-fit: cover; cursor: pointer;" 
                     onclick="openPhotoViewer('${photo.downloadUrl}', '${photo.originalFileName}')">
                <div class="card-body">
                    <p class="card-text small">${photo.originalFileName}</p>
                </div>
            </div>
        `;
        container.appendChild(photoDiv);
    });
    
    document.querySelector('#photoGalleryModal .modal-title').textContent = title;
    new bootstrap.Modal(document.getElementById('photoGalleryModal')).show();
}

function openPhotoViewer(imageUrl, fileName) {
    // Create a new window or modal to show the full-size image
    const modal = document.createElement('div');
    modal.className = 'modal fade';
    modal.innerHTML = `
        <div class="modal-dialog modal-xl">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">${fileName}</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body text-center">
                    <img src="${imageUrl}" class="img-fluid" alt="${fileName}">
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    new bootstrap.Modal(modal).show();
    
    modal.addEventListener('hidden.bs.modal', function() {
        document.body.removeChild(modal);
    });
} 