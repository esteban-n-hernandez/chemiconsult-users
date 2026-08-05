"use strict";

// ── Estado ───────────────────────────────────────────────────
let tasks      = [];
let usuarios   = [];
let filtroActivo      = '';
let editandoId        = null;
let pickerEstadoAbierto = null;

const STATUS_SEQ = ['TODO', 'IN_PROGRESS', 'EN_REVISION', 'DONE'];

const STATUS_CFG = {
    TODO:        { cls: 'todo',     lbl: 'Pendiente',   emoji: '📋' },
    IN_PROGRESS: { cls: 'inprog',   lbl: 'En progreso', emoji: '⏳' },
    EN_REVISION: { cls: 'revision', lbl: 'En revisión', emoji: '🔍' },
    DONE:        { cls: 'done',     lbl: 'Lista',        emoji: '✓'  },
};

// ── Init ─────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
    const role = (localStorage.getItem('userRole') || '').toUpperCase();
    const mods = JSON.parse(localStorage.getItem('userModulos') || '[]');
    if (role !== 'ROLE_IT' && !mods.includes('TAREAS')) return;

    initHeader('mob-sub', 'mob-avatar');
    setupFilters();
    setupForm();
    document.getElementById('fab-nueva')?.addEventListener('click', abrirNueva);
    document.addEventListener('click', cerrarPickerEstado);
    await Promise.all([cargarUsuarios(), cargarTareas()]);
});

// ── Data ─────────────────────────────────────────────────────
async function cargarTareas() {
    mostrarLoading(true);
    try {
        tasks = await apiFetch(`${MOB_API}/task`) || [];
        renderTareas();
    } catch {
        mobToast('Error cargando tareas', 'error');
    } finally {
        mostrarLoading(false);
    }
}

async function cargarUsuarios() {
    try {
        usuarios = await apiFetch(`${MOB_API}/users/asignables`) || [];
        const sel = document.getElementById('task-asignado');
        if (!sel) return;
        usuarios.forEach(u => {
            const lbl = u.username || u.email;
            sel.appendChild(new Option(lbl, u.id));
        });
    } catch { /* sin usuarios */ }
}

// ── Render ───────────────────────────────────────────────────
function renderTareas() {
    const list  = document.getElementById('task-list');
    const empty = document.getElementById('task-empty');

    list.querySelectorAll('.tarea-card').forEach(c => c.remove());

    const filtradas = filtroActivo
        ? tasks.filter(t => t.status === filtroActivo)
        : tasks;

    actualizarSubtitulo();

    if (filtradas.length === 0) {
        empty.style.display = 'flex';
        return;
    }
    empty.style.display = 'none';

    const ordenadas = [...filtradas].sort((a, b) => {
        const ua = urgencia(a), ub = urgencia(b);
        if (ua !== ub) return ua - ub;
        return STATUS_SEQ.indexOf(a.status) - STATUS_SEQ.indexOf(b.status);
    });

    const scrollEnd = list.querySelector('.scroll-end');
    ordenadas.forEach(t => list.insertBefore(crearCard(t), scrollEnd));
}

function actualizarSubtitulo() {
    const el = document.getElementById('mob-sub');
    if (!el) return;
    const pendiente  = tasks.filter(t => t.status === 'TODO').length;
    const en_curso   = tasks.filter(t => t.status === 'IN_PROGRESS').length;
    const partes = [];
    if (pendiente) partes.push(`${pendiente} pendiente${pendiente > 1 ? 's' : ''}`);
    if (en_curso)  partes.push(`${en_curso} en progreso`);
    const rawName = localStorage.getItem('username') || localStorage.getItem('userEmail') || '';
    const firstName = rawName.includes('@')
        ? rawName.split('@')[0].split(/[\._\-]/)[0]
        : rawName.split(' ')[0];
    const hour = new Date().getHours();
    const saludo = hour < 12 ? 'Buenos días' : hour < 20 ? 'Buenas tardes' : 'Buenas noches';
    const base = firstName ? `${saludo}, ${firstName}` : saludo;
    el.textContent = partes.length ? `${base} · ${partes.join(' · ')}` : base;
}

function crearCard(task) {
    const card = document.createElement('div');
    card.className = 'tarea-card';
    card.dataset.id   = task.id;
    if (task.status === 'DONE') card.dataset.done = 'true';

    const cfg       = STATUS_CFG[task.status] || STATUS_CFG.TODO;
    const initials  = task.userName ? getInitials(task.userName) : '—';
    const asignado  = task.userName || 'Sin asignar';

    card.innerHTML = `
      <div class="tarea-card-inner" onclick="abrirEditar(${task.id})">
        <div class="tarea-bar ${barClass(task)}"></div>
        <div class="tarea-body">
          <div class="tarea-title">${escHtml(task.title)}</div>
          ${task.description ? `<div class="tarea-desc">${escHtml(task.description)}</div>` : ''}
          <div class="tarea-meta">
            <div class="tarea-assign">
              <span class="assign-avatar">${escHtml(initials)}</span>
              ${escHtml(asignado)}
            </div>
            ${badgeFecha(task)}
          </div>
        </div>
      </div>
      <div class="tarea-footer">
        <button class="status-pill ${cfg.cls}"
                onclick="abrirPickerEstado(event,${task.id})">
          ${cfg.emoji} ${cfg.lbl}
        </button>
      </div>
    `;
    return card;
}

// ── Urgencia y badges ─────────────────────────────────────────
function urgencia(task) {
    if (task.status === 'DONE') return 99;
    if (!task.dueDate) return 10;
    const hoy = new Date(); hoy.setHours(0,0,0,0);
    const venc = new Date(task.dueDate + 'T00:00:00');
    const diff = Math.round((venc - hoy) / 86400000);
    if (diff < 0)  return 0;
    if (diff === 0) return 1;
    if (diff <= 2)  return 2;
    if (diff <= 7)  return 3;
    return 10;
}

function barClass(task) {
    if (task.status === 'DONE') return 'bar-done';
    if (!task.dueDate) return 'bar-none';
    const u = urgencia(task);
    if (u === 0) return 'bar-over';
    if (u <= 2)  return 'bar-soon';
    if (u <= 3)  return 'bar-week';
    return 'bar-ok';
}

function badgeFecha(task) {
    if (!task.dueDate || task.status === 'DONE') return '';
    const hoy  = new Date(); hoy.setHours(0,0,0,0);
    const venc = new Date(task.dueDate + 'T00:00:00');
    const diff = Math.round((venc - hoy) / 86400000);
    if (diff < 0)   return `<span class="dl-badge dl-over">Vencida</span>`;
    if (diff === 0) return `<span class="dl-badge dl-hoy">Hoy</span>`;
    if (diff === 1) return `<span class="dl-badge dl-soon">Mañana</span>`;
    if (diff <= 7)  return `<span class="dl-badge dl-soon">En ${diff} días</span>`;
    const d = venc.toLocaleDateString('es-AR', { day: '2-digit', month: 'short' });
    return `<span class="dl-badge dl-ok">${d}</span>`;
}

// ── Filtros ───────────────────────────────────────────────────
function setupFilters() {
    document.getElementById('filter-chips')?.addEventListener('click', e => {
        const chip = e.target.closest('.chip');
        if (!chip) return;
        document.querySelectorAll('#filter-chips .chip').forEach(c => c.classList.remove('active'));
        chip.classList.add('active');
        filtroActivo = chip.dataset.status;
        renderTareas();
    });
}

// ── Picker de estado ──────────────────────────────────────────
function abrirPickerEstado(e, id) {
    e.stopPropagation();
    const mismoPicker = pickerEstadoAbierto === id;
    cerrarPickerEstado();
    if (mismoPicker) return;

    const task = tasks.find(t => t.id === id);
    if (!task) return;

    const picker = document.getElementById('estado-picker');
    picker.innerHTML = STATUS_SEQ.map(s => {
        const c = STATUS_CFG[s];
        return `<button class="ep-opt${s === task.status ? ' ep-active' : ''}"
                        onclick="seleccionarEstadoTarea(event,${id},'${s}')">
                  ${c.emoji} ${c.lbl}
                </button>`;
    }).join('');

    const rect = e.currentTarget.getBoundingClientRect();
    picker.style.bottom = `${window.innerHeight - rect.top + 6}px`;
    picker.style.right  = `${window.innerWidth - rect.right}px`;

    picker.classList.add('open');
    pickerEstadoAbierto = id;
}

function cerrarPickerEstado() {
    if (pickerEstadoAbierto === null) return;
    document.getElementById('estado-picker')?.classList.remove('open');
    pickerEstadoAbierto = null;
}

async function seleccionarEstadoTarea(e, id, nuevoEstado) {
    e.stopPropagation();
    cerrarPickerEstado();
    const task = tasks.find(t => t.id === id);
    if (!task || task.status === nuevoEstado) return;
    try {
        await apiFetch(`${MOB_API}/task/${id}/status?status=${encodeURIComponent(nuevoEstado)}`, { method: 'PUT' });
        task.status = nuevoEstado;
        renderTareas();
        mobToast('Estado actualizado');
    } catch {
        mobToast('Error al actualizar estado', 'error');
    }
}

// ── Formulario crear / editar ─────────────────────────────────
function abrirNueva() {
    editandoId = null;
    document.getElementById('task-sheet-title').textContent = 'Nueva tarea';
    document.getElementById('task-submit-btn').textContent  = 'Crear tarea';
    document.getElementById('task-titulo').value  = '';
    document.getElementById('task-desc').value    = '';
    document.getElementById('task-fecha').value   = '';
    document.getElementById('task-asignado').value = localStorage.getItem('userId') || '';
    document.getElementById('task-delete-btn').style.display = 'none';
    openSheet('task-sheet');
    setTimeout(() => document.getElementById('task-titulo')?.focus(), 340);
}

function abrirEditar(id) {
    const task = tasks.find(t => t.id === id);
    if (!task) return;
    editandoId = id;
    document.getElementById('task-sheet-title').textContent = 'Editar tarea';
    document.getElementById('task-submit-btn').textContent  = 'Guardar cambios';
    document.getElementById('task-titulo').value   = task.title || '';
    document.getElementById('task-desc').value     = task.description || '';
    document.getElementById('task-fecha').value    = task.dueDate || '';
    document.getElementById('task-asignado').value = task.userId || '';
    document.getElementById('task-delete-btn').style.display = 'flex';
    openSheet('task-sheet');
}

function setupForm() {
    document.getElementById('task-form')?.addEventListener('submit', async e => {
        e.preventDefault();
        const titulo = document.getElementById('task-titulo').value.trim();
        if (!titulo) { mobToast('El título es requerido', 'error'); return; }

        const btn = document.getElementById('task-submit-btn');
        btn.disabled = true;

        const payload = {
            title:       titulo,
            description: document.getElementById('task-desc').value.trim() || null,
            userId:      document.getElementById('task-asignado').value
                             ? Number(document.getElementById('task-asignado').value)
                             : null,
            dueDate:     document.getElementById('task-fecha').value || null,
        };

        try {
            if (editandoId) {
                const updated = await apiFetch(`${MOB_API}/task/${editandoId}`, {
                    method:  'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body:    JSON.stringify(payload),
                });
                tasks = tasks.map(t => t.id === updated.id ? updated : t);
                mobToast('Tarea actualizada');
            } else {
                payload.status = 'TODO';
                const created = await apiFetch(`${MOB_API}/task`, {
                    method:  'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body:    JSON.stringify(payload),
                });
                tasks.unshift(created);
                mobToast('Tarea creada');
            }
            closeSheet('task-sheet');
            renderTareas();
        } catch (err) {
            mobToast(err.message || 'Error al guardar', 'error');
        } finally {
            btn.disabled = false;
        }
    });

    document.getElementById('task-delete-btn')?.addEventListener('click', async () => {
        if (!editandoId) return;
        if (!confirm('¿Eliminar esta tarea? No se puede deshacer.')) return;
        try {
            await apiFetch(`${MOB_API}/task/${editandoId}`, { method: 'DELETE' });
            tasks = tasks.filter(t => t.id !== editandoId);
            closeSheet('task-sheet');
            renderTareas();
            mobToast('Tarea eliminada');
        } catch {
            mobToast('Error al eliminar', 'error');
        }
    });

    document.getElementById('sheet-cancel-btn')?.addEventListener('click', () =>
        closeSheet('task-sheet'));
}

// ── Loading helper ────────────────────────────────────────────
function mostrarLoading(show) {
    const el = document.getElementById('task-loading');
    if (el) el.style.display = show ? 'flex' : 'none';
}
