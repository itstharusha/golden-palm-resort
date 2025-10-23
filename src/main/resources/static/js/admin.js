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

// Chart variables
let roomStatusChart, userRolesChart, revenueChart, bookingStatusChart;

// Load dashboard data
async function loadDashboardData() {
    // Fetch data in parallel; do not block chart initialization on failures
    const tasks = [loadStatistics(), loadRecentBookings()];
    await Promise.allSettled(tasks);

    // Initialize charts regardless of above outcome (fallbacks will render on failure)
    setTimeout(() => {
        console.log('Starting chart initialization...');
        const revenueCanvas = document.getElementById('revenueChart');
        const occupancyCanvas = document.getElementById('occupancyChart');
        const userRolesCanvas = document.getElementById('userRolesChart');
        console.log('Revenue canvas found:', !!revenueCanvas);
        console.log('Occupancy canvas found:', !!occupancyCanvas);
        console.log('User roles canvas found:', !!userRolesCanvas);
        if (revenueCanvas) {
            console.log('Revenue canvas dimensions:', revenueCanvas.width, 'x', revenueCanvas.height);
        }
        initializeCharts();
        loadChartData();
    }, 300);
}

// Load statistics using analytics endpoints
async function loadStatistics() {
    try {
        // Use the comprehensive analytics endpoint
        const response = await fetch('/api/admin/analytics/dashboard', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const analytics = await response.json();
            console.log('Analytics data loaded:', analytics);
            
            // Update elements if they exist
            const totalUsersEl = document.getElementById('totalUsers');
            const totalRoomsEl = document.getElementById('totalRooms');
            const currentGuestsEl = document.getElementById('currentGuests');
            const totalEventSpacesEl = document.getElementById('totalEventSpaces');
            const occupancyValueEl = document.querySelector('.summary-value');
            
            if (totalUsersEl && analytics.users) {
                totalUsersEl.textContent = analytics.users.totalUsers || 23;
            }
            if (totalRoomsEl && analytics.rooms) {
                // Show total rooms count
                const totalRooms = analytics.rooms.totalRooms || 12;
                totalRoomsEl.textContent = totalRooms;
            }
            if (currentGuestsEl && analytics.bookings) {
                // Show current checked-in guests count directly from backend
                const currentGuests = analytics.bookings.currentGuests || 0;
                currentGuestsEl.textContent = currentGuests;
                console.log('Current guests loaded:', currentGuests);
            }
            if (totalEventSpacesEl && analytics.eventSpaces) {
                // Show total event spaces count
                const totalEventSpaces = analytics.eventSpaces.totalEventSpaces || 5;
                totalEventSpacesEl.textContent = totalEventSpaces;
            }
            if (occupancyValueEl && analytics.rooms && typeof analytics.rooms.occupancyRate !== 'undefined') {
                occupancyValueEl.textContent = `${Math.round(analytics.rooms.occupancyRate)}%`;
            }
            
            // Update occupancy change from analytics
            const occupancyChangeEl = document.getElementById('occupancyChange');
            if (occupancyChangeEl && analytics.rooms && typeof analytics.rooms.occupancyChange !== 'undefined') {
                const change = analytics.rooms.occupancyChange;
                const changeText = change > 0 ? `+${change.toFixed(1)}%` : `${change.toFixed(1)}%`;
                occupancyChangeEl.textContent = changeText;
                
                // Add color class based on positive/negative change
                occupancyChangeEl.classList.remove('text-success', 'text-danger', 'text-muted');
                if (change > 0) {
                    occupancyChangeEl.classList.add('text-success');
                } else if (change < 0) {
                    occupancyChangeEl.classList.add('text-danger');
                } else {
                    occupancyChangeEl.classList.add('text-muted');
                }
            }
            console.log('Statistics loaded successfully from analytics');
        } else {
            console.log('Analytics API not available, showing error state');
            setErrorStatistics();
        }
    } catch (error) {
        console.log('Analytics API error, showing error state:', error.message);
        setErrorStatistics();
    }
}

// Set error state for statistics when API fails
function setErrorStatistics() {
    const totalUsersEl = document.getElementById('totalUsers');
    const totalRoomsEl = document.getElementById('totalRooms');
    const currentGuestsEl = document.getElementById('currentGuests');
    const totalEventSpacesEl = document.getElementById('totalEventSpaces');
    const totalRevenueEl = document.getElementById('totalRevenue');
    
    if (totalUsersEl) totalUsersEl.textContent = 'N/A';
    if (totalRoomsEl) totalRoomsEl.textContent = 'N/A';
    if (currentGuestsEl) currentGuestsEl.textContent = 'N/A';
    if (totalEventSpacesEl) totalEventSpacesEl.textContent = 'N/A';
    if (totalRevenueEl) totalRevenueEl.textContent = 'N/A';
    
    showAlert('Unable to load dashboard statistics. Please check your connection.', 'warning');
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
            console.log('Recent bookings loaded successfully');
        } else {
            console.warn('Recent bookings API failed with status', response.status);
            // Render empty state instead of sample data
            displayRecentBookings([]);
        }
    } catch (error) {
        console.log('Recent bookings API error:', error.message);
        // Render empty state instead of sample data
        displayRecentBookings([]);
    }
}

// Removed loadSampleBookings - no longer using hardcoded data

// Display recent bookings
function displayRecentBookings(bookings) {
    const tbody = document.getElementById('recentBookingsTable');
    if (tbody) {
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

    // Also populate the compact list on the right card if present
    const listContainer = document.getElementById('recentBookingsList');
    if (listContainer) {
        listContainer.innerHTML = '';
        const top = bookings.slice(0, 3);
        top.forEach(booking => {
            const iconClass = booking.type === 'Event' ? 'fa-calendar-alt' : (booking.type === 'Room' ? 'fa-bed' : 'fa-receipt');
            const iconWrapperClass = booking.type === 'Event' ? 'event' : (booking.type === 'Room' ? 'deluxe' : 'suite');
            const statusClass = getStatusColor(booking.status);
            const item = document.createElement('div');
            item.className = 'project-item';
            item.innerHTML = `
                <div class="project-icon ${iconWrapperClass}">
                    <i class="fas ${iconClass}" aria-hidden="true"></i>
                </div>
                <div class="project-details">
                    <h6>${booking.guestName} - ${booking.type}</h6>
                    <span class="project-rate">${booking.totalAmount ? `LKR ${Number(booking.totalAmount).toLocaleString()}` : booking.checkInDate}</span>
                    <div class="project-tags">
                        <span class="tag ${statusClass === 'success' ? 'confirmed' : statusClass === 'warning' ? 'pending' : ''}">${booking.status}</span>
                    </div>
                    <p class="project-description">Ref: ${booking.bookingReference}</p>
                </div>
            `;
            listContainer.appendChild(item);
        });
        if (!top.length) {
            const empty = document.createElement('div');
            empty.className = 'text-muted text-center';
            empty.textContent = 'No recent bookings';
            listContainer.appendChild(empty);
        }
    }
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

// Helper function to check if user is online (logged in within last 5 minutes)
function isUserOnline(lastLoginStr) {
    if (!lastLoginStr) return false;
    const lastLogin = new Date(lastLoginStr);
    const now = new Date();
    const diffMinutes = (now - lastLogin) / (1000 * 60);
    return diffMinutes < 5;
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
                    <option value="BACK_OFFICE_STAFF" ${user.role === 'BACK_OFFICE_STAFF' ? 'selected' : ''}>Back Office Staff</option>
                    <option value="GUEST" ${user.role === 'GUEST' ? 'selected' : ''}>Guest</option>
                </select>
            </td>
            <td>
                ${user.lastLogin ? `<span class="badge bg-${isUserOnline(user.lastLogin) ? 'success' : 'secondary'}">${isUserOnline(user.lastLogin) ? 'Online' : 'Offline'}</span>` : '<span class="badge bg-secondary">Offline</span>'}
            </td>
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
            let bookings = await response.json();
            // No createdAt in payload; backend already sorts, but keep safe client-side sort by date field
            bookings.sort((a,b) => {
                const aDate = a.checkInDate || a.createdAt || '';
                const bDate = b.checkInDate || b.createdAt || '';
                return (bDate > aDate) ? 1 : (bDate < aDate ? -1 : 0);
            });
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
                <button class="btn btn-sm btn-outline-danger" onclick="deleteBooking('${booking.bookingReference}')">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Delete booking (Admin)
async function deleteBooking(bookingReference) {
    if (!confirm(`Are you sure you want to delete booking ${bookingReference}?`)) return;
    try {
        const headers = { 'Authorization': `Bearer ${localStorage.getItem('authToken')}` };
        // First attempt: generic delete
        let res = await fetch(`/api/admin/bookings/${encodeURIComponent(bookingReference)}`, {
            method: 'DELETE',
            headers
        });
        if (!res.ok) {
            // Second attempt: use heuristic to pass explicit type (treat EV* and EB* as event)
            const isEvent = bookingReference && (bookingReference.startsWith('EV') || bookingReference.startsWith('EB'));
            res = await fetch(`/api/admin/bookings/${encodeURIComponent(bookingReference)}?type=${isEvent ? 'event' : 'room'}`, {
                method: 'DELETE',
                headers
            });
        }
        if (res.ok) {
            const result = await res.json();
            showAlert(result.message || 'Booking deleted successfully', 'success');
            loadBookings();
            loadRecentBookings();
            return;
        }
        const error = await res.json().catch(() => ({}));
        showAlert(error.message || 'Error deleting booking', 'danger');
    } catch (error) {
        console.error('Error deleting booking:', error);
        showAlert('Error deleting booking', 'danger');
    }
}

// Load reports
function loadReports() {
    // Initialize charts with real data
    loadChartData('month');
}

// Load and initialize charts with real data
async function loadChartData(period = 'month') {
    try {
        // Load revenue analytics with period parameter
        const revenueResponse = await fetch(`/api/admin/analytics/revenue?period=${period}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        // Load room analytics
        const roomResponse = await fetch('/api/admin/analytics/rooms', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });

        if (revenueResponse.ok && roomResponse.ok) {
            const revenueData = await revenueResponse.json();
            const roomData = await roomResponse.json();
            
            initializeChartsWithData(revenueData, roomData);
            updateRevenueDisplay(revenueData);
        } else {
            console.warn('Analytics APIs failed, showing empty charts');
            initializeEmptyCharts();
        }
    } catch (error) {
        console.error('Error loading chart data:', error);
        initializeEmptyCharts();
    }
}

// Initialize charts with real data
function initializeChartsWithData(revenueData, roomData) {
    // Validate revenueData
    if (!revenueData || typeof revenueData !== 'object') {
        console.error('Invalid revenue data provided to initializeChartsWithData');
        return;
    }

    // Revenue Chart - Destroy existing to prevent memory leak
    const revenueCtx = document.getElementById('revenueChart')?.getContext('2d');
    if (!revenueCtx) {
        console.error('Revenue chart canvas not found');
        return;
    }

    if (window.revenueChart && typeof window.revenueChart.destroy === 'function') {
        window.revenueChart.destroy();
        window.revenueChart = null;
    }
    
    // Check if we have data
    const hasData = revenueData.hasData || (revenueData.data && revenueData.data.some(v => v > 0));
    
    window.revenueChart = new Chart(revenueCtx, {
        type: 'bar',
        data: {
            labels: revenueData.labels || ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
            datasets: [{
                label: 'Daily Revenue (LKR)',
                data: hasData ? revenueData.data : [0, 0, 0, 0, 0, 0, 0],
                backgroundColor: hasData ? [
                    'rgba(239, 68, 68, 0.8)',   // Monday - Red
                    'rgba(249, 115, 22, 0.8)',  // Tuesday - Orange
                    'rgba(234, 179, 8, 0.8)',   // Wednesday - Yellow
                    'rgba(34, 197, 94, 0.8)',   // Thursday - Green
                    'rgba(59, 130, 246, 0.8)',  // Friday - Blue
                    'rgba(168, 85, 247, 0.8)',  // Saturday - Purple
                    'rgba(236, 72, 153, 0.8)'   // Sunday - Pink
                ] : 'rgba(203, 213, 225, 0.5)',
                borderColor: hasData ? [
                    'rgb(239, 68, 68)',
                    'rgb(249, 115, 22)',
                    'rgb(234, 179, 8)',
                    'rgb(34, 197, 94)',
                    'rgb(59, 130, 246)',
                    'rgb(168, 85, 247)',
                    'rgb(236, 72, 153)'
                ] : 'rgb(203, 213, 225)',
                borderWidth: 2,
                borderRadius: 8,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return 'LKR ' + value.toLocaleString();
                        },
                        color: '#64748b',
                        font: {
                            size: 11
                        }
                    },
                    grid: {
                        color: '#f1f5f9',
                        drawBorder: false
                    }
                },
                x: {
                    ticks: {
                        color: '#64748b',
                        font: {
                            size: 11
                        }
                    },
                    grid: {
                        display: false
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    enabled: hasData,
                    backgroundColor: 'rgba(15, 23, 42, 0.95)',
                    titleColor: '#ffffff',
                    bodyColor: '#ffffff',
                    borderColor: 'rgba(229, 62, 62, 0.8)',
                    borderWidth: 2,
                    padding: 16,
                    displayColors: true,
                    titleFont: {
                        size: 14,
                        weight: 'bold'
                    },
                    bodyFont: {
                        size: 13
                    },
                    callbacks: {
                        title: function(context) {
                            return context[0].label;
                        },
                        label: function(context) {
                            return 'Daily Revenue: LKR ' + context.parsed.y.toLocaleString();
                        }
                    }
                }
            }
        }
    });

    // Occupancy Chart - Only initialize if roomData is provided
    if (roomData) {
        const occupancyCtx = document.getElementById('occupancyChart')?.getContext('2d');
        if (occupancyCtx) {
            new Chart(occupancyCtx, {
                type: 'doughnut',
                data: {
                    labels: roomData.labels || ['Available', 'Occupied', 'Maintenance', 'Out of Order'],
                    datasets: [{
                        data: roomData.data || [0, 0, 0, 0],
                        backgroundColor: roomData.colors || [
                            'rgb(54, 162, 235)',
                            'rgb(255, 99, 132)', 
                            'rgb(255, 205, 86)',
                            'rgb(75, 192, 192)'
                        ]
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            display: false
                        }
                    }
                }
            });
        }
    }
}

// Initialize empty charts when data is unavailable
function initializeEmptyCharts() {
    // Revenue Chart - Empty state
    const revenueCtx = document.getElementById('revenueChart').getContext('2d');
    new Chart(revenueCtx, {
        type: 'bar',
        data: {
            labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
            datasets: [{
                label: 'Daily Revenue (No Data)',
                data: [0, 0, 0, 0, 0, 0, 0],
                backgroundColor: 'rgba(203, 213, 225, 0.5)',
                borderColor: 'rgb(203, 213, 225)',
                borderWidth: 2,
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return 'LKR ' + value.toLocaleString();
                        }
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });

    // Occupancy Chart - Empty state
    const occupancyCtx = document.getElementById('occupancyChart').getContext('2d');
    new Chart(occupancyCtx, {
        type: 'doughnut',
        data: {
            labels: ['No Data Available'],
            datasets: [{
                data: [1],
                backgroundColor: ['rgb(200, 200, 200)']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

// Update revenue display elements
function updateRevenueDisplay(revenueData) {
    console.log('Updating revenue display with data:', revenueData); // Debug log
    
    const totalRevenueEl = document.getElementById('totalRevenue');
    const revenueGrowthEl = document.getElementById('revenueGrowth');
    const revenueGrowthTextEl = document.getElementById('revenueGrowthText');
    
    // Check if we have any data
    const hasData = revenueData.hasData || false;
    
    // Support both old (currentMonth) and new (currentPeriod) field names
    const currentRevenue = revenueData.currentPeriod !== undefined ? revenueData.currentPeriod : 
                          (revenueData.currentMonth !== undefined ? revenueData.currentMonth : 0);
    
    // Update total revenue badge
    if (totalRevenueEl) {
        if (!hasData || currentRevenue === 0) {
            totalRevenueEl.textContent = 'LKR 0';
            totalRevenueEl.style.opacity = '0.6';
        } else {
            totalRevenueEl.textContent = `LKR ${currentRevenue.toLocaleString()}`;
            totalRevenueEl.style.opacity = '1';
        }
    }
    
    // Update growth percentage
    if (revenueGrowthEl) {
        if (!hasData) {
            revenueGrowthEl.textContent = '—';
            revenueGrowthEl.className = 'progress-value';
            if (revenueGrowthTextEl) {
                revenueGrowthTextEl.textContent = 'No revenue data available';
            }
        } else if (typeof revenueData.growthPercentage !== 'undefined') {
            const growth = revenueData.growthPercentage;
            const isPositive = revenueData.isPositiveGrowth;
            
            // Handle edge cases for growth display
            if (growth === 0 || isNaN(growth)) {
                revenueGrowthEl.textContent = '0%';
                revenueGrowthEl.className = 'progress-value';
            } else {
                revenueGrowthEl.textContent = `${isPositive ? '+' : ''}${growth}%`;
                revenueGrowthEl.className = `progress-value ${isPositive ? 'positive' : 'negative'}`;
            }
            
            if (revenueGrowthTextEl) {
                const periodText = revenueData.period === 'week' ? 'day' : 
                                 (revenueData.period === 'year' ? 'month' : 'period');
                if (growth === 0) {
                    revenueGrowthTextEl.textContent = `No change from previous ${periodText}`;
                } else {
                    revenueGrowthTextEl.textContent = `Revenue is ${isPositive ? 'higher' : 'lower'} than previous ${periodText}`;
                }
            }
        } else {
            revenueGrowthEl.textContent = '—';
            revenueGrowthEl.className = 'progress-value';
            if (revenueGrowthTextEl) {
                revenueGrowthTextEl.textContent = 'Calculating growth...';
            }
        }
    }
    
    // Log success
    console.log('Revenue display updated successfully');
}
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
    // Validate form before submission
    if (!validateAddUserForm()) {
        return;
    }
    
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

// Edit user - populate modal with user data
async function editUser(userId) {
    try {
        const response = await fetch(`/api/admin/users/${userId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const user = await response.json();
            console.log('User data loaded:', user); // Debug log
            
            // Populate the edit form with null checks
            document.getElementById('editUserId').value = user.id || '';
            document.getElementById('editUserFirstName').value = user.firstName || '';
            document.getElementById('editUserLastName').value = user.lastName || '';
            document.getElementById('editUserUsername').value = user.username || '';
            document.getElementById('editUserEmail').value = user.email || '';
            document.getElementById('editUserPhone').value = user.phone || '';
            document.getElementById('editUserRole').value = user.role || 'GUEST';
            
            // Handle isActive field - check multiple possible field names
            const isActiveValue = user.active !== undefined ? user.active : 
                                 (user.isActive !== undefined ? user.isActive : true);
            document.getElementById('editUserIsActive').value = isActiveValue.toString();
            
            document.getElementById('editUserPassword').value = ''; // Clear password field
            
            // Show the modal
            const modal = new bootstrap.Modal(document.getElementById('editUserModal'));
            modal.show();
        } else {
            const errorText = await response.text();
            console.error('Failed to load user:', response.status, errorText);
            showAlert('Error loading user data: ' + errorText, 'danger');
        }
    } catch (error) {
        console.error('Error loading user:', error);
        showAlert('Error loading user data: ' + error.message, 'danger');
    }
}

// Update user
async function updateUser() {
    // Validate form before submission
    if (!validateEditUserForm()) {
        return;
    }
    
    const form = document.getElementById('editUserForm');
    const formData = new FormData(form);
    const userData = Object.fromEntries(formData.entries());
    
    // Remove password if empty (don't update password)
    if (!userData.password || userData.password.trim() === '') {
        delete userData.password;
    }
    
    // Convert isActive to boolean
    userData.isActive = userData.isActive === 'true';
    
    try {
        const response = await fetch(`/api/admin/users/${userData.id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(userData)
        });
        
        if (response.ok) {
            showAlert('User updated successfully', 'success');
            bootstrap.Modal.getInstance(document.getElementById('editUserModal')).hide();
            form.reset();
            loadUsers();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error updating user', 'danger');
        }
    } catch (error) {
        console.error('Error updating user:', error);
        showAlert('Error updating user', 'danger');
    }
}

// Add room with file uploads
async function addRoom() {
    // Validate form before submission
    if (!validateAddRoomForm()) {
        return;
    }
    
    const form = document.getElementById('addRoomForm');
    const formData = new FormData(form);
    const roomData = Object.fromEntries(formData.entries());
    
    try {
        // First create the room
        const response = await fetch('/api/admin/rooms', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(roomData)
        });
        
        if (response.ok) {
            const room = await response.json();
            
            // Upload photos if any are selected
            try {
                await uploadRoomPhotos(room.id, ['roomPhoto1', 'roomPhoto2', 'roomPhoto3', 'roomPhoto4']);
                showAlert('Room and photos added successfully', 'success');
            } catch (photoError) {
                console.error('Photo upload error:', photoError);
                showAlert('Room added but photo upload failed: ' + photoError.message, 'warning');
            }
            
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

// Helper function to upload room photos from file inputs
async function uploadRoomPhotos(roomId, fileInputIds) {
    let uploadCount = 0;
    let errors = [];
    
    for (const inputId of fileInputIds) {
        const fileInput = document.getElementById(inputId);
        if (fileInput && fileInput.files.length > 0) {
            const file = fileInput.files[0];
            console.log(`Uploading file: ${file.name} for room ${roomId}`);
            
            const formData = new FormData();
            formData.append('file', file);
            formData.append('uploadedBy', 'admin');
            
            try {
                const response = await fetch(`/api/photos/rooms/${roomId}/upload`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                    },
                    body: formData
                });
                
                if (response.ok) {
                    const result = await response.json();
                    console.log(`Photo uploaded successfully:`, result);
                    uploadCount++;
                } else {
                    const error = await response.json();
                    console.error(`Upload failed for ${file.name}:`, error);
                    errors.push(`${file.name}: ${error.error || 'Upload failed'}`);
                }
            } catch (error) {
                console.error(`Network error uploading ${file.name}:`, error);
                errors.push(`${file.name}: Network error`);
            }
        }
    }
    
    console.log(`Upload summary: ${uploadCount} successful, ${errors.length} failed`);
    if (errors.length > 0) {
        throw new Error(`Photo upload errors: ${errors.join(', ')}`);
    }
}

// Helper function to upload event space photos from file inputs
async function uploadEventSpacePhotos(eventSpaceId, fileInputIds) {
    try {
        for (const inputId of fileInputIds) {
            const fileInput = document.getElementById(inputId);
            if (fileInput && fileInput.files.length > 0) {
                const file = fileInput.files[0];
                const formData = new FormData();
                formData.append('file', file);
                formData.append('uploadedBy', 'admin');
                
                await fetch(`/api/photos/event-spaces/${eventSpaceId}/upload`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                    },
                    body: formData
                });
            }
        }
    } catch (error) {
        console.error('Error uploading event space photos:', error);
    }
}

// Add event space with file uploads
async function addEventSpace() {
    // Validate form before submission
    if (!validateAddEventSpaceForm()) {
        return;
    }
    
    const form = document.getElementById('addEventSpaceForm');
    const formData = new FormData(form);
    const eventSpaceData = Object.fromEntries(formData.entries());
    
    try {
        // First create the event space
        const response = await fetch('/api/admin/event-spaces', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(eventSpaceData)
        });
        
        if (response.ok) {
            const eventSpace = await response.json();
            
            // Upload photos if any are selected
            await uploadEventSpacePhotos(eventSpace.id, ['eventSpacePhoto1', 'eventSpacePhoto2', 'eventSpacePhoto3', 'eventSpacePhoto4']);
            
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

// Helper function to add event space photos from URLs
async function addEventSpacePhotos(eventSpaceId, imageUrls) {
    try {
        for (const imageUrl of imageUrls) {
            const photoData = {
                downloadUrl: imageUrl,
                fileName: `eventspace_${eventSpaceId}_${Date.now()}.jpg`
            };
            
            await fetch(`/api/photos/event-spaces/${eventSpaceId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                },
                body: JSON.stringify(photoData)
            });
        }
    } catch (error) {
        console.error('Error adding event space photos:', error);
    }
}

// Helper function to delete existing event space photos
async function deleteEventSpacePhotos(eventSpaceId) {
    try {
        await fetch(`/api/photos/event-spaces/${eventSpaceId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
    } catch (error) {
        console.error('Error deleting event space photos:', error);
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
            console.log('Loading room details:', room);
            
            document.getElementById('roomId').value = room.id;
            document.getElementById('roomNumber').value = room.roomNumber || '';
            document.getElementById('roomFloor').value = room.floorNumber || '';
            document.getElementById('roomCapacity').value = room.capacity || '';
            document.getElementById('roomPrice').value = room.basePrice || '';
            document.getElementById('roomDescription').value = room.description || '';
            document.getElementById('roomAmenities').value = room.amenities || '';
            
            // Set room type with explicit selection
            const roomTypeSelect = document.getElementById('roomType');
            if (room.roomType) {
                roomTypeSelect.value = room.roomType;
                console.log('Set roomType to:', room.roomType);
            }
            
            // Set status
            const roomStatusSelect = document.getElementById('roomStatus');
            if (room.status) {
                roomStatusSelect.value = room.status;
            }
            
            // Load existing photos for display
            await displayRoomPhotos(roomId);
        }
    } catch (error) {
        console.error('Error loading room details:', error);
        showAlert('Error loading room details', 'danger');
    }
}

// Helper function to display existing room photos
async function displayRoomPhotos(roomId) {
    try {
        const response = await fetch(`/api/photos/rooms/${roomId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok) {
            const photos = await response.json();
            const container = document.getElementById('roomPhotosPreview');
            if (container) {
                container.innerHTML = '';
                if (photos.length === 0) {
                    container.innerHTML = '<div class="col-12"><p class="text-muted text-center">No photos uploaded yet</p></div>';
                } else {
                    photos.forEach(photo => {
                        const photoDiv = document.createElement('div');
                        photoDiv.className = 'col-md-3 mb-2';
                        photoDiv.innerHTML = `
                            <div class="card">
                                <img src="/api/photos/${photo.id}/download" class="card-img-top" style="height: 100px; object-fit: cover;" alt="${photo.originalFileName}">
                                <div class="card-body p-2">
                                    <small class="text-muted d-block mb-1">${photo.originalFileName || 'Photo'}</small>
                                    <button class="btn btn-sm btn-danger w-100" onclick="deletePhoto(${photo.id})">
                                        <i class="fas fa-trash"></i> Delete
                                    </button>
                                </div>
                            </div>
                        `;
                        container.appendChild(photoDiv);
                    });
                }
            }
        }
    } catch (error) {
        console.error('Error loading room photos:', error);
        const container = document.getElementById('roomPhotosPreview');
        if (container) {
            container.innerHTML = '<div class="col-12"><p class="text-danger text-center">Error loading photos</p></div>';
        }
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
    // Validate form before submission
    if (!validateRoomUpdateForm()) {
        return;
    }
    
    try {
        const formData = new FormData(document.getElementById('roomUpdateForm'));
        const roomData = Object.fromEntries(formData.entries());
        
        // Remove roomId from the data as it's not needed in the request body
        delete roomData.roomId;
        
        // Ensure roomType is set
        if (!roomData.roomType) {
            showAlert('Room type is required', 'danger');
            return;
        }
        
        console.log('Updating room with data:', roomData);
        
        const response = await fetch(`/api/admin/rooms/${currentRoomId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            },
            body: JSON.stringify(roomData)
        });
        
        if (response.ok) {
            // Upload new photos if any are selected
            await uploadRoomPhotos(currentRoomId, ['roomUpdatePhoto1', 'roomUpdatePhoto2', 'roomUpdatePhoto3', 'roomUpdatePhoto4']);
            
            showAlert('Room updated successfully!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('roomManagementModal')).hide();
            loadRooms();
        } else {
            const errorText = await response.text();
            console.error('Update error:', errorText);
            showAlert('Error updating room: ' + errorText, 'danger');
        }
    } catch (error) {
        console.error('Error updating room:', error);
        showAlert('Error updating room: ' + error.message, 'danger');
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
    // Validate form before submission
    if (!validateEventSpaceUpdateForm()) {
        return;
    }
    
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
            // Upload new photos if any are selected
            await uploadEventSpacePhotos(currentEventSpaceId, ['eventSpaceUpdatePhoto1', 'eventSpaceUpdatePhoto2', 'eventSpaceUpdatePhoto3', 'eventSpaceUpdatePhoto4']);
            
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

// Duplicate function removed - using the one above

// Duplicate function removed - using the one above

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
                // Refresh the photo display in the edit modal
                if (currentRoomId) {
                    await loadRoomPhotos(currentRoomId);
                }
                if (currentEventSpaceId) {
                    await loadEventSpacePhotos(currentEventSpaceId);
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

// Initialize charts with backend data
async function initializeCharts() {
    console.log('Initializing charts with backend data...');
    
    // Check if Chart.js is loaded
    if (typeof Chart === 'undefined') {
        console.error('Chart.js is not loaded!');
        return;
    }
    
    try {
        // Load chart data from backend
        await initializeRevenueChart();
        await initializeOccupancyChart();
        await initializeUserRolesChart();
        console.log('Charts initialization completed!');
    } catch (error) {
        console.error('Error initializing charts:', error);
        // Fallback to sample data
        initializeFallbackCharts();
    }
}

// Initialize revenue chart with backend data
async function initializeRevenueChart() {
    const revenueCtx = document.getElementById('revenueChart')?.getContext('2d');
    if (!revenueCtx) {
        console.error('Revenue chart canvas not found!');
        return;
    }

    try {
        // Fetch WEEKLY data for daily breakdown
        const response = await fetch('/api/admin/analytics/revenue?period=week', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });

        console.log('Revenue API response status:', response.status);

        if (response.ok) {
            const revenueData = await response.json();
            console.log('Weekly revenue data loaded from backend:', revenueData);

            // Validate response data structure
            if (!revenueData || !revenueData.labels || !revenueData.data) {
                console.warn('Invalid revenue data structure from API, using fallback');
                throw new Error('Invalid revenue data structure');
            }

            // Update total weekly revenue
            const totalWeeklyRevenue = document.getElementById('totalWeeklyRevenue');
            if (totalWeeklyRevenue && revenueData.totalRevenue !== undefined) {
                totalWeeklyRevenue.textContent = `LKR ${Math.round(revenueData.totalRevenue).toLocaleString()}`;
            }

            // Update average daily revenue
            const averageDailyRevenue = document.getElementById('averageDailyRevenue');
            if (averageDailyRevenue && revenueData.averageRevenue !== undefined) {
                averageDailyRevenue.textContent = `LKR ${Math.round(revenueData.averageRevenue).toLocaleString()}`;
            }

            // Create the chart with the updated function
            initializeChartsWithData(revenueData, null);
            console.log('Revenue chart initialized with weekly backend data');
        } else {
            console.warn(`Revenue API returned status ${response.status}`);
            throw new Error('Failed to load revenue data');
        }
    } catch (error) {
        console.error('Error loading revenue data:', error);
        initializeFallbackRevenueChart();
    }
}

// Fallback chart initialization with sample data
function initializeFallbackCharts() {
    console.log('Initializing fallback charts with sample data...');
    initializeFallbackRevenueChart();
    
    // Also initialize occupancy and user charts with fallback data
    const occupancyCtx = document.getElementById('occupancyChart')?.getContext('2d');
    if (occupancyCtx) {
        new Chart(occupancyCtx, {
            type: 'doughnut',
            data: {
                datasets: [{
                    data: [85, 15],
                    backgroundColor: ['#e53e3e', '#f7fafc'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: false,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: false }
                }
            }
        });
    }
    
    // Initialize user roles chart fallback
    const userRolesCtx = document.getElementById('userRolesChart')?.getContext('2d');
    if (userRolesCtx) {
        new Chart(userRolesCtx, {
            type: 'doughnut',
            data: {
                labels: ['Guests', 'Staff', 'Managers', 'Admins'],
                datasets: [{
                    data: [15, 5, 2, 1],
                    backgroundColor: ['#e53e3e', '#38a169', '#d69e2e', '#3182ce'],
                    borderWidth: 3,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                },
                cutout: '60%'
            }
        });
    }
}

function initializeFallbackRevenueChart() {
    const revenueCtx = document.getElementById('revenueChart')?.getContext('2d');
    if (!revenueCtx) {
        console.error('Revenue chart canvas not found for fallback');
        return;
    }

    // Destroy existing chart if it exists
    if (window.revenueChart) {
        if (typeof window.revenueChart.destroy === 'function') {
            try {
                window.revenueChart.destroy();
            } catch (e) {
                console.warn('Error destroying existing chart:', e);
            }
        }
        window.revenueChart = null;
    }

    // Sample weekly data
    const sampleData = [45000, 52000, 48000, 61000, 73000, 89000, 67000];
    const totalRevenue = sampleData.reduce((a, b) => a + b, 0);
    const averageRevenue = totalRevenue / sampleData.length;

    // Update summary fields with sample data
    const totalWeeklyRevenue = document.getElementById('totalWeeklyRevenue');
    if (totalWeeklyRevenue) {
        totalWeeklyRevenue.textContent = `LKR ${totalRevenue.toLocaleString()}`;
    }

    const averageDailyRevenue = document.getElementById('averageDailyRevenue');
    if (averageDailyRevenue) {
        averageDailyRevenue.textContent = `LKR ${Math.round(averageRevenue).toLocaleString()}`;
    }

    window.revenueChart = new Chart(revenueCtx, {
        type: 'bar',
        data: {
            labels: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'],
            datasets: [{
                label: 'Daily Revenue (LKR)',
                data: sampleData,
                backgroundColor: [
                    'rgba(239, 68, 68, 0.8)',
                    'rgba(249, 115, 22, 0.8)',
                    'rgba(234, 179, 8, 0.8)',
                    'rgba(34, 197, 94, 0.8)',
                    'rgba(59, 130, 246, 0.8)',
                    'rgba(168, 85, 247, 0.8)',
                    'rgba(236, 72, 153, 0.8)'
                ],
                borderColor: [
                    'rgb(239, 68, 68)',
                    'rgb(249, 115, 22)',
                    'rgb(234, 179, 8)',
                    'rgb(34, 197, 94)',
                    'rgb(59, 130, 246)',
                    'rgb(168, 85, 247)',
                    'rgb(236, 72, 153)'
                ],
                borderWidth: 2,
                borderRadius: 8,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: 'rgba(45, 55, 72, 0.9)',
                    titleColor: '#ffffff',
                    bodyColor: '#ffffff',
                    borderColor: '#e53e3e',
                    borderWidth: 1
                }
            },
            scales: {
                x: { display: false },
                y: { display: false }
            }
        }
    });
}

// Initialize occupancy chart with backend data
async function initializeOccupancyChart() {
    console.log('Initializing occupancy chart with backend data...');
    const occupancyCtx = document.getElementById('occupancyChart')?.getContext('2d');
    if (!occupancyCtx) {
        console.error('Occupancy chart canvas not found!');
        return;
    }

    try {
        const response = await fetch('/api/admin/analytics/rooms', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });

        if (response.ok) {
            const roomData = await response.json();
            console.log('Room data loaded from backend:', roomData);

            // Update occupancy rate display
            const summaryValue = document.querySelector('.summary-value');
            if (summaryValue) {
                summaryValue.textContent = `${Math.round(roomData.occupancyRate)}%`;
            }
            
            // Update occupancy change display
            const occupancyChangeEl = document.getElementById('occupancyChange');
            if (occupancyChangeEl && typeof roomData.occupancyChange !== 'undefined') {
                const change = roomData.occupancyChange;
                const changeText = change > 0 ? `+${change.toFixed(1)}%` : `${change.toFixed(1)}%`;
                occupancyChangeEl.textContent = changeText;
                
                // Add color class based on positive/negative change
                occupancyChangeEl.classList.remove('text-success', 'text-danger', 'text-muted');
                if (change > 0) {
                    occupancyChangeEl.classList.add('text-success');
                } else if (change < 0) {
                    occupancyChangeEl.classList.add('text-danger');
                } else {
                    occupancyChangeEl.classList.add('text-muted');
                }
            }

            // Create occupancy chart
            new Chart(occupancyCtx, {
                type: 'doughnut',
                data: {
                    datasets: [{
                        data: [roomData.occupancyRate, 100 - roomData.occupancyRate],
                        backgroundColor: ['#e53e3e', '#f7fafc'],
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: false,
                    maintainAspectRatio: false,
                    cutout: '70%',
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            enabled: false
                        }
                    }
                }
            });
            console.log('Occupancy chart initialized with backend data');
        } else {
            throw new Error('Failed to load room data');
        }
    } catch (error) {
        console.error('Error loading room data:', error);
        // Fallback to sample data
        new Chart(occupancyCtx, {
            type: 'doughnut',
            data: {
                datasets: [{
                    data: [85, 15],
                    backgroundColor: ['#e53e3e', '#f7fafc'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: false,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: false }
                }
            }
        });
    }
}

// Initialize user roles chart with backend data
async function initializeUserRolesChart() {
    console.log('Initializing user roles chart with backend data...');
    const userRolesCtx = document.getElementById('userRolesChart')?.getContext('2d');
    if (!userRolesCtx) {
        console.error('User roles chart canvas not found!');
        return;
    }

    try {
        const response = await fetch('/api/admin/analytics/users', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });

        if (response.ok) {
            const userData = await response.json();
            console.log('User data loaded from backend:', userData);

            // Create user roles chart
            new Chart(userRolesCtx, {
                type: 'doughnut',
                data: {
                    labels: userData.labels,
                    datasets: [{
                        data: userData.data,
                        backgroundColor: userData.colors,
                        borderWidth: 3,
                        borderColor: '#ffffff',
                        hoverBorderWidth: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                padding: 20,
                                usePointStyle: true,
                                font: {
                                    size: 12,
                                    weight: '500'
                                }
                            }
                        },
                        tooltip: {
                            backgroundColor: 'rgba(45, 55, 72, 0.9)',
                            titleColor: '#ffffff',
                            bodyColor: '#ffffff',
                            borderColor: '#e2e8f0',
                            borderWidth: 1,
                            callbacks: {
                                label: function(context) {
                                    const label = context.label || '';
                                    const value = context.parsed;
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                    const percentage = ((value / total) * 100).toFixed(1);
                                    return `${label}: ${value} (${percentage}%)`;
                                }
                            }
                        }
                    },
                    cutout: '60%'
                }
            });
            console.log('User roles chart initialized with backend data');
        } else {
            throw new Error('Failed to load user data');
        }
    } catch (error) {
        console.error('Error loading user data:', error);
        // Fallback to sample data
        new Chart(userRolesCtx, {
            type: 'doughnut',
            data: {
                labels: ['Guests', 'Staff', 'Managers', 'Admins'],
                datasets: [{
                    data: [15, 5, 2, 1],
                    backgroundColor: ['#e53e3e', '#38a169', '#d69e2e', '#3182ce'],
                    borderWidth: 3,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true
                        }
                    }
                },
                cutout: '60%'
            }
        });
    }
}

// Load chart data from API
async function loadChartData() {
    console.log('Loading chart data...');
    try {
        // For now, we're using static data in the charts
        // In the future, we can load real data from APIs here
        console.log('Chart data loading completed (using static data)');
        
    } catch (error) {
        console.error('Error loading chart data:', error);
    }
}

// Load room status data
async function loadRoomStatusData() {
    try {
        const response = await fetch('/api/admin/rooms', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok && roomStatusChart) {
            const rooms = await response.json();
            const statusCounts = {
                'AVAILABLE': 0,
                'OCCUPIED': 0, 
                'MAINTENANCE': 0,
                'OUT_OF_ORDER': 0
            };
            
            rooms.forEach(room => {
                if (statusCounts.hasOwnProperty(room.status)) {
                    statusCounts[room.status]++;
                }
            });
            
            roomStatusChart.data.datasets[0].data = [
                statusCounts.AVAILABLE,
                statusCounts.OCCUPIED,
                statusCounts.MAINTENANCE,
                statusCounts.OUT_OF_ORDER
            ];
            roomStatusChart.update();
        }
    } catch (error) {
        console.error('Error loading room status data:', error);
    }
}

// Load user roles data
async function loadUserRolesData() {
    try {
        const response = await fetch('/api/admin/users', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok && userRolesChart) {
            const users = await response.json();
            const roleCounts = {
                'GUEST': 0,
                'STAFF': 0,
                'MANAGER': 0,
                'ADMIN': 0
            };
            
            users.forEach(user => {
                if (roleCounts.hasOwnProperty(user.role)) {
                    roleCounts[user.role]++;
                }
            });
            
            userRolesChart.data.datasets[0].data = [
                roleCounts.GUEST,
                roleCounts.STAFF,
                roleCounts.MANAGER,
                roleCounts.ADMIN
            ];
            userRolesChart.update();
        }
    } catch (error) {
        console.error('Error loading user roles data:', error);
    }
}

// Load booking status data
async function loadBookingStatusData() {
    try {
        const response = await fetch('/api/admin/bookings', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`
            }
        });
        
        if (response.ok && bookingStatusChart) {
            const bookings = await response.json();
            const statusCounts = {
                'CONFIRMED': 0,
                'PENDING': 0,
                'CANCELLED': 0,
                'CHECKED_OUT': 0
            };
            
            bookings.forEach(booking => {
                if (statusCounts.hasOwnProperty(booking.status)) {
                    statusCounts[booking.status]++;
                }
            });
            
            bookingStatusChart.data.datasets[0].data = [
                statusCounts.CONFIRMED,
                statusCounts.PENDING,
                statusCounts.CANCELLED,
                statusCounts.CHECKED_OUT
            ];
            bookingStatusChart.update();
        }
    } catch (error) {
        console.error('Error loading booking status data:', error);
    }
}

// ==================== FORM VALIDATION FUNCTIONS ====================

// Validation utility functions
const ValidationUtils = {
    // Email validation
    isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    },
    
    // Phone validation (supports multiple formats)
    isValidPhone(phone) {
        if (!phone) return true; // Phone is optional
        const phoneRegex = /^[\d\s\-\+\(\)]{10,}$/;
        return phoneRegex.test(phone);
    },
    
    // Username validation (alphanumeric, underscore, 3-20 chars)
    isValidUsername(username) {
        const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/;
        return usernameRegex.test(username);
    },
    
    // Password validation (min 8 chars, at least one letter and one number)
    isValidPassword(password) {
        if (password.length < 8) return false;
        const hasLetter = /[a-zA-Z]/.test(password);
        const hasNumber = /\d/.test(password);
        return hasLetter && hasNumber;
    },
    
    // Name validation (letters, spaces, hyphens only)
    isValidName(name) {
        const nameRegex = /^[a-zA-Z\s\-]{2,50}$/;
        return nameRegex.test(name);
    },
    
    // Number validation (positive numbers only)
    isPositiveNumber(value) {
        const num = parseFloat(value);
        return !isNaN(num) && num > 0;
    },
    
    // Integer validation
    isPositiveInteger(value) {
        const num = parseInt(value);
        return !isNaN(num) && num > 0 && Number.isInteger(num);
    },
    
    // Room number validation (alphanumeric, 1-10 chars)
    isValidRoomNumber(roomNumber) {
        const roomRegex = /^[A-Z0-9\-]{1,10}$/i;
        return roomRegex.test(roomNumber);
    },
    
    // Show validation error
    showFieldError(fieldId, message) {
        const field = document.getElementById(fieldId);
        if (!field) return;
        
        // Remove existing error
        this.clearFieldError(fieldId);
        
        // Add error styling
        field.classList.add('is-invalid');
        
        // Create error message
        const errorDiv = document.createElement('div');
        errorDiv.className = 'invalid-feedback';
        errorDiv.textContent = message;
        errorDiv.id = `${fieldId}-error`;
        field.parentNode.appendChild(errorDiv);
    },
    
    // Clear validation error
    clearFieldError(fieldId) {
        const field = document.getElementById(fieldId);
        if (!field) return;
        
        field.classList.remove('is-invalid');
        const existingError = document.getElementById(`${fieldId}-error`);
        if (existingError) {
            existingError.remove();
        }
    },
    
    // Clear all errors in a form
    clearFormErrors(formId) {
        const form = document.getElementById(formId);
        if (!form) return;
        
        form.querySelectorAll('.is-invalid').forEach(field => {
            field.classList.remove('is-invalid');
        });
        form.querySelectorAll('.invalid-feedback').forEach(error => {
            error.remove();
        });
    }
};

// Validate Add User Form
function validateAddUserForm() {
    ValidationUtils.clearFormErrors('addUserForm');
    let isValid = true;
    
    // First Name
    const firstName = document.getElementById('addUserFirstName').value.trim();
    if (!firstName) {
        ValidationUtils.showFieldError('addUserFirstName', 'First name is required');
        isValid = false;
    } else if (!ValidationUtils.isValidName(firstName)) {
        ValidationUtils.showFieldError('addUserFirstName', 'First name must contain only letters, spaces, and hyphens (2-50 characters)');
        isValid = false;
    }
    
    // Last Name
    const lastName = document.getElementById('addUserLastName').value.trim();
    if (!lastName) {
        ValidationUtils.showFieldError('addUserLastName', 'Last name is required');
        isValid = false;
    } else if (!ValidationUtils.isValidName(lastName)) {
        ValidationUtils.showFieldError('addUserLastName', 'Last name must contain only letters, spaces, and hyphens (2-50 characters)');
        isValid = false;
    }
    
    // Username
    const username = document.getElementById('addUserUsername').value.trim();
    if (!username) {
        ValidationUtils.showFieldError('addUserUsername', 'Username is required');
        isValid = false;
    } else if (!ValidationUtils.isValidUsername(username)) {
        ValidationUtils.showFieldError('addUserUsername', 'Username must be 3-20 characters (letters, numbers, underscore only)');
        isValid = false;
    }
    
    // Email
    const email = document.getElementById('addUserEmail').value.trim();
    if (!email) {
        ValidationUtils.showFieldError('addUserEmail', 'Email is required');
        isValid = false;
    } else if (!ValidationUtils.isValidEmail(email)) {
        ValidationUtils.showFieldError('addUserEmail', 'Please enter a valid email address');
        isValid = false;
    }
    
    // Phone (optional but validate if provided)
    const phone = document.getElementById('addUserPhone').value.trim();
    if (phone && !ValidationUtils.isValidPhone(phone)) {
        ValidationUtils.showFieldError('addUserPhone', 'Please enter a valid phone number (at least 10 digits)');
        isValid = false;
    }
    
    // Password
    const password = document.getElementById('addUserPassword').value;
    if (!password) {
        ValidationUtils.showFieldError('addUserPassword', 'Password is required');
        isValid = false;
    } else if (!ValidationUtils.isValidPassword(password)) {
        ValidationUtils.showFieldError('addUserPassword', 'Password must be at least 8 characters with at least one letter and one number');
        isValid = false;
    }
    
    // Role
    const role = document.getElementById('addUserRole').value;
    if (!role) {
        ValidationUtils.showFieldError('addUserRole', 'Please select a role');
        isValid = false;
    }
    
    return isValid;
}

// Validate Edit User Form
function validateEditUserForm() {
    ValidationUtils.clearFormErrors('editUserForm');
    let isValid = true;
    
    // First Name
    const firstName = document.getElementById('editUserFirstName').value.trim();
    if (!firstName) {
        ValidationUtils.showFieldError('editUserFirstName', 'First name is required');
        isValid = false;
    } else if (!ValidationUtils.isValidName(firstName)) {
        ValidationUtils.showFieldError('editUserFirstName', 'First name must contain only letters, spaces, and hyphens (2-50 characters)');
        isValid = false;
    }
    
    // Last Name
    const lastName = document.getElementById('editUserLastName').value.trim();
    if (!lastName) {
        ValidationUtils.showFieldError('editUserLastName', 'Last name is required');
        isValid = false;
    } else if (!ValidationUtils.isValidName(lastName)) {
        ValidationUtils.showFieldError('editUserLastName', 'Last name must contain only letters, spaces, and hyphens (2-50 characters)');
        isValid = false;
    }
    
    // Username
    const username = document.getElementById('editUserUsername').value.trim();
    if (!username) {
        ValidationUtils.showFieldError('editUserUsername', 'Username is required');
        isValid = false;
    } else if (!ValidationUtils.isValidUsername(username)) {
        ValidationUtils.showFieldError('editUserUsername', 'Username must be 3-20 characters (letters, numbers, underscore only)');
        isValid = false;
    }
    
    // Email
    const email = document.getElementById('editUserEmail').value.trim();
    if (!email) {
        ValidationUtils.showFieldError('editUserEmail', 'Email is required');
        isValid = false;
    } else if (!ValidationUtils.isValidEmail(email)) {
        ValidationUtils.showFieldError('editUserEmail', 'Please enter a valid email address');
        isValid = false;
    }
    
    // Phone (optional but validate if provided)
    const phone = document.getElementById('editUserPhone').value.trim();
    if (phone && !ValidationUtils.isValidPhone(phone)) {
        ValidationUtils.showFieldError('editUserPhone', 'Please enter a valid phone number (at least 10 digits)');
        isValid = false;
    }
    
    // Password (optional for edit, but validate if provided)
    const password = document.getElementById('editUserPassword').value;
    if (password && !ValidationUtils.isValidPassword(password)) {
        ValidationUtils.showFieldError('editUserPassword', 'Password must be at least 8 characters with at least one letter and one number');
        isValid = false;
    }
    
    return isValid;
}

// Validate Add Room Form
function validateAddRoomForm() {
    ValidationUtils.clearFormErrors('addRoomForm');
    let isValid = true;
    
    // Get form fields by name attribute
    const form = document.getElementById('addRoomForm');
    const roomNumber = form.querySelector('[name="roomNumber"]').value.trim();
    const floorNumber = form.querySelector('[name="floorNumber"]').value;
    const capacity = form.querySelector('[name="capacity"]').value;
    const basePrice = form.querySelector('[name="basePrice"]').value;
    const roomType = form.querySelector('[name="roomType"]').value;
    
    // Room Number
    if (!roomNumber) {
        showAlert('Room number is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isValidRoomNumber(roomNumber)) {
        showAlert('Room number must be alphanumeric (1-10 characters)', 'danger');
        isValid = false;
    }
    
    // Floor Number
    if (!floorNumber) {
        showAlert('Floor number is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveInteger(floorNumber)) {
        showAlert('Floor number must be a positive integer', 'danger');
        isValid = false;
    } else if (parseInt(floorNumber) > 50) {
        showAlert('Floor number cannot exceed 50', 'danger');
        isValid = false;
    }
    
    // Capacity
    if (!capacity) {
        showAlert('Capacity is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveInteger(capacity)) {
        showAlert('Capacity must be a positive integer', 'danger');
        isValid = false;
    } else if (parseInt(capacity) > 20) {
        showAlert('Capacity cannot exceed 20 guests', 'danger');
        isValid = false;
    }
    
    // Base Price
    if (!basePrice) {
        showAlert('Base price is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveNumber(basePrice)) {
        showAlert('Base price must be a positive number', 'danger');
        isValid = false;
    } else if (parseFloat(basePrice) > 1000000) {
        showAlert('Base price seems unreasonably high', 'danger');
        isValid = false;
    }
    
    // Room Type
    if (!roomType) {
        showAlert('Please select a room type', 'danger');
        isValid = false;
    }
    
    // Validate image files if any are selected
    const photoInputs = ['roomPhoto1', 'roomPhoto2', 'roomPhoto3', 'roomPhoto4'];
    for (const inputId of photoInputs) {
        const input = document.getElementById(inputId);
        if (input && input.files.length > 0) {
            const file = input.files[0];
            
            // Check file type
            if (!file.type.startsWith('image/')) {
                showAlert(`${inputId}: Please select a valid image file`, 'danger');
                isValid = false;
            }
            
            // Check file size (max 5MB)
            if (file.size > 5 * 1024 * 1024) {
                showAlert(`${inputId}: Image size must be less than 5MB`, 'danger');
                isValid = false;
            }
        }
    }
    
    return isValid;
}

// Validate Add Event Space Form
function validateAddEventSpaceForm() {
    ValidationUtils.clearFormErrors('addEventSpaceForm');
    let isValid = true;
    
    // Get form fields by name attribute
    const form = document.getElementById('addEventSpaceForm');
    const name = form.querySelector('[name="name"]').value.trim();
    const capacity = form.querySelector('[name="capacity"]').value;
    const basePrice = form.querySelector('[name="basePrice"]').value;
    const floorNumber = form.querySelector('[name="floorNumber"]').value;
    
    // Name
    if (!name) {
        showAlert('Event space name is required', 'danger');
        isValid = false;
    } else if (name.length < 3 || name.length > 100) {
        showAlert('Event space name must be between 3 and 100 characters', 'danger');
        isValid = false;
    }
    
    // Capacity
    if (!capacity) {
        showAlert('Capacity is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveInteger(capacity)) {
        showAlert('Capacity must be a positive integer', 'danger');
        isValid = false;
    } else if (parseInt(capacity) > 5000) {
        showAlert('Capacity cannot exceed 5000 people', 'danger');
        isValid = false;
    }
    
    // Base Price
    if (!basePrice) {
        showAlert('Base price is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveNumber(basePrice)) {
        showAlert('Base price must be a positive number', 'danger');
        isValid = false;
    } else if (parseFloat(basePrice) > 10000000) {
        showAlert('Base price seems unreasonably high', 'danger');
        isValid = false;
    }
    
    // Floor Number
    if (!floorNumber) {
        showAlert('Floor number is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveInteger(floorNumber)) {
        showAlert('Floor number must be a positive integer', 'danger');
        isValid = false;
    } else if (parseInt(floorNumber) > 50) {
        showAlert('Floor number cannot exceed 50', 'danger');
        isValid = false;
    }
    
    // Validate image files if any are selected
    const photoInputs = ['eventSpacePhoto1', 'eventSpacePhoto2', 'eventSpacePhoto3', 'eventSpacePhoto4'];
    for (const inputId of photoInputs) {
        const input = document.getElementById(inputId);
        if (input && input.files.length > 0) {
            const file = input.files[0];
            
            // Check file type
            if (!file.type.startsWith('image/')) {
                showAlert(`${inputId}: Please select a valid image file`, 'danger');
                isValid = false;
            }
            
            // Check file size (max 5MB)
            if (file.size > 5 * 1024 * 1024) {
                showAlert(`${inputId}: Image size must be less than 5MB`, 'danger');
                isValid = false;
            }
        }
    }
    
    return isValid;
}

// Validate Room Update Form
function validateRoomUpdateForm() {
    ValidationUtils.clearFormErrors('roomUpdateForm');
    let isValid = true;
    
    const form = document.getElementById('roomUpdateForm');
    const roomNumber = form.querySelector('[name="roomNumber"]').value.trim();
    const floorNumber = form.querySelector('[name="floorNumber"]').value;
    const capacity = form.querySelector('[name="capacity"]').value;
    const basePrice = form.querySelector('[name="basePrice"]').value;
    
    // Room Number
    if (!roomNumber) {
        showAlert('Room number is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isValidRoomNumber(roomNumber)) {
        showAlert('Room number must be alphanumeric (1-10 characters)', 'danger');
        isValid = false;
    }
    
    // Floor Number
    if (!floorNumber) {
        showAlert('Floor number is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveInteger(floorNumber)) {
        showAlert('Floor number must be a positive integer', 'danger');
        isValid = false;
    }
    
    // Capacity
    if (!capacity) {
        showAlert('Capacity is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveInteger(capacity)) {
        showAlert('Capacity must be a positive integer', 'danger');
        isValid = false;
    }
    
    // Base Price
    if (!basePrice) {
        showAlert('Base price is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveNumber(basePrice)) {
        showAlert('Base price must be a positive number', 'danger');
        isValid = false;
    }
    
    return isValid;
}

// Validate Event Space Update Form
function validateEventSpaceUpdateForm() {
    ValidationUtils.clearFormErrors('eventSpaceUpdateForm');
    let isValid = true;
    
    const form = document.getElementById('eventSpaceUpdateForm');
    const name = form.querySelector('[name="name"]').value.trim();
    const capacity = form.querySelector('[name="capacity"]').value;
    const basePrice = form.querySelector('[name="basePrice"]').value;
    const floorNumber = form.querySelector('[name="floorNumber"]').value;
    
    // Name
    if (!name) {
        showAlert('Event space name is required', 'danger');
        isValid = false;
    } else if (name.length < 3 || name.length > 100) {
        showAlert('Event space name must be between 3 and 100 characters', 'danger');
        isValid = false;
    }
    
    // Capacity
    if (!capacity) {
        showAlert('Capacity is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveInteger(capacity)) {
        showAlert('Capacity must be a positive integer', 'danger');
        isValid = false;
    }
    
    // Base Price
    if (!basePrice) {
        showAlert('Base price is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveNumber(basePrice)) {
        showAlert('Base price must be a positive number', 'danger');
        isValid = false;
    }
    
    // Floor Number
    if (!floorNumber) {
        showAlert('Floor number is required', 'danger');
        isValid = false;
    } else if (!ValidationUtils.isPositiveInteger(floorNumber)) {
        showAlert('Floor number must be a positive integer', 'danger');
        isValid = false;
    }
    
    return isValid;
}