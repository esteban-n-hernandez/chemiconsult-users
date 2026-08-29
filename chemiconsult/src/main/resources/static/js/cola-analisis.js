(function () {
    'use strict';

    const TOKEN     = () => localStorage.getItem('token') || '';
    const FETCH_HDR = () => ({ 'Content-Type': 'application/json', 'Authorization': `Bearer ${TOKEN()}` });

    const $ = id => document.getElementById(id);
    const esc = s => (s ?? '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');

    let datosOriginales = [];
    let filtroEstado    = 'todos';
    let filtroUserId    = null;   // null = "yo"; '*' = todos
    let busqueda        = '';
    const openParamIds  = new Set();  // persist accordion open state across re-renders

    const FLASK_SVG = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16"><path d="M6 0a.5.5 0 0 0 0 1h1v2.013A2.5 2.5 0 0 0 5.3 4.9L2.294 8.907A2 2 0 0 0 3.9 12h8.2a2 2 0 0 0 1.606-3.193L10.7 4.9A2.5 2.5 0 0 0 9 3.013V1h1a.5.5 0 0 0 0-1H6z"/></svg>`;

    const miUserId       = localStorage.getItem('userId');
    const miUserNombre   = localStorage.getItem('userName') || localStorage.getItem('userEmail') || 'Yo';

    // ── Carga ──────────────────────────────────────────────────────────────

    async function cargar() {
        mostrarSkeleton();
        try {
            const res = await fetch('/api/mi-cola/global', { headers: FETCH_HDR() });
            if (!res.ok) throw new Error(res.status);
            datosOriginales = await res.json();
            construirUserPills();
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

    // ── Filtro de usuario ──────────────────────────────────────────────────

    function construirUserPills() {
        const contenedor = $('mcUserPills');
        if (!contenedor) return;

        // Recopilar responsables únicos
        const mapa = new Map(); // id → nombre
        datosOriginales.forEach(p => {
            if (p.responsableId != null) {
                mapa.set(String(p.responsableId), p.responsableNombre || String(p.responsableId));
            }
        });

        const pills = [];

        // Pill "Todos" (solo si hay más de un responsable)
        if (mapa.size > 1) {
            pills.push(crearPill('*', 'Todos', filtroUserId === '*'));
        }

        // Pill del usuario actual primero
        const miIdStr = String(miUserId);
        if (mapa.has(miIdStr)) {
            const activo = filtroUserId === null || filtroUserId === miIdStr;
            pills.push(crearPill(miIdStr, 'Yo', activo));
            mapa.delete(miIdStr);
        }

        // Resto de responsables
        mapa.forEach((nombre, id) => {
            pills.push(crearPill(id, nombre, filtroUserId === id));
        });

        contenedor.innerHTML = '';
        pills.forEach(p => contenedor.appendChild(p));

        // Si no hay filtro aún y el usuario existe, lo activamos
        if (filtroUserId === null) {
            const yo = contenedor.querySelector(`[data-uid="${miIdStr}"]`);
            if (yo) yo.classList.add('active');
        }
    }

    function crearPill(uid, label, activo) {
        const btn = document.createElement('button');
        btn.className = 'mc-user-pill' + (activo ? ' active' : '');
        btn.dataset.uid = uid;
        btn.textContent = label;
        btn.addEventListener('click', () => {
            document.querySelectorAll('.mc-user-pill').forEach(p => p.classList.remove('active'));
            btn.classList.add('active');
            filtroUserId = uid === miIdStr() ? null : uid;
            renderTodo();
        });
        return btn;
    }

    function miIdStr() { return String(miUserId); }

    function datosFiltradosPorUsuario() {
        if (filtroUserId === '*') return datosOriginales;
        const uid = filtroUserId ?? miIdStr();
        return datosOriginales.filter(p => String(p.responsableId) === uid);
    }

    // ── Render principal ───────────────────────────────────────────────────

    function renderTodo() {
        actualizarStats();
        renderLista();
    }

    function actualizarStats() {
        const datos = datosFiltradosPorUsuario();
        let pend = 0, anal = 0, conf = 0, obs = 0;
        datos.forEach(p => {
            pend += p.totalPendientes;
            anal += p.totalAnalizado;
            conf += p.totalConfirmado;
            obs  += p.totalObservado;
        });
        setText('statPendientes', pend);
        setText('statPendientesPlural', pend === 1 ? '' : 's');
        setText('statAnalizado', anal);
        setText('statAnalizadoPlural', anal === 1 ? '' : 's');
        setText('statConfirmado', conf);
        setText('statConfirmadoPlural', conf === 1 ? '' : 's');
        setText('statRepetir', obs);
        $('mcStats').style.display = 'flex';
    }

    function setText(id, val) {
        const el = $(id);
        if (el) el.textContent = val;
    }

    function renderLista() {
        const lista  = $('mcLista');
        const busq   = busqueda.toLowerCase();
        const filtro = filtroEstado;

        const datos = datosFiltradosPorUsuario().filter(p => {
            // Excluir siempre los confirmados de la vista
            const visibles = p.muestras.filter(m => m.estadoAnalisis !== 'CONFIRMADO');
            if (visibles.length === 0) return false;

            const hayText = !busq ||
                p.parametroNombre.toLowerCase().includes(busq) ||
                visibles.some(m =>
                    (m.nroProtocolo || '').toLowerCase().includes(busq) ||
                    (m.clienteNombre || '').toLowerCase().includes(busq) ||
                    (m.puntoMuestreo || '').toLowerCase().includes(busq)
                );
            if (!hayText) return false;

            if (filtro === 'todos') return true;
            return visibles.some(m => m.estadoAnalisis === filtro);
        });

        if (datos.length === 0) {
            lista.innerHTML = '';
            $('mcEmpty').style.display = 'block';
            return;
        }
        $('mcEmpty').style.display = 'none';

        lista.innerHTML = datos.map(p => renderParametroBloque(p)).join('');

        lista.querySelectorAll('.mc-param-head').forEach(head => {
            const pid     = head.dataset.parametroId;
            const body    = head.nextElementSibling;
            const chevron = head.querySelector('.mc-chevron');
            // Restore open state from before re-render
            if (openParamIds.has(pid)) {
                body.classList.add('open');
                chevron.classList.add('open');
            }
            head.addEventListener('click', () => {
                const abierto = body.classList.toggle('open');
                chevron.classList.toggle('open', abierto);
                if (abierto) openParamIds.add(pid);
                else openParamIds.delete(pid);
            });
        });

        lista.querySelectorAll('.mc-estado-btn').forEach(btn => {
            btn.addEventListener('click', e => {
                e.stopPropagation();
                abrirEstadoDrop(btn);
            });
        });
    }

    function renderParametroBloque(p) {
        // Los confirmados no se muestran en la lista
        const muestrasNoConf = p.muestras.filter(m => m.estadoAnalisis !== 'CONFIRMADO');
        const muestrasParaRender = filtroEstado === 'todos'
            ? muestrasNoConf
            : muestrasNoConf.filter(m => m.estadoAnalisis === filtroEstado);

        const pend = muestrasNoConf.filter(m => m.estadoAnalisis === 'PENDIENTE').length;
        const anal = muestrasNoConf.filter(m => m.estadoAnalisis === 'ANALIZADO').length;
        const rep  = muestrasNoConf.filter(m => m.estadoAnalisis === 'REPETIR').length;

        const badges = [
            pend > 0 ? `<span class="mc-badge mc-badge-pend"><i class="bi bi-hourglass-split"></i> ${pend}</span>`   : '',
            anal > 0 ? `<span class="mc-badge mc-badge-anal"><i class="bi bi-check2-circle"></i> ${anal}</span>`      : '',
            rep  > 0 ? `<span class="mc-badge mc-badge-rep"><i class="bi bi-arrow-repeat"></i> ${rep}</span>`          : '',
        ].join('');

        const responsableLabel = (filtroUserId === '*' && p.responsableNombre)
            ? `<span class="mc-param-resp">${esc(p.responsableNombre)}</span>` : '';

        return `
        <div class="mc-param-block">
            <div class="mc-param-head" data-parametro-id="${p.parametroId}">
                <div class="mc-param-info">
                    <div class="mc-param-icon">${FLASK_SVG}</div>
                    <div>
                        <div class="mc-param-nombre">${esc(p.parametroNombre)} ${responsableLabel}</div>
                        ${p.unidad ? `<div class="mc-param-unidad">${esc(p.unidad)}</div>` : ''}
                    </div>
                </div>
                <div class="mc-param-badges">${badges}</div>
                <i class="bi bi-chevron-down mc-chevron"></i>
            </div>
            <div class="mc-param-body">
                <table class="mc-tabla">
                    <thead>
                        <tr>
                            <th>N° Protocolo</th>
                            <th style="text-align:center">Estado análisis</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${muestrasParaRender.map(m => renderMuestraRow(m)).join('')}
                    </tbody>
                </table>
            </div>
        </div>`;
    }

    function renderMuestraRow(m) {
        const ea = m.estadoAnalisis || 'PENDIENTE';
        const eaClass = `mc-ea-${ea === 'REPETIR' ? 'repetir' : ea.toLowerCase()}`;
        const eaLabel = labelEstadoAnalisis(ea);
        const eaIcon  = iconEstadoAnalisis(ea);

        return `
        <tr>
            <td><span class="mc-proto">${esc(m.nroProtocolo || '—')}</span></td>
            <td>
                <div class="mc-toggle-wrap">
                    <button class="mc-estado-btn ${eaClass}"
                            data-id="${m.analisisParametroId}"
                            data-estado="${ea}">
                        <i class="bi ${eaIcon}"></i> ${eaLabel}
                        <i class="bi bi-chevron-down" style="font-size:9px;margin-left:2px;opacity:.7"></i>
                    </button>
                </div>
            </td>
        </tr>`;
    }

    function labelEstadoAnalisis(estado) {
        return { PENDIENTE: 'Pendiente', ANALIZADO: 'Analizado', CONFIRMADO: 'Confirmado', REPETIR: ' Repetir' }[estado] || estado;
    }

    function iconEstadoAnalisis(estado) {
        return {
            PENDIENTE:  'bi-hourglass-split',
            ANALIZADO:  'bi-check2-circle',
            CONFIRMADO: 'bi-patch-check-fill',
            REPETIR:    'bi-arrow-repeat',
        }[estado] || 'bi-hourglass-split';
    }

    // ── Dropdown de estado análisis ────────────────────────────────────────

    let dropActivoBtn = null;

    function abrirEstadoDrop(btn) {
        const drop = $('mcEstadoDrop');
        if (dropActivoBtn === btn && drop.style.display === 'flex') {
            cerrarEstadoDrop();
            return;
        }
        dropActivoBtn = btn;

        const rect = btn.getBoundingClientRect();
        drop.style.display  = 'flex';
        drop.style.position = 'fixed';
        drop.style.top      = (rect.bottom + 4) + 'px';
        drop.style.left     = rect.left + 'px';
    }

    function cerrarEstadoDrop() {
        $('mcEstadoDrop').style.display = 'none';
        dropActivoBtn = null;
    }

    $('mcEstadoDrop').querySelectorAll('.mc-estado-opt').forEach(opt => {
        opt.addEventListener('click', async () => {
            if (!dropActivoBtn) return;
            const id          = dropActivoBtn.dataset.id;
            const nuevoEstado = opt.dataset.estado;

            if (nuevoEstado === 'CONFIRMADO') {
                cerrarEstadoDrop();
                await abrirModalConfirmar(id);
                return;
            }

            await cambiarEstado(id, nuevoEstado);
            // After re-render, reposition dropdown on the new button (if still visible)
            const newBtn = document.querySelector(`.mc-estado-btn[data-id="${id}"]`);
            if (newBtn) {
                dropActivoBtn = null;  // reset so abrirEstadoDrop doesn't toggle-close
                abrirEstadoDrop(newBtn);
            } else {
                cerrarEstadoDrop();
            }
        });
    });

    document.addEventListener('click', e => {
        if (!e.target.closest('.mc-estado-btn') && !e.target.closest('#mcEstadoDrop')) {
            cerrarEstadoDrop();
        }
    });

    // ── Cambiar estado ─────────────────────────────────────────────────────

    async function cambiarEstado(id, nuevoEstado) {
        try {
            const res = await fetch(`/api/mi-cola/${id}/estado`, {
                method: 'PATCH',
                headers: FETCH_HDR(),
                body: JSON.stringify({ estado: nuevoEstado }),
            });
            if (!res.ok) throw new Error(res.status);

            // Actualizar datos locales
            for (const p of datosOriginales) {
                const muestra = p.muestras.find(m => String(m.analisisParametroId) === String(id));
                if (muestra) {
                    muestra.estadoAnalisis = nuevoEstado;
                    // Recalcular conteos del bloque
                    p.totalPendientes = p.muestras.filter(m => m.estadoAnalisis === 'PENDIENTE').length;
                    p.totalAnalizado  = p.muestras.filter(m => m.estadoAnalisis === 'ANALIZADO').length;
                    p.totalConfirmado = p.muestras.filter(m => m.estadoAnalisis === 'CONFIRMADO').length;
                    p.totalObservado  = p.muestras.filter(m => m.estadoAnalisis === 'REPETIR').length;
                    break;
                }
            }

            renderTodo();
        } catch (e) {
            alert('Error al actualizar el estado. Intentá de nuevo.');
        }
    }

    // ── Filtros y búsqueda ─────────────────────────────────────────────────

    document.querySelectorAll('.mc-filter-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.mc-filter-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            filtroEstado = btn.dataset.filtro;
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

    // ── Modal de confirmación de resultado ────────────────────────────────

    let modalAnalisisParametroId = null;
    let modalAnalisisId          = null;
    let modalParametroId         = null;

    async function abrirModalConfirmar(analisisParametroId) {
        let targetMuestra = null;
        let targetParam   = null;
        for (const p of datosOriginales) {
            const m = p.muestras.find(m => String(m.analisisParametroId) === String(analisisParametroId));
            if (m) { targetMuestra = m; targetParam = p; break; }
        }
        if (!targetMuestra || !targetParam) return;

        modalAnalisisParametroId = analisisParametroId;
        modalAnalisisId          = targetMuestra.analisisId;
        modalParametroId         = targetParam.parametroId;

        $('mcModalParamNombre').textContent = targetParam.parametroNombre || '—';
        $('mcModalProto').textContent       = targetMuestra.nroProtocolo  || '—';
        $('mcModalCliente').textContent     = targetMuestra.clienteNombre || '—';
        $('mcModalPunto').textContent       = targetMuestra.puntoMuestreo || '—';
        $('mcModalUnidad').textContent      = targetParam.unidad          || '—';

        const unidadInline = $('mcModalUnidadInline');
        if (targetParam.unidad) {
            unidadInline.textContent = `(${targetParam.unidad})`;
            unidadInline.style.display = '';
        } else {
            unidadInline.style.display = 'none';
        }

        $('mcModalResultado').value    = '';
        $('mcModalObservacion').value  = '';
        $('mcModalMetodo').innerHTML   = '<option value="">Cargando…</option>';
        $('mcModalLimites').innerHTML  = '<span class="mc-modal-limites-empty">Cargando…</span>';
        $('mcModalGuardarBtn').disabled = false;
        $('mcModalGuardarBtn').innerHTML = '<i class="bi bi-patch-check-fill"></i> Confirmar';

        $('mcModalLoading').classList.remove('d-none');
        $('mcModalConfirmar').classList.add('visible');

        try {
            const detalleRes = await fetch(`/api/estudios/${modalAnalisisId}/detalle`, { headers: FETCH_HDR() });
            if (!detalleRes.ok) throw new Error(`Error ${detalleRes.status} al cargar el detalle`);
            const detalle = await detalleRes.json();

            const paramDet = (detalle.parametros || []).find(p => String(p.id) === String(modalParametroId));

            $('mcModalResultado').value   = paramDet?.valorResultado || '';
            $('mcModalObservacion').value = paramDet?.observacion    || '';

            renderModalLimites(paramDet?.limites || []);

            const matrizId = detalle.matrizId || '';
            const metodUrl = `/api/parametros/${modalParametroId}/metodologias${matrizId ? `?matrizId=${matrizId}` : ''}`;
            const metodRes = await fetch(metodUrl, { headers: FETCH_HDR() });
            if (metodRes.ok) {
                const metodologias = await metodRes.json();
                renderModalMetodologias(metodologias, paramDet?.metodologiaId);
            } else {
                const sel = $('mcModalMetodo');
                sel.innerHTML = '<option value="">Sin metodología</option>';
                if (paramDet?.metodologiaId) {
                    const opt = document.createElement('option');
                    opt.value       = paramDet.metodologiaId;
                    opt.textContent = paramDet.metodologiaNombre || String(paramDet.metodologiaId);
                    opt.selected    = true;
                    sel.appendChild(opt);
                }
            }
        } catch (e) {
            $('mcModalLimites').innerHTML = `<span class="mc-modal-limites-empty" style="color:var(--rojo,#ef4444)">Error: ${esc(e.message)}</span>`;
            $('mcModalMetodo').innerHTML  = '<option value="">Error al cargar</option>';
        } finally {
            $('mcModalLoading').classList.add('d-none');
        }
    }

    function esLimiteAusenciaModal(tipo, texto) {
        if (tipo === 'AUSENCIA') return true;
        if (tipo === 'TEXTO' && texto && texto.trim().toLowerCase() === 'ausente') return true;
        return false;
    }

    function parsearRangoTextoModal(texto) {
        if (!texto) return null;
        const parts = texto.replace(/\s/g, '').split('/');
        if (parts.length === 2) {
            const min = parseFloat(parts[0].replace(',', '.'));
            const max = parseFloat(parts[1].replace(',', '.'));
            if (!isNaN(min) && !isNaN(max)) return { min, max };
        }
        return null;
    }

    function renderModalLimites(limites) {
        const container = $('mcModalLimites');
        if (!limites || limites.length === 0) {
            container.innerHTML = '<span class="mc-modal-limites-empty">Sin límites definidos para esta muestra</span>';
            return;
        }
        container.innerHTML = `
            <table class="mc-limites-tabla">
                <thead>
                    <tr>
                        <th>Normativa</th>
                        <th>Límite</th>
                        <th class="limite-estado-th">Estado</th>
                    </tr>
                </thead>
                <tbody>
                    ${limites.map(l => {
                        const esAus = esLimiteAusenciaModal(l.tipoLimite, l.limiteTexto);
                        let limiteStr;
                        if (esAus) limiteStr = 'Ausente';
                        else if (l.tipoLimite === 'MAX') limiteStr = `≤ ${l.limiteMax}`;
                        else if (l.tipoLimite === 'MIN') limiteStr = `≥ ${l.limiteMin}`;
                        else if (l.tipoLimite === 'RANGO' && l.limiteMin != null && l.limiteMax != null) limiteStr = `${l.limiteMin} – ${l.limiteMax}`;
                        else limiteStr = l.limiteTexto || '—';
                        return `
                        <tr data-tipo="${esAus ? 'AUSENCIA' : (l.tipoLimite || '')}"
                            data-min="${l.limiteMin ?? ''}"
                            data-max="${l.limiteMax ?? ''}"
                            data-texto="${l.limiteTexto ?? ''}">
                            <td>${esc(l.origenNombre || '—')}</td>
                            <td>${esc(limiteStr)}</td>
                            <td class="limite-estado-td"><span class="limite-estado-badge">—</span></td>
                        </tr>`;
                    }).join('')}
                </tbody>
            </table>`;
        validarLimitesModal();
    }

    function validarLimitesModal() {
        const rawVal = ($('mcModalResultado').value || '').trim();
        const vLower = rawVal.toLowerCase();
        const rows   = $('mcModalLimites')?.querySelectorAll('tbody tr[data-tipo]') || [];

        rows.forEach(tr => {
            const badge  = tr.querySelector('.limite-estado-badge');
            if (!badge) return;

            const tipo   = tr.dataset.tipo;
            const texto  = tr.dataset.texto;
            const minStr = tr.dataset.min;
            const maxStr = tr.dataset.max;

            if (!rawVal) {
                badge.className = 'limite-estado-badge nd';
                badge.textContent = '—';
                return;
            }

            let cumple = null;

            if (tipo === 'AUSENCIA') {
                if (vLower === 'ausente') cumple = true;
                else if (vLower === 'presente' || vLower === 'presencia') cumple = false;
                else { const n = parseFloat(rawVal.replace(',', '.')); if (!isNaN(n)) cumple = false; }
            } else if (tipo === 'TEXTO') {
                const rango = parsearRangoTextoModal(texto);
                if (rango) {
                    if (vLower === 'presencia' || vLower === 'presente') { cumple = false; }
                    else { const n = parseFloat(rawVal.replace(',', '.')); cumple = isNaN(n) ? null : (n >= rango.min && n <= rango.max); }
                }
            } else {
                if (vLower === 'presencia' || vLower === 'presente') {
                    cumple = false;
                } else {
                    const valor  = parseFloat(rawVal.replace(',', '.'));
                    if (!isNaN(valor)) {
                        const min = minStr !== '' ? parseFloat(minStr.replace(',', '.')) : -Infinity;
                        const max = maxStr !== '' ? parseFloat(maxStr.replace(',', '.')) : Infinity;
                        if (tipo === 'MAX')   cumple = valor <= max;
                        else if (tipo === 'MIN')   cumple = valor >= min;
                        else if (tipo === 'RANGO') cumple = valor >= min && valor <= max;
                    }
                }
            }

            badge.className = cumple === true  ? 'limite-estado-badge si'
                            : cumple === false ? 'limite-estado-badge no'
                            :                   'limite-estado-badge nd';
            badge.textContent = cumple === true ? 'Cumple' : cumple === false ? 'No cumple' : 'Sin evaluar';
        });
    }

    function renderModalMetodologias(metodologias, currentId) {
        const wrap = $('mcModalMetodo').parentElement;
        const sel  = $('mcModalMetodo');
        const lista = metodologias || [];

        if (lista.length <= 1) {
            const nombre = lista.length === 1 ? (lista[0].nombre || lista[0].name || '—') : 'Sin metodología';
            const id     = lista.length === 1 ? lista[0].id : '';
            wrap.innerHTML = `
                <label>Metodología</label>
                <div class="form-control-custom" style="opacity:.8;cursor:default;user-select:text;">${esc(nombre)}</div>
                <input type="hidden" id="mcModalMetodo" value="${id}">`;
        } else {
            sel.innerHTML = '<option value="">Sin metodología</option>';
            lista.forEach(m => {
                const opt = document.createElement('option');
                opt.value       = m.id;
                opt.textContent = m.nombre || m.name || String(m.id);
                if (String(m.id) === String(currentId)) opt.selected = true;
                sel.appendChild(opt);
            });
        }
    }

    function cerrarModalConfirmar() {
        $('mcModalConfirmar').classList.remove('visible');
        modalAnalisisParametroId = null;
        modalAnalisisId          = null;
        modalParametroId         = null;
    }

    async function guardarModalConfirmar() {
        const valorResultado = $('mcModalResultado').value.trim();
        const observacion    = $('mcModalObservacion').value.trim();
        const metodVal       = $('mcModalMetodo').value;
        const metodologiaId  = metodVal ? Number(metodVal) : null;

        const btn = $('mcModalGuardarBtn');
        btn.disabled = true;
        btn.innerHTML = '<i class="bi bi-hourglass-split"></i> Guardando…';

        try {
            const res = await fetch(`/api/estudios/${modalAnalisisId}/resultados`, {
                method:  'PUT',
                headers: FETCH_HDR(),
                body:    JSON.stringify([{ parametroId: modalParametroId, valorResultado, observacion, metodologiaId }]),
            });
            if (!res.ok) throw new Error(`Error ${res.status} al guardar el resultado`);

            await cambiarEstado(modalAnalisisParametroId, 'CONFIRMADO');
            cerrarModalConfirmar();
        } catch (e) {
            alert('Error: ' + e.message);
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-patch-check-fill"></i> Confirmar';
        }
    }

    $('mcModalCerrar').addEventListener('click', cerrarModalConfirmar);
    $('mcModalCancelarBtn').addEventListener('click', cerrarModalConfirmar);
    $('mcModalGuardarBtn').addEventListener('click', guardarModalConfirmar);
    $('mcModalResultado').addEventListener('input', validarLimitesModal);
    $('mcModalConfirmar').addEventListener('click', e => {
        if (e.target === $('mcModalConfirmar')) cerrarModalConfirmar();
    });
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape' && $('mcModalConfirmar').classList.contains('visible')) cerrarModalConfirmar();
    });

    // ── Inicio ─────────────────────────────────────────────────────────────

    document.addEventListener('DOMContentLoaded', () => {
        if (typeof inicializarHeader === 'function') inicializarHeader();
        cargar();
    });

})();
