// Fundoo Notes AngularJS Application Module
const app = angular.module('fundooApp', []);

app.controller('FundooController', ['$scope', '$http', '$timeout', '$window', function($scope, $http, $timeout, $window) {
    const API_BASE = '/api/v1';

    // COLOR PALETTE MAP
    $scope.COLOR_MAP = {
        white: '#ffffff',
        blue: '#e8f0fe',
        green: '#e6f4ea',
        yellow: '#fef7e0',
        red: '#fce8e6',
        purple: '#f3e8fd',
        orange: '#feefe3',
        pink: '#ffebf3'
    };

    // --- APPLICATION STATE ---
    $scope.token = $window.localStorage.getItem('fundoo_token') || null;
    try {
        const storedUser = $window.localStorage.getItem('fundoo_user');
        $scope.user = (storedUser && storedUser !== 'undefined') ? JSON.parse(storedUser) : null;
    } catch (e) {
        $scope.user = null;
        console.error('Failed to parse cached user data:', e);
    }
    $scope.notes = [];
    $scope.labels = [];
    $scope.currentView = 'notes'; // 'notes', 'archive', 'trash', or labelId (number)
    $scope.searchQuery = '';

    // Auth screen sub-tabs: 'login', 'register', 'forgot', 'reset'
    $scope.authTab = 'login';
    $scope.showActivationHelper = false;
    $scope.activationToken = '';

    // Inputs Models
    $scope.login = { email: '', password: '' };
    $scope.reg = { firstName: '', lastName: '', email: '', mobileNumber: '', password: '' };
    $scope.forgot = { email: '' };
    $scope.reset = { token: '', newPassword: '' };

    // Workspace settings
    $scope.sidebarCollapsed = false;
    $scope.creatorExpanded = false;
    $scope.creatorColor = 'white';
    $scope.newNote = { title: '', description: '' };

    // Modals templates
    $scope.editingNote = null;
    $scope.editingNoteColor = 'white';
    $scope.showingLabelDropdown = false;
    $scope.showingLabelsModal = false;
    $scope.newLabelName = '';

    $scope.showingCollaboratorsModal = false;
    $scope.collaboratingNoteId = null;
    $scope.collaborators = [];
    $scope.newCollab = { email: '', role: 'VIEWER' };

    // --- INITIALIZATION ---
    $scope.init = function() {
        if ($scope.token) {
            $scope.fetchLabels();
            $scope.refreshNotes();
        }
    };

    // Helper to build headers
    function getHeaders() {
        const headers = {
            'Content-Type': 'application/json'
        };
        if ($scope.token) {
            headers['Authorization'] = `Bearer ${$scope.token}`;
        }
        return headers;
    }

    // Dynamic hex mapper
    $scope.getHexColor = function(colorName) {
        return $scope.COLOR_MAP[colorName || 'white'] || $scope.COLOR_MAP.white;
    };

    // Custom alerts toast
    function showToast(message, type = 'success') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <i class="${type === 'success' ? 'fa-regular fa-circle-check' : 'fa-solid fa-circle-exclamation'}"></i>
            <span>${message}</span>
        `;
        container.appendChild(toast);
        
        $timeout(() => {
            toast.style.animation = 'fadeOut 0.3s ease-in forwards';
            $timeout(() => toast.remove(), 300);
        }, 4000);
    }

    // ----------------- AUTHENTICATION FLOWS -----------------
    $scope.switchAuthTab = function(tab) {
        $scope.authTab = tab;
        $scope.showActivationHelper = false;
    };

    $scope.handleLogin = function() {
        $http({
            method: 'POST',
            url: `${API_BASE}/users/login`,
            headers: getHeaders(),
            data: $scope.login
        }).then(function(response) {
            const result = response.data;
            if (result.success) {
                $scope.token = result.data.token;
                $scope.user = result.data.user;
                $window.localStorage.setItem('fundoo_token', $scope.token);
                $window.localStorage.setItem('fundoo_user', JSON.stringify($scope.user));
                
                showToast('Login successful!');
                $scope.login = { email: '', password: '' };
                $scope.fetchLabels();
                $scope.refreshNotes();
            } else {
                showToast(result.message || 'Login failed', 'error');
            }
        }, function(error) {
            const msg = (error.data && error.data.message) ? error.data.message : 'Invalid credentials or server offline.';
            showToast(msg, 'error');
        });
    };

    $scope.handleRegister = function() {
        $http({
            method: 'POST',
            url: `${API_BASE}/users/register`,
            headers: getHeaders(),
            data: $scope.reg
        }).then(function(response) {
            const result = response.data;
            if (result.success) {
                showToast('Registration successful! Please verify your email.');
                $scope.showActivationHelper = true;
                $scope.activationToken = '';
                $scope.reg = { firstName: '', lastName: '', email: '', mobileNumber: '', password: '' };
            } else {
                showToast(result.message || 'Registration failed', 'error');
            }
        }, function(error) {
            const msg = (error.data && error.data.message) ? error.data.message : 'Registration failed.';
            showToast(msg, 'error');
        });
    };

    $scope.handleVerifyInline = function() {
        const cleanToken = ($scope.activationToken || '').replace(/['"]+/g, '').trim();
        if (!cleanToken) {
            showToast('Please paste a token', 'error');
            return;
        }

        $http({
            method: 'GET',
            url: `${API_BASE}/users/verify-email?token=${encodeURIComponent(cleanToken)}`,
            headers: getHeaders()
        }).then(function(response) {
            const result = response.data;
            if (result.success) {
                showToast('Account activated! You can now sign in.');
                $scope.showActivationHelper = false;
                $scope.switchAuthTab('login');
            } else {
                showToast(result.message || 'Verification failed', 'error');
            }
        }, function(error) {
            const msg = (error.data && error.data.message) ? error.data.message : 'Activation failed.';
            showToast(msg, 'error');
        });
    };

    $scope.handleForgotPassword = function() {
        $http({
            method: 'POST',
            url: `${API_BASE}/users/forgot-password`,
            headers: getHeaders(),
            data: $scope.forgot
        }).then(function(response) {
            const result = response.data;
            if (result.success) {
                showToast('Reset password link and token sent! Please check your email.');
                $scope.forgot = { email: '' };
                $scope.switchAuthTab('reset');
            } else {
                showToast(result.message || 'Failed to send request', 'error');
            }
        }, function(error) {
            const msg = (error.data && error.data.message) ? error.data.message : 'Forgot password request failed.';
            showToast(msg, 'error');
        });
    };

    $scope.handleResetPassword = function() {
        $http({
            method: 'POST',
            url: `${API_BASE}/users/reset-password`,
            headers: getHeaders(),
            data: $scope.reset
        }).then(function(response) {
            const result = response.data;
            if (result.success) {
                showToast('Password reset successfully! Log in now.');
                $scope.reset = { token: '', newPassword: '' };
                $scope.switchAuthTab('login');
            } else {
                showToast(result.message || 'Reset password failed', 'error');
            }
        }, function(error) {
            const msg = (error.data && error.data.message) ? error.data.message : 'Password reset failed.';
            showToast(msg, 'error');
        });
    };

    $scope.handleLogout = function() {
        $scope.token = null;
        $scope.user = null;
        $window.localStorage.removeItem('fundoo_token');
        $window.localStorage.removeItem('fundoo_user');
        $scope.notes = [];
        $scope.labels = [];
        
        $scope.login = { email: '', password: '' };
        showToast('Logged out successfully.');
        $scope.switchAuthTab('login');
    };

    // ----------------- SIDEBAR AND NAVIGATION -----------------
    $scope.toggleSidebar = function() {
        $scope.sidebarCollapsed = !$scope.sidebarCollapsed;
    };

    $scope.switchNav = function(view) {
        $scope.currentView = view;
        // Keep search empty when navigating folders
        $scope.searchQuery = '';
        
        $scope.refreshNotes();
    };

    // ----------------- NOTE CREATOR FUNCTIONS -----------------
    $scope.expandCreator = function() {
        $scope.creatorExpanded = true;
        $scope.creatorColor = 'white';
    };

    $scope.selectCreatorColor = function(colorName) {
        $scope.creatorColor = colorName;
    };

    $scope.collapseCreator = function(save = false) {
        if (save) {
            $scope.handleCreateNote();
        }
        $scope.newNote = { title: '', description: '' };
        $scope.creatorExpanded = false;
    };

    $scope.autoGrowTextarea = function(event) {
        const el = event.target;
        el.style.height = 'auto';
        el.style.height = el.scrollHeight + 'px';
    };

    // ----------------- FETCH NOTES & RENDER FILTERS -----------------
    $scope.refreshNotes = function() {
        let url = `${API_BASE}/notes?size=100`;

        if ($scope.searchQuery) {
            url = `${API_BASE}/notes/search?query=${encodeURIComponent($scope.searchQuery)}&size=100`;
        } else if (typeof $scope.currentView === 'number') {
            url = `${API_BASE}/labels/${$scope.currentView}/notes?size=100`;
        } else if ($scope.currentView === 'archive') {
            url += `&archived=true&trashed=false`;
        } else if ($scope.currentView === 'trash') {
            url += `&archived=false&trashed=true`;
        } else {
            url += `&archived=false&trashed=false`;
        }

        $http({
            method: 'GET',
            url: url,
            headers: getHeaders()
        }).then(function(response) {
            const result = response.data;
            if (result.success) {
                $scope.notes = result.data.content || [];
            }
        }, function(error) {
            console.error('Fetch notes error:', error);
        });
    };

    // Note lists sorting filters
    $scope.hasPinnedNotes = function() {
        return $scope.notes.some(n => n.pinned && !n.archived && !n.trashed);
    };

    $scope.noteFilter = function(note) {
        if ($scope.searchQuery) {
            return true; // Search matches are pre-filtered by API
        }
        
        if (typeof $scope.currentView === 'number') {
            // Label-based active view
            return !note.trashed;
        }

        if ($scope.currentView === 'archive') {
            return note.archived && !note.trashed;
        }

        if ($scope.currentView === 'trash') {
            return note.trashed;
        }

        // Default 'notes' view: show non-pinned, active, non-archived, non-trashed notes
        return !note.pinned && !note.archived && !note.trashed;
    };

    // Header Search input with debouncer
    let searchDebounceTimeout = null;
    $scope.handleSearch = function() {
        if (searchDebounceTimeout) $timeout.cancel(searchDebounceTimeout);
        searchDebounceTimeout = $timeout(function() {
            $scope.refreshNotes();
        }, 300);
    };

    // Dynamic details for empty notes views
    $scope.isEmptyState = function() {
        // Evaluate based on currentView and notes length
        if (typeof $scope.currentView === 'number') {
            return !$scope.notes || $scope.notes.length === 0;
        }
        
        if ($scope.currentView === 'notes') {
            return !$scope.notes.some(n => !n.archived && !n.trashed);
        }

        if ($scope.currentView === 'archive') {
            return !$scope.notes.some(n => n.archived && !n.trashed);
        }

        if ($scope.currentView === 'trash') {
            return !$scope.notes.some(n => n.trashed);
        }
        
        return $scope.notes.length === 0;
    };

    $scope.getEmptyStateIcon = function() {
        if ($scope.currentView === 'archive') return 'fa-box-archive';
        if ($scope.currentView === 'trash') return 'fa-trash-can';
        if ($scope.searchQuery) return 'fa-magnifying-glass';
        return 'fa-lightbulb';
    };

    $scope.getEmptyStateTitle = function() {
        if ($scope.currentView === 'archive') return 'Your archived notes appear here';
        if ($scope.currentView === 'trash') return 'No notes in Trash';
        if ($scope.searchQuery) return 'No matching notes found';
        return 'Your notes will appear here';
    };

    $scope.getEmptyStateDesc = function() {
        if ($scope.currentView === 'archive' || $scope.currentView === 'trash') return '';
        if ($scope.searchQuery) return 'Try adjusting your search terms';
        return 'Start creating notes to organize your life!';
    };

    $scope.getSectionTitle = function() {
        if (typeof $scope.currentView === 'number') {
            const labelObj = $scope.labels.find(l => l.id === $scope.currentView);
            return labelObj ? labelObj.name.toUpperCase() : 'LABEL';
        }
        if ($scope.currentView === 'archive') return 'ARCHIVE';
        if ($scope.currentView === 'trash') return 'TRASH';
        
        // Notes title switches if pinned cards exist
        return $scope.hasPinnedNotes() ? 'OTHERS' : 'NOTES';
    };

    // ----------------- NOTE OPERATIONS (CRUD) -----------------
    $scope.handleCreateNote = function() {
        if (!$scope.newNote.title && !$scope.newNote.description) return;

        $http({
            method: 'POST',
            url: `${API_BASE}/notes`,
            headers: getHeaders(),
            data: {
                title: $scope.newNote.title,
                description: $scope.newNote.description,
                color: $scope.creatorColor,
                pinned: false,
                archived: false,
                trashed: false
            }
        }).then(function(response) {
            if (response.data.success) {
                $scope.refreshNotes();
            }
        });
    };

    $scope.toggleNotePin = function(note) {
        $http({
            method: 'PATCH',
            url: `${API_BASE}/notes/${note.id}/pin`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                $scope.refreshNotes();
            }
        });
    };

    $scope.toggleNoteArchive = function(note) {
        $http({
            method: 'PATCH',
            url: `${API_BASE}/notes/${note.id}/archive`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                showToast(response.data.data.archived ? 'Note archived' : 'Note unarchived');
                $scope.refreshNotes();
            }
        });
    };

    $scope.toggleNoteTrash = function(note) {
        $http({
            method: 'PATCH',
            url: `${API_BASE}/notes/${note.id}/trash`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                showToast(response.data.data.trashed ? 'Note moved to trash' : 'Note restored');
                $scope.refreshNotes();
            }
        });
    };

    $scope.updateNoteColor = function(noteId, colorName) {
        $http({
            method: 'PATCH',
            url: `${API_BASE}/notes/${noteId}/color?color=${colorName}`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                $scope.refreshNotes();
            }
        });
    };

    $scope.deletePermanently = function(noteId) {
        if (!$window.confirm('Delete this note permanently? This action cannot be undone.')) return;

        $http({
            method: 'DELETE',
            url: `${API_BASE}/notes/${noteId}`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                showToast('Note permanently deleted');
                $scope.refreshNotes();
            }
        });
    };

    // ----------------- NOTE DETAIL EDIT MODAL -----------------
    $scope.openEditNoteModal = function(note) {
        if (note.trashed) return;

        $scope.editingNote = angular.copy(note);
        $scope.editingNoteColor = note.color || 'white';
        $scope.showingLabelDropdown = false;
    };

    $scope.selectEditColor = function(colorName) {
        $scope.editingNoteColor = colorName;
    };

    $scope.saveEditNote = function() {
        if (!$scope.editingNote) return;

        $http({
            method: 'PUT',
            url: `${API_BASE}/notes/${$scope.editingNote.id}`,
            headers: getHeaders(),
            data: {
                title: $scope.editingNote.title,
                description: $scope.editingNote.description,
                color: $scope.editingNoteColor,
                pinned: $scope.editingNote.pinned,
                archived: $scope.editingNote.archived,
                trashed: $scope.editingNote.trashed
            }
        }).then(function(response) {
            if (response.data.success) {
                $scope.closeEditNoteModal();
                $scope.refreshNotes();
            } else {
                showToast('Failed to save changes', 'error');
            }
        });
    };

    $scope.closeEditNoteModal = function() {
        $scope.editingNote = null;
    };

    $scope.handleDeleteFromEdit = function() {
        if ($scope.editingNote) {
            $scope.toggleNoteTrash($scope.editingNote);
            $scope.closeEditNoteModal();
        }
    };

    // ----------------- LABELS MANAGEMENT -----------------
    $scope.fetchLabels = function() {
        $http({
            method: 'GET',
            url: `${API_BASE}/labels`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                $scope.labels = response.data.data || [];
            }
        });
    };

    $scope.openLabelsModal = function() {
        $scope.newLabelName = '';
        $scope.showingLabelsModal = true;
    };

    $scope.closeLabelsModal = function() {
        $scope.showingLabelsModal = false;
        $scope.fetchLabels();
        $scope.refreshNotes();
    };

    $scope.handleCreateLabel = function() {
        if (!$scope.newLabelName.trim()) return;

        $http({
            method: 'POST',
            url: `${API_BASE}/labels`,
            headers: getHeaders(),
            data: { name: $scope.newLabelName }
        }).then(function(response) {
            if (response.data.success) {
                $scope.newLabelName = '';
                $scope.labels.push(response.data.data);
            }
        });
    };

    $scope.handleUpdateLabelName = function(label) {
        if (!label.name.trim()) return;

        $http({
            method: 'PUT',
            url: `${API_BASE}/labels/${label.id}`,
            headers: getHeaders(),
            data: { name: label.name }
        }).then(function(response) {
            if (!response.data.success) {
                showToast('Failed to update label', 'error');
            }
        });
    };

    $scope.handleDeleteLabel = function(labelId) {
        if (!$window.confirm('Delete this label? It will be detached from all notes.')) return;

        $http({
            method: 'DELETE',
            url: `${API_BASE}/labels/${labelId}`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                $scope.labels = $scope.labels.filter(l => l.id !== labelId);
                if ($scope.currentView === labelId) {
                    $scope.switchNav('notes');
                }
            }
        });
    };

    // ----------------- ASSOCIATE LABELS (IN EDIT MODAL) -----------------
    $scope.toggleLabelDropdown = function() {
        $scope.showingLabelDropdown = !$scope.showingLabelDropdown;
    };

    $scope.isLabelAttached = function(labelId) {
        if (!$scope.editingNote || !$scope.editingNote.labels) return false;
        return $scope.editingNote.labels.some(l => l.id === labelId);
    };

    $scope.handleLabelToggle = function(labelId, isChecked) {
        const method = isChecked ? 'POST' : 'DELETE';
        const noteId = $scope.editingNote.id;

        $http({
            method: method,
            url: `${API_BASE}/notes/${noteId}/labels/${labelId}`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                $scope.editingNote = response.data.data;
            }
        });
    };

    $scope.detachLabelFromEdit = function(labelId) {
        $scope.handleLabelToggle(labelId, false);
    };

    // ----------------- COLLABORATORS MANAGEMENT -----------------
    $scope.openCollaboratorsModal = function(noteId) {
        $scope.collaboratingNoteId = noteId;
        $scope.newCollab = { email: '', role: 'VIEWER' };
        
        // Find local note for details
        const note = $scope.notes.find(n => n.id === noteId) || $scope.editingNote;
        
        // Set modal owner displays
        document.getElementById('collab-owner-name').textContent = $scope.user ? `${$scope.user.firstName} ${$scope.user.lastName} (Owner)` : 'Owner';
        document.getElementById('collab-owner-email').textContent = $scope.user ? $scope.user.email : '';

        $scope.fetchCollaborators(noteId);
        $scope.showingCollaboratorsModal = true;
    };

    $scope.openCollaboratorsModalFromEdit = function() {
        if ($scope.editingNote) {
            $scope.openCollaboratorsModal($scope.editingNote.id);
        }
    };

    $scope.closeCollaboratorsModal = function() {
        $scope.showingCollaboratorsModal = false;
        $scope.collaboratingNoteId = null;

        // If edit modal is active, refresh the editingNote state
        if ($scope.editingNote) {
            $http({
                method: 'GET',
                url: `${API_BASE}/notes/${$scope.editingNote.id}`,
                headers: getHeaders()
            }).then(function(response) {
                if (response.data.success) {
                    $scope.editingNote = response.data.data;
                }
            });
        } else {
            $scope.refreshNotes();
        }
    };

    $scope.fetchCollaborators = function(noteId) {
        $http({
            method: 'GET',
            url: `${API_BASE}/notes/${noteId}/collaborators`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                $scope.collaborators = response.data.data || [];
            }
        });
    };

    $scope.handleAddCollaborator = function() {
        const noteId = $scope.collaboratingNoteId;
        if (!$scope.newCollab.email.trim()) {
            showToast('Please provide an email address', 'error');
            return;
        }

        $http({
            method: 'POST',
            url: `${API_BASE}/notes/${noteId}/collaborators`,
            headers: getHeaders(),
            data: $scope.newCollab
        }).then(function(response) {
            if (response.data.success) {
                showToast('Collaborator added successfully!');
                $scope.newCollab = { email: '', role: 'VIEWER' };
                $scope.fetchCollaborators(noteId);
            } else {
                showToast(response.data.message || 'Failed to add collaborator', 'error');
            }
        }, function(error) {
            const msg = (error.data && error.data.message) ? error.data.message : 'Failed to add collaborator';
            showToast(msg, 'error');
        });
    };

    $scope.handleRemoveCollaborator = function(collabRecordId) {
        const noteId = $scope.collaboratingNoteId;
        if (!$window.confirm('Remove access for this collaborator?')) return;

        $http({
            method: 'DELETE',
            url: `${API_BASE}/notes/${noteId}/collaborators/${collabRecordId}`,
            headers: getHeaders()
        }).then(function(response) {
            if (response.data.success) {
                showToast('Collaborator removed successfully');
                $scope.fetchCollaborators(noteId);
            } else {
                showToast(response.data.message || 'Failed to remove collaborator', 'error');
            }
        });
    };

    // Helper to click outside overlays to trigger save/close
    $scope.handleOverlayClick = function(event, modalId) {
        if (event.target.id === modalId) {
            if (modalId === 'edit-note-modal') {
                $scope.saveEditNote();
            } else if (modalId === 'labels-modal') {
                $scope.closeLabelsModal();
            } else if (modalId === 'collaborators-modal') {
                $scope.closeCollaboratorsModal();
            }
        }
    };

    // Run startup
    $scope.init();
}]);
