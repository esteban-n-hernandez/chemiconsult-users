"use strict";

// ── API base viene de config.js (API_BASE) ───────────────────
const MOB_API = `${API_BASE}/api`;

// ── Auth fetch ───────────────────────────────────────────────
async function apiFetch(url, opts = {}) {
    const token = localStorage.getItem('token');
    const res = await fetch(url, {
        ...opts,
        headers: { ...(opts.headers || {}), 'Authorization': `Bearer ${token}` }
    });
    if (res.status === 401) { window.location.href = 'login.html'; return null; }
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.message || `HTTP ${res.status}`);
    }
    return res.status === 204 ? null : res.json();
}

// ── Header ───────────────────────────────────────────────────
function initHeader(subId, avatarId) {
    const rawName = localStorage.getItem('username') || localStorage.getItem('userEmail') || '';
    const firstName = rawName.includes('@')
        ? rawName.split('@')[0].split(/[\._\-]/)[0]
        : rawName.split(' ')[0];

    const hour = new Date().getHours();
    const saludo = hour < 12 ? 'Buenos días' : hour < 20 ? 'Buenas tardes' : 'Buenas noches';

    const elSub    = document.getElementById(subId);
    const elAvatar = document.getElementById(avatarId);
    if (elSub)    elSub.textContent    = firstName ? `${saludo}, ${firstName}` : saludo;
    if (elAvatar) elAvatar.textContent = getInitials(rawName);
}

// ── Toast ────────────────────────────────────────────────────
function mobToast(msg, type = 'ok') {
    const el = document.getElementById('mob-toast');
    if (!el) return;
    el.textContent = msg;
    el.className = `mob-toast show ${type}`;
    clearTimeout(el._t);
    el._t = setTimeout(() => { el.className = 'mob-toast'; }, 3000);
}

// ── Tabs ─────────────────────────────────────────────────────
function switchTab(tab) {
    document.querySelectorAll('.tab-item').forEach(b =>
        b.classList.toggle('active', b.dataset.tab === tab));
    document.querySelectorAll('.view').forEach(v =>
        v.classList.toggle('active', v.id === `view-${tab}`));
}

// ── Bottom sheet ─────────────────────────────────────────────
function openSheet(id) {
    document.getElementById(id).classList.add('open');
    document.getElementById('sheet-backdrop').classList.add('open');
}
function closeSheet(id) {
    document.getElementById(id).classList.remove('open');
    document.getElementById('sheet-backdrop').classList.remove('open');
}

// ── Utils ────────────────────────────────────────────────────
function getInitials(name) {
    if (!name) return '?';
    if (name.includes('@')) {
        const local = name.split('@')[0];
        const parts = local.split(/[\._\-]/);
        return (parts.length >= 2 ? parts[0][0] + parts[1][0] : local.substring(0, 2)).toUpperCase();
    }
    const parts = name.trim().split(' ');
    return (parts.length >= 2 ? parts[0][0] + parts[1][0] : name.substring(0, 2)).toUpperCase();
}

function escHtml(str) {
    return String(str ?? '')
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ── Init ─────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.tab-item').forEach(btn =>
        btn.addEventListener('click', () => switchTab(btn.dataset.tab)));

    document.getElementById('sheet-backdrop')?.addEventListener('click', () => {
        document.querySelectorAll('.bottom-sheet.open')
            .forEach(s => s.classList.remove('open'));
        document.getElementById('sheet-backdrop').classList.remove('open');
    });
});
