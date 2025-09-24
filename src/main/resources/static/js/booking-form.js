/**
 * Booking Form Component
 * Handles the booking form UI and submission
 */

class BookingForm {
    constructor() {
        this.form = document.getElementById('bookingForm');
        this.isGuest = false;
        this.initEventListeners();
    }

    initEventListeners() {
        // Toggle guest information fields
        const bookingForSelf = document.getElementById('bookingForSelf');
        const bookingForGuest = document.getElementById('bookingForGuest');
        const guestInfoSection = document.getElementById('guestInfoSection');

        if (bookingForSelf && bookingForGuest) {
            bookingForSelf.addEventListener('change', () => {
                this.isGuest = false;
                guestInfoSection.classList.add('d-none');
                this.clearGuestFields();
                this.setGuestFieldRequired(false);
            });

            bookingForGuest.addEventListener('change', () => {
                this.isGuest = true;
                guestInfoSection.classList.remove('d-none');
                this.setGuestFieldRequired(true);
            });
        }

        // Form submission
        if (this.form) {
            this.form.addEventListener('submit', (e) => this.handleSubmit(e));
        }

        // Date validation
        const checkInInput = document.getElementById('checkInDate');
        const checkOutInput = document.getElementById('checkOutDate');
        
        if (checkInInput && checkOutInput) {
            checkInInput.addEventListener('change', () => this.validateDates());
            checkOutInput.addEventListener('change', () => this.validateDates());
        }
    }

    setGuestFieldRequired(required) {
        const guestFields = [
            'guestFirstName',
            'guestLastName',
            'guestEmail',
            'guestPhone'
        ];

        guestFields.forEach(fieldId => {
            const field = document.getElementById(fieldId);
            if (field) {
                field.required = required;
            }
        });
    }

    clearGuestFields() {
        const guestFields = [
            'guestFirstName',
            'guestLastName',
            'guestEmail',
            'guestPhone',
            'specialRequests'
        ];

        guestFields.forEach(fieldId => {
            const field = document.getElementById(fieldId);
            if (field) {
                field.value = '';
            }
        });
    }

    validateDates() {
        const checkInDate = new Date(document.getElementById('checkInDate').value);
        const checkOutDate = new Date(document.getElementById('checkOutDate').value);
        
        if (checkInDate && checkOutDate && checkInDate >= checkOutDate) {
            alert('Check-out date must be after check-in date');
            document.getElementById('checkOutDate').value = '';
            return false;
        }
        return true;
    }

    async handleSubmit(e) {
        e.preventDefault();
        
        if (!this.validateDates()) {
            return;
        }

        const formData = new FormData(this.form);
        const bookingData = {
            roomId: parseInt(formData.get('roomId')),
            checkInDate: formData.get('checkInDate'),
            checkOutDate: formData.get('checkOutDate'),
            guestCount: parseInt(formData.get('guestCount')),
            specialRequests: formData.get('specialRequests'),
            requireAirportPickup: formData.get('requireAirportPickup') === 'on',
            flightNumber: formData.get('flightNumber') || null,
            specialAccommodations: formData.get('specialAccommodations') || null
        };

        // Add guest information if booking for someone else
        if (this.isGuest) {
            bookingData.guestEmail = formData.get('guestEmail');
            bookingData.guestFirstName = formData.get('guestFirstName');
            bookingData.guestLastName = formData.get('guestLastName');
            bookingData.guestPhone = formData.get('guestPhone');
        }

        try {
            const response = await fetch('/api/bookings', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                },
                body: JSON.stringify(bookingData)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Failed to create booking');
            }

            const result = await response.json();
            this.showSuccessMessage('Booking created successfully!');
            // Redirect to booking confirmation page
            window.location.href = `/booking-confirmation.html?bookingId=${result.bookingId}`;
        } catch (error) {
            console.error('Error creating booking:', error);
            this.showError(error.message || 'Failed to create booking. Please try again.');
        }
    }

    showSuccessMessage(message) {
        this.showAlert(message, 'success');
    }

    showError(message) {
        this.showAlert(message, 'danger');
    }

    showAlert(message, type) {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
        alertDiv.role = 'alert';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        
        const container = document.querySelector('.container');
        container.insertBefore(alertDiv, container.firstChild);
        
        // Auto-remove alert after 5 seconds
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }
}

// Initialize the booking form when the DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new BookingForm();
});
