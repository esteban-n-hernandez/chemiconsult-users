(function () {
    'use strict';

    const TOKEN     = () => localStorage.getItem('token') || '';
    const FETCH_HDR = () => ({ 'Content-Type': 'application/json', 'Authorization': `Bearer ${TOKEN()}` });

    const $ = id => document.getElementById(id);
    const esc = s => (s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');

    let datosOriginales = [];
    let filtroActual    = 'todos';
    let busqueda        = '';

    // ── Carga ──────────────────────────────────────────────────────────────

    async function cargar() {
        mostrarSkeleton();
        try {
            const res = await fetch('/api/mi-cola/todos', { headers: FETCH_HDR() });
            if (!res.ok) throw new Error(res.status);
            datosOriginales = await res.json();
            renderTodo();
        } catch (e) {
            $('mcLista').innerHTML = `<p style="color:var(--rojo,#ef4444);padding:20px">Error al cargar la cola: ${e.message}</p>`;
        }
    }

    function mostrarSkeleton() {
        $('mcStats').style.display = 'none';
        $('mcEmpty').style.display = 'none';
        $('mcLista').innerHTML = [1, 2, 3].map(() => `
            <div class="mc-skel">
                <div class="mc-skel-line" style="width:40%;height:16px;margin-bottom:14px"></div>
                <div class="mc-skel-line" style="width:80%"></div>
                <div class="mc-skel-line" style="width:60%"></div>
            </div>`).join('');
    }

    // ── Render principal ───────────────────────────────────────────────────

    function renderTodo() {
        actualizarStats();
        renderLista();
    }

    function actualizarStats() {
        let totalPend = 0, totalAnal = 0;
        datosOriginales.forEach(p => {
            totalPend += p.totalPendientes;
            totalAnal += p.totalAnalizado;
        });
        $('statPendientes').textContent     = totalPend;
        $('statPendientesPlural').textContent = totalPend === 1 ? '' : 's';
        $('statAnalizado').textContent      = totalAnal;
        $('statAnalizadoPlural').textContent  = totalAnal === 1 ? '' : 's';
        $('statTotal').textContent          = totalPend + totalAnal;
        $('mcStats').style.display          = 'flex';
    }

    function renderLista() {
        const lista   = $('mcLista');
        const busq    = busqueda.toLowerCase();
        const filtro  = filtroActual;

        // Filtrar por búsqueda de texto
        const filtrados = datosOriginales.filter(p => {
            const hayText = !busq ||
                p.parametroNombre.toLowerCase().includes(busq) ||
                p.muestras.some(m =>
                    (m.nroProtocolo || '').toLowerCase().includes(busq) ||
                    (m.clienteNombre || '').toLowerCase().includes(busq) ||
                    (m.puntoMuestreo || '').toLowerCase().includes(busq)
                );
            if (!hayText) return false;

            // Filtrar por estado
            if (filtro === 'pendiente') return p.totalPendientes > 0;
            if (filtro === 'analizado') return p.totalAnalizado  > 0;
            return true;
        });

        if (filtrados.length === 0) {
            lista.innerHTML = '';
            $('mcEmpty').style.display = 'block';
            return;
        }
        $('mcEmpty').style.display = 'none';

        lista.innerHTML = filtrados.map(p => renderParametroBloque(p)).join('');

        // Eventos de acordeón
        lista.querySelectorAll('.mc-param-head').forEach(head => {
            head.addEventListener('click', () => {
                const body    = head.nextElementSibling;
                const chevron = head.querySelector('.mc-chevron');
                const abierto = body.classList.toggle('open');
                chevron.classList.toggle('open', abierto);
            });
        });

        // Eventos de toggle analizado
        lista.querySelectorAll('.mc-toggle').forEach(btn => {
            btn.addEventListener('click', async e => {
                e.stopPropagation();
                await toggleAnalizado(btn);
            });
        });
    }

    function renderParametroBloque(p) {
        const muestrasParaFiltrar = filtroActual === 'pendiente'
            ? p.muestras.filter(m => !m.analizado)
            : filtroActual === 'analizado'
                ? p.muestras.filter(m => m.analizado)
                : p.muestras;

        const badgePend  = p.totalPendientes > 0
            ? `<span class="mc-badge mc-badge-pend"><i class="bi bi-hourglass-split"></i> ${p.totalPendientes} pendiente${p.totalPendientes !== 1 ? 's' : ''}</span>` : '';
        const badgeAnal  = p.totalAnalizado > 0
            ? `<span class="mc-badge mc-badge-anal"><i class="bi bi-check2-circle"></i> ${p.totalAnalizado} analizado${p.totalAnalizado !== 1 ? 's' : ''}</span>` : '';

        return `
        <div class="mc-param-block">
            <div class="mc-param-head">
                <div class="mc-param-info">
                    <div class="mc-param-icon"><i class="bi bi-flask"></i></div>
                    <div>
                        <div class="mc-param-nombre">${esc(p.parametroNombre)}</div>
                        ${p.unidad ? `<div class="mc-param-unidad">${esc(p.unidad)}</div>` : ''}
                    </div>
                </div>
                <div class="mc-param-badges">
                    ${badgePend}
                    ${badgeAnal}
                </div>
                <i class="bi bi-chevron-down mc-chevron"></i>
            </div>
            <div class="mc-param-body">
                <table class="mc-tabla">
                    <thead>
                        <tr>
                            <th>N° Protocolo</th>
                            <th>Cliente</th>
                            <th>Punto de muestreo</th>
                            <th>Fecha entrega</th>
                            <th>Estado</th>
                            <th style="text-align:center">Analizado</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${muestrasParaFiltrar.map(m => renderMuestraRow(m)).join('')}
                    </tbody>
                </table>
            </div>
        </div>`;
    }

    function renderMuestraRow(m) {
        const estadoClass = `mc-estado-${m.estado || 'INGRESADO'}`;
        const estadoLabel = (m.estado || 'INGRESADO').replace(/_/g, ' ');
        const fecha       = m.fechaEntrega
            ? new Date(m.fechaEntrega + 'T00:00:00').toLocaleDateString('es-AR')
            : '—';

        const toggleClass = m.analizado ? 'analizado' : 'pendiente';
        const toggleIcon  = m.analizado ? 'bi-check2-circle' : 'bi-hourglass-split';
        const toggleLabel = m.analizado ? 'Analizado' : 'Pendiente';

        return `
        <tr>
            <td><span class="mc-proto">${esc(m.nroProtocolo || '—')}</span></td>
            <td>${esc(m.clienteNombre || '—')}</td>
            <td>${esc(m.puntoMuestreo || '—')}</td>
            <td>${fecha}</td>
            <td><span class="mc-estado-badge ${estadoClass}">${estadoLabel}</span></td>
            <td>
                <div class="mc-toggle-wrap">
                    <button class="mc-toggle ${toggleClass}"
                            data-id="${m.analisisParametroId}"
                            data-analizado="${m.analizado}">
                        <i class="bi ${toggleIcon}"></i>
                        ${toggleLabel}
                    </button>
                </div>
            </td>
        </tr>`;
    }

    // ── Toggle analizado ───────────────────────────────────────────────────

    async function toggleAnalizado(btn) {
        const id       = btn.dataset.id;
        const anterior = btn.dataset.analizado === 'true';
        const nuevo    = !anterior;

        btn.disabled = true;
        btn.style.opacity = '0.6';

        try {
            const res = await fetch(`/api/mi-cola/${id}/analizado`, {
                method: 'PATCH',
                headers: FETCH_HDR()
            });
            if (!res.ok) throw new Error(res.status);

            // Actualizar datos locales
            for (const p of datosOriginales) {
                const muestra = p.muestras.find(m => String(m.analisisParametroId) === String(id));
                if (muestra) {
                    muestra.analizado = nuevo;
                    p.totalPendientes = p.muestras.filter(m => !m.analizado).length;
                    p.totalAnalizado  = p.muestras.filter(m =>  m.analizado).length;
                    break;
                }
            }

            renderTodo();
        } catch (e) {
            btn.disabled = false;
            btn.style.opacity = '';
            alert('Error al actualizar el estado. Intentá de nuevo.');
        }
    }

    // ── Filtros y búsqueda ─────────────────────────────────────────────────

    document.querySelectorAll('.mc-filter-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.mc-filter-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            filtroActual = btn.dataset.filtro;
            renderLista();
        });
    });

    let searchTimer;
    $('mcSearch').addEventListener('input', e => {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(() => {
            busqueda = e.target.value.trim();
            renderLista();
        }, 200);
    });

    $('mcRefresh').addEventListener('click', cargar);

    // ── Inicio ─────────────────────────────────────────────────────────────

    document.addEventListener('DOMContentLoaded', () => {
        if (typeof inicializarHeader === 'function') inicializarHeader();
        cargar();
    });

})();
