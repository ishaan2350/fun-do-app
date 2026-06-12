// Fundoo Notes SPA Client Script
const API_BASE = '/api/v1';

// Application State
let state = {
    token: localStorage.getItem('fundoo_token') || null,
    user: JSON.parse(localStorage.getItem('fundoo_user')) || null,
    notes: [],
    labels: [],
    currentView: 'notes', // 'notes', 'archive', 'trash', or labelId (number)
    searchQuery: '',
    
    // Note Creator state
    creatorColor: 'white',
    
    // Note Editor Modal state
    editingNote: null,
    editingNoteColor: 'white',
    
    // Collaborators Modal state
    collaboratingNoteId: null
};

// Colors mapping matching the CSS variables
const COLOR_MAP = {
    white: '#ffffff',
    blue: '#e8f0fe',
    green: '#e6f4ea',
    yellow: '#fef7e0',
    red: '#fce8e6',
    purple: '#f3e8fd',
    orange: '#feefe3',
    pink: '#ffebf3'
};

// Initial setup
document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

function initApp() {
    if (state.token) {
        showDashboard();
        fetchLabels();
        refreshNotes();
    } else {
        showAuth();
    }
}

// REST Headers Builder
function getHeaders() {
    const headers = {
        'Content-Type': 'application/json'
    };
    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }
    return headers;
}

// Toast Notification
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <i class="${type === 'success' ? 'fa-regular fa-circle-check' : 'fa-solid fa-circle-exclamation'}"></i>
        <span>${message}</span>
    `;
    container.appendChild(toast);
    
    // Auto-remove after 4 seconds
    setTimeout(() => {
        toast.style.animation = 'fadeOut 0.3s ease-in forwards';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ----------------- SCREEN SWITCHERS -----------------
function showDashboard() {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('dashboard-screen').classList.remove('hidden');
    
    // Set user display name
    if (state.user) {
        document.getElementById('user-display-name').textContent = `${state.user.firstName} ${state.user.lastName}`;
    }
}

function showAuth() {
    document.getElementById('dashboard-screen').classList.add('hidden');
    document.getElementById('auth-screen').classList.remove('hidden');
    switchAuthTab('login');
}

function switchAuthTab(tab) {
    const loginForm = document.getElementById('login-form');
    const regForm = document.getElementById('register-form');
    const tabLogin = document.getElementById('tab-login');
    const tabReg = document.getElementById('tab-register');
    const activationHelper = document.getElementById('activation-helper');

    activationHelper.classList.add('hidden');

    if (tab === 'login') {
        loginForm.classList.remove('hidden');
        regForm.classList.add('hidden');
        tabLogin.classList.add('active');
        tabReg.classList.remove('active');
    } else {
        loginForm.classList.add('hidden');
        regForm.classList.remove('hidden');
        tabLogin.classList.remove('active');
        tabReg.classList.add('active');
    }
}

// ----------------- AUTHENTICATION FLOWS -----------------
async function handleLogin(event) {
    event.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_BASE}/users/login`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ email, password })
        });
        const result = await response.json();

        if (response.ok && result.success) {
            state.token = result.data.token;
            state.user = result.data.user;
            localStorage.setItem('fundoo_token', state.token);
            localStorage.setItem('fundoo_user', JSON.stringify(state.user));
            
            showToast('Login successful!');
            showDashboard();
            fetchLabels();
            refreshNotes();
        } else {
            showToast(result.message || 'Login failed. Please try again.', 'error');
        }
    } catch (err) {
        console.error('Login error:', err);
        showToast('Connection to server failed.', 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const firstName = document.getElementById('reg-firstname').value;
    const lastName = document.getElementById('reg-lastname').value;
    const email = document.getElementById('reg-email').value;
    const mobileNumber = document.getElementById('reg-mobile').value;
    const password = document.getElementById('reg-password').value;

    try {
        const response = await fetch(`${API_BASE}/users/register`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ firstName, lastName, email, mobileNumber, password })
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Registration successful! Please verify email.');
            // Display simulated local verification helper
            document.getElementById('activation-helper').classList.remove('hidden');
            document.getElementById('verify-token-input').value = '';
        } else {
            showToast(result.message || 'Registration failed.', 'error');
        }
    } catch (err) {
        console.error('Registration error:', err);
        showToast('Connection to server failed.', 'error');
    }
}

async function handleVerifyInline() {
    const token = document.getElementById('verify-token-input').value.trim();
    if (!token) {
        showToast('Please paste a token', 'error');
        return;
    }

    try {
        // Strip any quotes or whitespace from token
        const cleanToken = token.replace(/['"]+/g, '').trim();
        const response = await fetch(`${API_BASE}/users/verify-email?token=${encodeURIComponent(cleanToken)}`, {
            method: 'GET',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Account verified successfully! You can now log in.');
            document.getElementById('activation-helper').classList.add('hidden');
            switchAuthTab('login');
        } else {
            showToast(result.message || 'Verification failed.', 'error');
        }
    } catch (err) {
        console.error('Verification error:', err);
        showToast('Connection to server failed.', 'error');
    }
}

function handleLogout() {
    state.token = null;
    state.user = null;
    localStorage.removeItem('fundoo_token');
    localStorage.removeItem('fundoo_user');
    
    // Clear forms
    document.getElementById('login-email').value = '';
    document.getElementById('login-password').value = '';
    document.getElementById('reg-firstname').value = '';
    document.getElementById('reg-lastname').value = '';
    document.getElementById('reg-email').value = '';
    document.getElementById('reg-mobile').value = '';
    document.getElementById('reg-password').value = '';
    
    showToast('Logged out successfully.');
    showAuth();
}

// ----------------- SIDEBAR AND NAVIGATION -----------------
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('collapsed');
}

function switchNav(view) {
    // Update nav classes
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });

    state.currentView = view;
    state.searchQuery = '';
    document.getElementById('search-input').value = '';

    // Handle view titles & Creator visibility
    const creatorWrapper = document.getElementById('note-creator-wrapper');
    const sectionTitle = document.getElementById('section-title');

    if (typeof view === 'number') {
        // Label View
        const labelObj = state.labels.find(l => l.id === view);
        sectionTitle.textContent = labelObj ? labelObj.name.toUpperCase() : 'LABEL';
        creatorWrapper.classList.add('hidden');
    } else {
        // Standard View
        const navBtn = document.getElementById(`nav-${view}`);
        if (navBtn) navBtn.classList.add('active');

        if (view === 'notes') {
            sectionTitle.textContent = 'NOTES';
            creatorWrapper.classList.remove('hidden');
        } else if (view === 'archive') {
            sectionTitle.textContent = 'ARCHIVE';
            creatorWrapper.classList.add('hidden');
        } else if (view === 'trash') {
            sectionTitle.textContent = 'TRASH';
            creatorWrapper.classList.add('hidden');
        }
    }

    refreshNotes();
}

// ----------------- NOTE CREATOR FUNCTIONS -----------------
function expandNoteCreator() {
    document.getElementById('note-creator-collapsed').classList.add('hidden');
    document.getElementById('note-creator-expanded').classList.remove('hidden');
    document.getElementById('note-title').focus();
    
    // Reset Creator state
    state.creatorColor = 'white';
    updateCreatorBackground();
}

function collapseNoteCreator(save = false) {
    if (save) {
        handleCreateNote();
    }
    
    // Reset inputs
    document.getElementById('note-title').value = '';
    document.getElementById('note-description').value = '';
    document.getElementById('note-creator-collapsed').classList.remove('hidden');
    document.getElementById('note-creator-expanded').classList.add('hidden');
}

function selectCreatorColor(colorName, hex) {
    state.creatorColor = colorName;
    updateCreatorBackground();
}

function updateCreatorBackground() {
    const creator = document.getElementById('note-creator-expanded');
    // Set custom background color inline
    creator.style.backgroundColor = COLOR_MAP[state.creatorColor];
    
    // Make font dark if note is colored, else keep light
    if (state.creatorColor === 'white') {
        creator.style.color = 'var(--text-primary)';
        creator.querySelectorAll('input, textarea').forEach(el => el.style.color = 'var(--text-primary)');
    } else {
        creator.style.color = '#1e293b';
        creator.querySelectorAll('input, textarea').forEach(el => el.style.color = '#1e293b');
    }
}

function autoGrowTextarea(element) {
    element.style.height = 'auto';
    element.style.height = (element.scrollHeight) + 'px';
}

// ----------------- NOTES DATA FETCH & RENDER -----------------
async function refreshNotes() {
    try {
        let url = `${API_BASE}/notes?size=100`;
        
        if (state.searchQuery) {
            url = `${API_BASE}/notes/search?query=${encodeURIComponent(state.searchQuery)}&size=100`;
        } else if (typeof state.currentView === 'number') {
            url = `${API_BASE}/labels/${state.currentView}/notes?size=100`;
        } else if (state.currentView === 'archive') {
            url += `&archived=true&trashed=false`;
        } else if (state.currentView === 'trash') {
            url += `&archived=false&trashed=true`;
        } else {
            // Default active notes
            url += `&archived=false&trashed=false`;
        }

        const response = await fetch(url, {
            method: 'GET',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            // Spring Boot Page structure maps content inside data.content
            state.notes = result.data.content || [];
            renderNotes();
        } else {
            console.error('Failed to fetch notes:', result.message);
        }
    } catch (err) {
        console.error('Refresh notes error:', err);
    }
}

function renderNotes() {
    const pinnedSection = document.getElementById('pinned-section');
    const pinnedGrid = document.getElementById('pinned-notes-grid');
    const otherGrid = document.getElementById('notes-grid');
    const emptyState = document.getElementById('empty-state');

    pinnedGrid.innerHTML = '';
    otherGrid.innerHTML = '';

    // Filter into pinned vs unpinned if we are in notes view (only show Pinned header when notes contains pins)
    const isMainNotesView = state.currentView === 'notes' && !state.searchQuery;
    
    let pinnedNotes = [];
    let otherNotes = [];

    if (isMainNotesView) {
        pinnedNotes = state.notes.filter(n => n.pinned);
        otherNotes = state.notes.filter(n => !n.pinned);
    } else {
        otherNotes = state.notes;
    }

    // Toggle Pinned Section Visibility
    if (pinnedNotes.length > 0) {
        pinnedSection.classList.remove('hidden');
        pinnedNotes.forEach(note => pinnedGrid.appendChild(createNoteCard(note)));
    } else {
        pinnedSection.classList.add('hidden');
    }

    // Render other notes
    if (otherNotes.length > 0) {
        emptyState.classList.add('hidden');
        otherNotes.forEach(note => otherGrid.appendChild(createNoteCard(note)));
    } else if (pinnedNotes.length === 0) {
        // Fully empty state
        emptyState.classList.remove('hidden');
        updateEmptyStateMessage();
    }
}

function updateEmptyStateMessage() {
    const emptyIcon = document.querySelector('#empty-state i');
    const emptyTitle = document.querySelector('#empty-state h3');
    const emptyDesc = document.querySelector('#empty-state p');

    if (state.currentView === 'archive') {
        emptyIcon.className = 'fa-solid fa-box-archive';
        emptyTitle.textContent = 'Your archived notes appear here';
        emptyDesc.textContent = '';
    } else if (state.currentView === 'trash') {
        emptyIcon.className = 'fa-regular fa-trash-can';
        emptyTitle.textContent = 'No notes in Trash';
        emptyDesc.textContent = '';
    } else if (state.searchQuery) {
        emptyIcon.className = 'fa-solid fa-magnifying-glass';
        emptyTitle.textContent = 'No matching notes found';
        emptyDesc.textContent = 'Try adjusting your search terms';
    } else {
        emptyIcon.className = 'fa-regular fa-lightbulb';
        emptyTitle.textContent = 'Your notes will appear here';
        emptyDesc.textContent = 'Start creating notes to organize your life!';
    }
}

function createNoteCard(note) {
    const card = document.createElement('div');
    // Map CSS variable color classes
    card.className = `note-card ${note.color || 'white'}`;
    if (note.pinned) card.classList.add('is-pinned');

    // Build custom inline color backdrops
    card.style.backgroundColor = COLOR_MAP[note.color || 'white'];

    // Note font styling based on coloring
    if (note.color && note.color !== 'white') {
        card.style.color = '#1e293b';
    } else {
        card.style.color = 'var(--text-primary)';
    }

    // Pin Button Markup
    const pinMarkup = state.currentView !== 'trash' ? `
        <button class="pin-card-btn" onclick="event.stopPropagation(); toggleNotePin(${note.id})">
            <i class="fa-solid fa-thumbtack"></i>
        </button>
    ` : '';

    // Dynamic Labels pill badges markup
    let labelsMarkup = '';
    if (note.labels && note.labels.length > 0) {
        labelsMarkup = `<div class="mini-labels-row">`;
        note.labels.forEach(lbl => {
            labelsMarkup += `<span class="label-badge">${lbl.name}</span>`;
        });
        labelsMarkup += `</div>`;
    }

    // Dynamic Collaborators mini avatars markup
    let collabMarkup = '';
    if (note.collaborators && note.collaborators.length > 0) {
        collabMarkup = `<div class="mini-avatar-row">`;
        note.collaborators.forEach(c => {
            const initial = c.email.substring(0, 1).toUpperCase();
            collabMarkup += `<div class="collab-badge-avatar" title="${c.email} (${c.role})">${initial}</div>`;
        });
        collabMarkup += `</div>`;
    }

    // Card Action Buttons Markup
    let actionButtons = '';
    if (state.currentView === 'trash') {
        // Trash actions: Permanent Delete or Restore
        actionButtons = `
            <i class="fa-solid fa-trash-arrow-up" title="Restore Note" onclick="event.stopPropagation(); toggleNoteTrash(${note.id})"></i>
            <i class="fa-solid fa-trash" title="Delete Permanently" class="delete-icon" onclick="event.stopPropagation(); deletePermanently(${note.id})"></i>
        `;
    } else {
        // Normal actions: Color, Archive, Trash, Collab
        actionButtons = `
            <div class="card-color-picker" onclick="event.stopPropagation();">
                <i class="fa-solid fa-palette" title="Change Color"></i>
                <div class="card-color-palette">
                    <div class="color-option white" style="background-color: #ffffff;" onclick="updateNoteColor(${note.id}, 'white')"></div>
                    <div class="color-option blue" style="background-color: #e8f0fe;" onclick="updateNoteColor(${note.id}, 'blue')"></div>
                    <div class="color-option green" style="background-color: #e6f4ea;" onclick="updateNoteColor(${note.id}, 'green')"></div>
                    <div class="color-option yellow" style="background-color: #fef7e0;" onclick="updateNoteColor(${note.id}, 'yellow')"></div>
                    <div class="color-option red" style="background-color: #fce8e6;" onclick="updateNoteColor(${note.id}, 'red')"></div>
                    <div class="color-option purple" style="background-color: #f3e8fd;" onclick="updateNoteColor(${note.id}, 'purple')"></div>
                    <div class="color-option orange" style="background-color: #feefe3;" onclick="updateNoteColor(${note.id}, 'orange')"></div>
                    <div class="color-option pink" style="background-color: #ffebf3;" onclick="updateNoteColor(${note.id}, 'pink')"></div>
                </div>
            </div>
            <i class="fa-solid fa-user-plus" title="Collaborators" onclick="event.stopPropagation(); openCollaboratorsModal(${note.id})"></i>
            <i class="${note.archived ? 'fa-solid fa-box-open' : 'fa-solid fa-box-archive'}" title="${note.archived ? 'Unarchive' : 'Archive'}" onclick="event.stopPropagation(); toggleNoteArchive(${note.id})"></i>
            <i class="fa-regular fa-trash-can delete-icon" title="Trash Note" onclick="event.stopPropagation(); toggleNoteTrash(${note.id})"></i>
        `;
    }

    card.innerHTML = `
        ${pinMarkup}
        <div class="card-content" onclick="openEditNoteModal(${JSON.stringify(note).replace(/"/g, '&quot;')})">
            <h4>${note.title || 'Untitled'}</h4>
            <p>${note.description || 'No description'}</p>
            ${labelsMarkup}
            ${collabMarkup}
        </div>
        <div class="card-options">
            ${actionButtons}
        </div>
    `;

    return card;
}

// ----------------- NOTE OPERATIONS -----------------
async function handleCreateNote() {
    const title = document.getElementById('note-title').value;
    const description = document.getElementById('note-description').value;

    if (!title && !description) return;

    try {
        const response = await fetch(`${API_BASE}/notes`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({
                title,
                description,
                color: state.creatorColor,
                pinned: false,
                archived: false,
                trashed: false
            })
        });
        const result = await response.json();

        if (response.ok && result.success) {
            refreshNotes();
        } else {
            showToast(result.message || 'Failed to create note', 'error');
        }
    } catch (err) {
        console.error('Create note error:', err);
    }
}

async function toggleNotePin(noteId) {
    try {
        const response = await fetch(`${API_BASE}/notes/${noteId}/pin`, {
            method: 'PATCH',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            refreshNotes();
        } else {
            showToast(result.message || 'Failed to toggle pin', 'error');
        }
    } catch (err) {
        console.error('Pin error:', err);
    }
}

async function toggleNoteArchive(noteId) {
    try {
        const response = await fetch(`${API_BASE}/notes/${noteId}/archive`, {
            method: 'PATCH',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast(result.data.archived ? 'Note archived' : 'Note unarchived');
            refreshNotes();
        } else {
            showToast(result.message || 'Failed to toggle archive', 'error');
        }
    } catch (err) {
        console.error('Archive error:', err);
    }
}

async function toggleNoteTrash(noteId) {
    try {
        const response = await fetch(`${API_BASE}/notes/${noteId}/trash`, {
            method: 'PATCH',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast(result.data.trashed ? 'Note moved to trash' : 'Note restored');
            refreshNotes();
        } else {
            showToast(result.message || 'Failed to toggle trash', 'error');
        }
    } catch (err) {
        console.error('Trash error:', err);
    }
}

async function updateNoteColor(noteId, colorName) {
    try {
        const response = await fetch(`${API_BASE}/notes/${noteId}/color?color=${colorName}`, {
            method: 'PATCH',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            refreshNotes();
        } else {
            showToast(result.message || 'Failed to update color', 'error');
        }
    } catch (err) {
        console.error('Color update error:', err);
    }
}

async function deletePermanently(noteId) {
    if (!confirm('Are you sure you want to delete this note permanently? This action cannot be undone.')) return;

    try {
        const response = await fetch(`${API_BASE}/notes/${noteId}`, {
            method: 'DELETE',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Note permanently deleted');
            refreshNotes();
        } else {
            showToast(result.message || 'Failed to delete note', 'error');
        }
    } catch (err) {
        console.error('Permanent delete error:', err);
    }
}

// ----------------- NOTE EDIT MODAL FLOWS -----------------
function openEditNoteModal(note) {
    // If note is trashed, we shouldn't allow editing (Google Keep style)
    if (note.trashed) return;

    state.editingNote = note;
    state.editingNoteColor = note.color || 'white';
    
    document.getElementById('edit-note-title').value = note.title || '';
    document.getElementById('edit-note-description').value = note.description || '';
    
    updateEditModalBackground();
    renderEditModalBadges();
    
    // Clear label dropdown state
    document.getElementById('note-label-dropdown').classList.add('hidden');
    
    document.getElementById('edit-note-modal').classList.remove('hidden');
}

function updateEditModalBackground() {
    const card = document.getElementById('edit-modal-card');
    card.style.backgroundColor = COLOR_MAP[state.editingNoteColor];
    
    if (state.editingNoteColor === 'white') {
        card.style.color = 'var(--text-primary)';
        card.querySelectorAll('input, textarea').forEach(el => el.style.color = 'var(--text-primary)');
    } else {
        card.style.color = '#1e293b';
        card.querySelectorAll('input, textarea').forEach(el => el.style.color = '#1e293b');
    }
}

function renderEditModalBadges() {
    const colList = document.getElementById('modal-collaborators-list');
    const lblList = document.getElementById('modal-labels-list');

    colList.innerHTML = '';
    lblList.innerHTML = '';

    const note = state.editingNote;

    if (note.collaborators && note.collaborators.length > 0) {
        note.collaborators.forEach(c => {
            const initial = c.email.substring(0, 1).toUpperCase();
            const badge = document.createElement('div');
            badge.className = 'collab-badge-avatar';
            badge.title = `${c.email} (${c.role})`;
            badge.textContent = initial;
            colList.appendChild(badge);
        });
    }

    if (note.labels && note.labels.length > 0) {
        note.labels.forEach(l => {
            const badge = document.createElement('span');
            badge.className = 'label-badge';
            badge.innerHTML = `
                <span>${l.name}</span>
                <i class="fa-solid fa-xmark" onclick="handleDetachLabelFromEdit(${l.id})"></i>
            `;
            lblList.appendChild(badge);
        });
    }
}

function selectEditColor(colorName) {
    state.editingNoteColor = colorName;
    updateEditModalBackground();
}

async function saveEditNote() {
    if (!state.editingNote) return;

    const id = state.editingNote.id;
    const title = document.getElementById('edit-note-title').value;
    const description = document.getElementById('edit-note-description').value;

    try {
        // Step 1: Save core fields (title, description, color)
        const response = await fetch(`${API_BASE}/notes/${id}`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify({
                title,
                description,
                color: state.editingNoteColor,
                pinned: state.editingNote.pinned,
                archived: state.editingNote.archived,
                trashed: state.editingNote.trashed
            })
        });
        const result = await response.json();

        if (response.ok && result.success) {
            closeEditNoteModal();
            refreshNotes();
        } else {
            showToast(result.message || 'Failed to update note', 'error');
        }
    } catch (err) {
        console.error('Update note error:', err);
    }
}

function closeEditNoteModal() {
    document.getElementById('edit-note-modal').classList.add('hidden');
    state.editingNote = null;
}

function handleDeleteFromEdit() {
    if (state.editingNote) {
        toggleNoteTrash(state.editingNote.id);
        closeEditNoteModal();
    }
}

// ----------------- GLOBAL LABELS MANAGEMENT -----------------
async function fetchLabels() {
    try {
        const response = await fetch(`${API_BASE}/labels`, {
            method: 'GET',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            state.labels = result.data || [];
            renderSidebarLabels();
        }
    } catch (err) {
        console.error('Fetch labels error:', err);
    }
}

function renderSidebarLabels() {
    const container = document.getElementById('sidebar-labels-container');
    container.innerHTML = '';

    state.labels.forEach(lbl => {
        const btn = document.createElement('button');
        btn.id = `nav-label-${lbl.id}`;
        btn.className = `nav-item ${state.currentView === lbl.id ? 'active' : ''}`;
        btn.onclick = () => switchNav(lbl.id);
        btn.innerHTML = `<i class="fa-solid fa-tag"></i> <span>${lbl.name}</span>`;
        container.appendChild(btn);
    });
}

function openLabelsModal() {
    renderLabelsEditList();
    document.getElementById('labels-modal').classList.remove('hidden');
}

function closeLabelsModal() {
    document.getElementById('labels-modal').classList.add('hidden');
    fetchLabels(); // Reload sidebar
    refreshNotes(); // Refresh active view
}

function renderLabelsEditList() {
    const list = document.getElementById('labels-edit-list');
    list.innerHTML = '';

    state.labels.forEach(lbl => {
        const item = document.createElement('div');
        item.className = 'labels-edit-item';
        item.innerHTML = `
            <input type="text" value="${lbl.name}" onchange="handleUpdateLabelName(${lbl.id}, this.value)">
            <div class="labels-edit-actions">
                <i class="fa-regular fa-trash-can delete-label-icon" onclick="handleDeleteLabel(${lbl.id})"></i>
            </div>
        `;
        list.appendChild(item);
    });
}

async function handleCreateLabel() {
    const input = document.getElementById('new-label-name');
    const name = input.value.trim();

    if (!name) return;

    try {
        const response = await fetch(`${API_BASE}/labels`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ name })
        });
        const result = await response.json();

        if (response.ok && result.success) {
            input.value = '';
            // Refresh
            state.labels.push(result.data);
            renderLabelsEditList();
            renderSidebarLabels();
        } else {
            showToast(result.message || 'Failed to create label', 'error');
        }
    } catch (err) {
        console.error('Create label error:', err);
    }
}

async function handleUpdateLabelName(labelId, newName) {
    if (!newName.trim()) return;

    try {
        const response = await fetch(`${API_BASE}/labels/${labelId}`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify({ name: newName })
        });
        const result = await response.json();

        if (response.ok && result.success) {
            // Update local state
            const index = state.labels.findIndex(l => l.id === labelId);
            if (index !== -1) state.labels[index].name = newName;
            renderSidebarLabels();
        } else {
            showToast(result.message || 'Failed to update label', 'error');
        }
    } catch (err) {
        console.error('Update label error:', err);
    }
}

async function handleDeleteLabel(labelId) {
    if (!confirm('Are you sure you want to delete this label? It will be detached from all notes.')) return;

    try {
        const response = await fetch(`${API_BASE}/labels/${labelId}`, {
            method: 'DELETE',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            state.labels = state.labels.filter(l => l.id !== labelId);
            renderLabelsEditList();
            renderSidebarLabels();
            
            // If we were viewing this label, switch to notes
            if (state.currentView === labelId) {
                switchNav('notes');
            }
        } else {
            showToast(result.message || 'Failed to delete label', 'error');
        }
    } catch (err) {
        console.error('Delete label error:', err);
    }
}

// ----------------- NOTE LABELS ASSOCIATING (EDIT MODAL) -----------------
function toggleLabelDropdown() {
    const dropdown = document.getElementById('note-label-dropdown');
    const isHidden = dropdown.classList.contains('hidden');

    if (isHidden) {
        renderLabelDropdownOptions();
        dropdown.classList.remove('hidden');
    } else {
        dropdown.classList.add('hidden');
    }
}

function renderLabelDropdownOptions() {
    const list = document.getElementById('note-label-options');
    list.innerHTML = '';

    const note = state.editingNote;
    const attachedIds = (note.labels || []).map(l => l.id);

    state.labels.forEach(lbl => {
        const checked = attachedIds.includes(lbl.id) ? 'checked' : '';
        const labelItem = document.createElement('label');
        labelItem.className = 'label-option-checkbox';
        labelItem.innerHTML = `
            <input type="checkbox" ${checked} onchange="handleLabelToggle(${note.id}, ${lbl.id}, this.checked)">
            <span>${lbl.name}</span>
        `;
        list.appendChild(labelItem);
    });
}

async function handleLabelToggle(noteId, labelId, isChecked) {
    try {
        const method = isChecked ? 'POST' : 'DELETE';
        const url = `${API_BASE}/notes/${noteId}/labels/${labelId}`;

        const response = await fetch(url, {
            method,
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            // Update editing note state labels
            state.editingNote = result.data;
            renderEditModalBadges();
        } else {
            showToast(result.message || 'Failed to toggle label association', 'error');
        }
    } catch (err) {
        console.error('Label toggle error:', err);
    }
}

async function handleDetachLabelFromEdit(labelId) {
    if (!state.editingNote) return;
    await handleLabelToggle(state.editingNote.id, labelId, false);
}

// ----------------- COLLABORATORS MANAGEMENT -----------------
async function openCollaboratorsModal(noteId) {
    state.collaboratingNoteId = noteId;
    
    // Clear form
    document.getElementById('new-collab-email').value = '';
    document.getElementById('new-collab-role').value = 'VIEWER';

    // Set Owner info (default to current user or look inside note if ownerId is different)
    const note = state.notes.find(n => n.id === noteId) || state.editingNote;
    
    // Fill Owner display
    document.getElementById('collab-owner-name').textContent = state.user ? `${state.user.firstName} ${state.user.lastName} (Owner)` : 'Owner';
    document.getElementById('collab-owner-email').textContent = state.user ? state.user.email : '';

    renderCollaboratorsList(note.collaborators || []);
    document.getElementById('collaborators-modal').classList.remove('hidden');
}

function openCollaboratorsModalFromEdit() {
    if (state.editingNote) {
        openCollaboratorsModal(state.editingNote.id);
    }
}

function closeCollaboratorsModal() {
    document.getElementById('collaborators-modal').classList.add('hidden');
    state.collaboratingNoteId = null;
    
    // If edit modal is open, we need to refresh editingNote's collaborations
    if (state.editingNote) {
        // Fetch fresh note details
        fetchNoteDetailsForEdit(state.editingNote.id);
    } else {
        refreshNotes();
    }
}

async function fetchNoteDetailsForEdit(noteId) {
    try {
        const response = await fetch(`${API_BASE}/notes/${noteId}`, {
            method: 'GET',
            headers: getHeaders()
        });
        const result = await response.json();
        if (response.ok && result.success) {
            state.editingNote = result.data;
            renderEditModalBadges();
        }
    } catch (err) {
        console.error('Fetch note details error:', err);
    }
}

function renderCollaboratorsList(collaborators) {
    const container = document.getElementById('collaborators-list-container');
    container.innerHTML = '';

    if (collaborators.length === 0) {
        container.innerHTML = '<p style="font-size: 13px; color: var(--text-muted); text-align: center; padding: 10px;">Not shared with anyone yet</p>';
        return;
    }

    collaborators.forEach(c => {
        const item = document.createElement('div');
        item.className = 'collaborator-item';
        item.innerHTML = `
            <div class="collab-details">
                <span class="collab-email">${c.email}</span>
                <span class="collab-role">${c.role}</span>
            </div>
            <button class="delete-collab-btn" onclick="handleRemoveCollaborator(${c.id})">
                <i class="fa-regular fa-trash-can"></i>
            </button>
        `;
        container.appendChild(item);
    });
}

async function handleAddCollaborator() {
    const noteId = state.collaboratingNoteId;
    const email = document.getElementById('new-collab-email').value.trim();
    const role = document.getElementById('new-collab-role').value;

    if (!email) {
        showToast('Please provide an email address', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/notes/${noteId}/collaborators`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ email, role })
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Collaborator added successfully!');
            document.getElementById('new-collab-email').value = '';
            
            // Reload list
            fetchCollaboratorsForModal(noteId);
        } else {
            showToast(result.message || 'Failed to add collaborator', 'error');
        }
    } catch (err) {
        console.error('Add collab error:', err);
    }
}

async function fetchCollaboratorsForModal(noteId) {
    try {
        const response = await fetch(`${API_BASE}/notes/${noteId}/collaborators`, {
            method: 'GET',
            headers: getHeaders()
        });
        const result = await response.json();
        if (response.ok && result.success) {
            renderCollaboratorsList(result.data || []);
        }
    } catch (err) {
        console.error('Fetch collaborators error:', err);
    }
}

async function handleRemoveCollaborator(collabRecordId) {
    const noteId = state.collaboratingNoteId;
    if (!confirm('Remove access for this collaborator?')) return;

    try {
        const response = await fetch(`${API_BASE}/notes/${noteId}/collaborators/${collabRecordId}`, {
            method: 'DELETE',
            headers: getHeaders()
        });
        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Collaborator removed successfully');
            fetchCollaboratorsForModal(noteId);
        } else {
            showToast(result.message || 'Failed to remove collaborator', 'error');
        }
    } catch (err) {
        console.error('Remove collab error:', err);
    }
}

// ----------------- SEARCH HANDLER -----------------
let searchDebounceTimeout = null;
function handleSearch(query) {
    clearTimeout(searchDebounceTimeout);
    state.searchQuery = query.trim();
    
    searchDebounceTimeout = setTimeout(() => {
        refreshNotes();
    }, 300);
}

// Helper to click outside overlays to close modal
function handleModalOverlayClick(event, modalId) {
    if (event.target.id === modalId) {
        if (modalId === 'edit-note-modal') {
            saveEditNote();
        } else if (modalId === 'labels-modal') {
            closeLabelsModal();
        } else if (modalId === 'collaborators-modal') {
            closeCollaboratorsModal();
        }
    }
}
