const API_URL = `${API_BASE}/api`;
const POR_PAGINA = 10;

// ── Estado por tabla ──
const tablas = {
    resoluciones: {
        url: `${API_URL}/resoluciones`,
        tbodyId: 'tablaResolucionesBody',
        filtroId: 'filtroResoluciones',
        pagId:    'paginadorResoluciones',
        colSpan:  3,
        data: [], filtro: '', pagina: 1,
        renderFila: r => `<td>${r.nombre}</td><td>${r.descripcion || '-'}</td>`,
        textoVacio: 'No hay resoluciones registradas.',
        textoBuscar: r => `${r.nombre} ${r.descripcion || ''}`,
        renderAccionesExtra: r => `<button class="btn-accion-info" onclick="verParametros(${r.id})" title="Ver parámetros"><i class="bi bi-eye"></i></button>`,
    },
    parametros: {
        url: `${API_URL}/parametros`,
        tbodyId: 'tablaParametrosBody',
        filtroId: 'filtroParametros',
        pagId:    'paginadorParametros',
        colSpan:  2,
        data: [], filtro: '', pagina: 1,
        renderFila: r => `<td>${r.nombre}</td>`,
        textoVacio: 'No hay parámetros cargados.',
        textoBuscar: r => r.nombre,
    },
    metodologias: {
        url: `${API_URL}/metodologias`,
        tbodyId: 'tablaMetodologiasBody',
        filtroId: 'filtroMetodologias',
        pagId:    'paginadorMetodologias',
        colSpan:  3,
        data: [], filtro: '', pagina: 1,
        renderFila: r => `<td>${r.nombre}</td><td>${r.descripcion || '-'}</td>`,
        textoVacio: 'No hay metodologías cargadas.',
        textoBuscar: r => `${r.nombre} ${r.descripcion || ''}`,
    },
    tiposmuestra: {
        url: `${API_URL}/tipos-muestra`,
        tbodyId: 'tablaTiposMuestraBody',
        filtroId: 'filtroTiposMuestra',
        pagId:    'paginadorTiposMuestra',
        colSpan:  3,
        data: [], filtro: '', pagina: 1,
        renderFila: r => `<td>${r.nombre}</td><td>${r.matriz?.nombre || '-'}</td>`,
        textoVacio: 'No hay tipos de muestra registrados.',
        textoBuscar: r => `${r.nombre} ${r.matriz?.nombre || ''}`,
    },
};

// ── Carga inicial ──
document.addEventListener('DOMContentLoaded', () => {
    inicializarHeader();
    Object.keys(tablas).forEach(nombre => {
        cargarTabla(nombre);
        document.getElementById(tablas[nombre].filtroId)
            .addEventListener('input', e => {
                tablas[nombre].filtro = e.target.value.toLowerCase().trim();
                tablas[nombre].pagina = 1;
                renderTabla(nombre);
            });
    });
    cargarNumeradores();
});

// ── Numeradores ──
async function cargarNumeradores() {
    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`${API_URL}/numeradores`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        renderNumeradores(data);
    } catch {
        document.getElementById('tablaNumeradoresBody').innerHTML =
            `<tr><td colspan="2" class="text-center text-danger" style="padding:16px;">Error al cargar los numeradores.</td></tr>`;
    }
}

function renderNumeradores(data) {
    document.getElementById('tablaNumeradoresBody').innerHTML = data.length === 0
        ? `<tr><td colspan="2" class="text-center" style="padding:16px;color:#6c757d;">Sin numeradores registrados.</td></tr>`
        : data.map(n => `
            <tr>
                <td><code style="font-size:13px;">${n.nombre}</code></td>
                <td>
                    <input type="number" class="unidad-inline-input" value="${n.valor}" min="0"
                           title="Presioná Enter o hacé clic fuera para guardar"
                           onblur="guardarNumerador(${n.id}, this)"
                           onkeydown="if(event.key==='Enter') this.blur();">
                </td>
            </tr>`).join('');
}

async function guardarNumerador(id, input) {
    const valor = parseInt(input.value, 10);
    if (isNaN(valor) || valor < 0) {
        input.classList.add('campo-error');
        setTimeout(() => input.classList.remove('campo-error'), 1500);
        return;
    }
    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`${API_URL}/numeradores/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify({ valor })
        });
        if (!res.ok) throw new Error();
        mostrarToast('Numerador actualizado correctamente.');
    } catch {
        mostrarToast('No se pudo actualizar el numerador.', 'danger');
    }
}

async function cargarTabla(nombre) {
    const t = tablas[nombre];
    const token = localStorage.getItem('token');
    try {
        const res = await fetch(t.url, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        t.data = await res.json();
        renderTabla(nombre);
    } catch (err) {
        console.error(`Error cargando ${nombre}:`, err);
        document.getElementById(t.tbodyId).innerHTML =
            `<tr><td colspan="${t.colSpan}" class="text-center text-danger">Error al cargar los datos</td></tr>`;
    }
}

function renderTabla(nombre) {
    const t = tablas[nombre];

    const filtrados = t.filtro
        ? t.data.filter(r => t.textoBuscar(r).toLowerCase().includes(t.filtro))
        : t.data;

    const totalPaginas = Math.max(1, Math.ceil(filtrados.length / POR_PAGINA));
    if (t.pagina > totalPaginas) t.pagina = totalPaginas;

    const inicio = (t.pagina - 1) * POR_PAGINA;
    const pagina = filtrados.slice(inicio, inicio + POR_PAGINA);

    const tbody = document.getElementById(t.tbodyId);

    if (filtrados.length === 0) {
        tbody.innerHTML = `<tr><td colspan="${t.colSpan}" class="text-center" style="padding:16px;color:#6c757d;">
            ${t.filtro ? 'Sin resultados para la búsqueda.' : t.textoVacio}
        </td></tr>`;
    } else {
        tbody.innerHTML = pagina.map(r => `
            <tr>
                ${t.renderFila(r)}
                <td>
                    <div class="tabla-acciones">
                        ${t.renderAccionesExtra ? t.renderAccionesExtra(r) : ''}
                        <button class="btn-accion-danger" onclick="eliminar('${nombre}', ${r.id})" title="Eliminar">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </td>
            </tr>`).join('');
    }

    renderPaginador(nombre, filtrados.length, totalPaginas);
}

function renderPaginador(nombre, total, totalPaginas) {
    const t = tablas[nombre];
    const inicio = Math.min((t.pagina - 1) * POR_PAGINA + 1, total);
    const fin    = Math.min(t.pagina * POR_PAGINA, total);

    const infoTexto = total === 0
        ? 'Sin resultados'
        : `Mostrando ${inicio}–${fin} de ${total}`;

    document.getElementById(t.pagId).innerHTML = `
        <span class="pag-info">${infoTexto}</span>
        <div class="pag-controles">
            <button class="pag-btn" onclick="cambiarPagina('${nombre}', ${t.pagina - 1})"
                ${t.pagina <= 1 ? 'disabled' : ''}>‹ Anterior</button>
            <span class="pag-pagina">${t.pagina} / ${totalPaginas}</span>
            <button class="pag-btn" onclick="cambiarPagina('${nombre}', ${t.pagina + 1})"
                ${t.pagina >= totalPaginas ? 'disabled' : ''}>Siguiente ›</button>
        </div>`;
}

function cambiarPagina(nombre, nuevaPagina) {
    tablas[nombre].pagina = nuevaPagina;
    renderTabla(nombre);
}

// ── Formularios ──
document.addEventListener('DOMContentLoaded', () => {
    cargarMatrices();

    document.getElementById('formParametro').addEventListener('submit', async e => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        const body = {
            nombre: document.getElementById('paramNombre').value.trim(),
        };
        try {
            const res = await fetch(`${API_URL}/parametros`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify(body),
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            e.target.reset();
            cargarTabla('parametros');
        } catch (err) {
            console.error('Error guardando parámetro:', err);
            mostrarToast('No se pudo guardar el parámetro.', 'danger');
        }
    });

    document.getElementById('formResolucion').addEventListener('submit', async e => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        const body = {
            nombre:      document.getElementById('resNombre').value.trim(),
            descripcion: document.getElementById('resOrganismo').value.trim(),
            matrizId:    Number(document.getElementById('resMatriz').value),
        };
        try {
            const res = await fetch(`${API_URL}/resoluciones`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify(body),
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            e.target.reset();
            cargarTabla('resoluciones');
        } catch (err) {
            console.error('Error guardando resolución:', err);
            mostrarToast('No se pudo guardar la resolución.', 'danger');
        }
    });

    document.getElementById('formTipoMuestra').addEventListener('submit', async e => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        const body = {
            nombre:   document.getElementById('tmNombre').value.trim(),
            matrizId: Number(document.getElementById('tmMatriz').value),
        };
        try {
            const res = await fetch(`${API_URL}/tipos-muestra`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify(body),
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            e.target.reset();
            cargarTabla('tiposmuestra');
        } catch (err) {
            console.error('Error guardando tipo de muestra:', err);
            mostrarToast('No se pudo guardar el tipo de muestra.', 'danger');
        }
    });

    document.getElementById('formMetodologia').addEventListener('submit', async e => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        const body = {
            nombre:      document.getElementById('metNombre').value.trim(),
            descripcion: document.getElementById('metReferencia').value.trim(),
        };
        try {
            const res = await fetch(`${API_URL}/metodologias`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify(body),
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            e.target.reset();
            cargarTabla('metodologias');
        } catch (err) {
            console.error('Error guardando metodología:', err);
            mostrarToast('No se pudo guardar la metodología.', 'danger');
        }
    });
});

async function cargarMatrices() {
    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`${API_URL}/matrices`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const matrices = await res.json();
        ['resMatriz', 'tmMatriz'].forEach(selectId => {
            const select = document.getElementById(selectId);
            if (!select) return;
            matrices.filter(m => m.activo).forEach(m => {
                const opt = document.createElement('option');
                opt.value = m.id;
                opt.textContent = m.nombre;
                select.appendChild(opt);
            });
        });
    } catch (err) {
        console.error('Error cargando matrices:', err);
    }
}

// ── Editor de parámetros de resolución ──
let _currentResolucionId = null;
let _currentDetalle = null;

async function verParametros(id) {
    _currentResolucionId = id;
    const overlay = document.getElementById('paramsOverlay');
    const titulo  = document.getElementById('paramsModalTitulo');
    const cuerpo  = document.getElementById('paramsModalCuerpo');

    titulo.textContent = 'Cargando...';
    cuerpo.innerHTML   = '<p class="params-loading">Cargando parámetros...</p>';
    overlay.classList.add('visible');

    await _cargarYRenderModalParams();
}

async function _cargarYRenderModalParams() {
    const token = localStorage.getItem('token');
    const cuerpo = document.getElementById('paramsModalCuerpo');
    try {
        const [resDetalle, resParams] = await Promise.all([
            fetch(`${API_URL}/resoluciones/${_currentResolucionId}/detalle`, { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch(`${API_URL}/parametros`, { headers: { 'Authorization': `Bearer ${token}` } }),
        ]);
        if (!resDetalle.ok || !resParams.ok) throw new Error('Error HTTP');
        const [detalle, todosLosParams] = await Promise.all([resDetalle.json(), resParams.json()]);
        _currentDetalle = detalle;

        document.getElementById('paramsModalTitulo').textContent = detalle.nombre;
        _renderEditorParams(detalle, todosLosParams);
    } catch (err) {
        console.error('Error cargando parámetros:', err);
        cuerpo.innerHTML = '<p class="params-error">Error al cargar los datos.</p>';
    }
}

function _renderEditorParams(detalle, todosLosParams) {
    const cuerpo = document.getElementById('paramsModalCuerpo');

    // Aplanar params incluidos deduplicados por param ID
    const includedMap = new Map();
    for (const destino of (detalle.destinos || [])) {
        for (const p of (destino.parametros || [])) {
            if (!includedMap.has(p.id)) includedMap.set(p.id, p);
        }
    }
    const included = [...includedMap.values()].sort((a, b) => a.nombre.localeCompare(b.nombre, "es"));
    const includedIds = new Set(included.map(p => p.id));
    const excluded = todosLosParams.filter(p => !includedIds.has(p.id)).sort((a, b) => a.nombre.localeCompare(b.nombre, "es"));

    const filasIncluidos = included.length > 0
        ? included.map(p => `
            <tr>
                <td>${p.nombre}</td>
                <td>
                    <input type="text" class="unidad-inline-input" value="${p.unidad || ''}"
                           placeholder="sin unidad"
                           onblur="actualizarUnidadParam(${p.id}, this.value)"
                           onkeydown="if(event.key==='Enter'){this.blur();}">
                </td>
                <td>${formatearLimite(p)}</td>
                <td>
                    <div class="tabla-acciones">
                        <button class="btn-accion-info" onclick="abrirEditorLimite(${p.id})" title="Editar límite">
                            <i class="bi bi-sliders"></i>
                        </button>
                        <button class="btn-accion-danger" onclick="quitarParametroResolucion(${p.id})" title="Quitar parámetro">
                            <i class="bi bi-dash-circle"></i>
                        </button>
                    </div>
                </td>
            </tr>`).join('')
        : `<tr><td colspan="4" class="params-vacio">No hay parámetros configurados.</td></tr>`;

    const filasExcluidos = excluded.length > 0
        ? excluded.map(p => `
            <tr data-nombre="${p.nombre.toLowerCase()}">
                <td>${p.nombre}</td>
                <td>
                    <input type="text" id="unidadInput_${p.id}" class="unidad-inline-input"
                           placeholder="ej: µg/L, mg/kg">
                </td>
                <td>
                    <button class="btn-accion-info" onclick="agregarParametroResolucion(${p.id})" title="Agregar parámetro">
                        <i class="bi bi-plus-circle"></i>
                    </button>
                </td>
            </tr>`).join('')
        : `<tr><td colspan="3" class="params-vacio">Todos los parámetros ya están incluidos.</td></tr>`;

    cuerpo.innerHTML = `
        <div class="params-section-label">Incluidos (${included.length})</div>
        <table class="tabla-config">
            <thead><tr><th>Parámetro</th><th>Unidad</th><th>Límite</th><th></th></tr></thead>
            <tbody>${filasIncluidos}</tbody>
        </table>

        <div class="params-divider"></div>

        <div class="params-section-label">Disponibles para agregar</div>
        <input type="text" class="tabla-filtro params-buscador" placeholder="Buscar parámetro..."
               oninput="filtrarParamsExcluidos(this.value)">
        <table class="tabla-config">
            <thead><tr><th>Parámetro</th><th>Unidad para esta resolución</th><th></th></tr></thead>
            <tbody id="tbodyParamsExcluidos">${filasExcluidos}</tbody>
        </table>`;
}

function filtrarParamsExcluidos(valor) {
    const q = valor.toLowerCase().trim();
    document.querySelectorAll('#tbodyParamsExcluidos tr[data-nombre]').forEach(tr => {
        tr.style.display = tr.dataset.nombre.includes(q) ? '' : 'none';
    });
}

async function agregarParametroResolucion(parametroId) {
    const token = localStorage.getItem('token');
    const unidad = (document.getElementById(`unidadInput_${parametroId}`)?.value || '').trim();
    try {
        const res = await fetch(`${API_URL}/resoluciones/${_currentResolucionId}/parametros/${parametroId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify({ unidad: unidad || null }),
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        await _cargarYRenderModalParams();
    } catch (err) {
        console.error('Error agregando parámetro:', err);
        mostrarToast('No se pudo agregar el parámetro.', 'danger');
    }
}

async function actualizarUnidadParam(parametroId, unidad) {
    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`${API_URL}/resoluciones/${_currentResolucionId}/parametros/${parametroId}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify({ unidad: unidad.trim() || null }),
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        mostrarToast('Unidad actualizada.', 'success');
    } catch (err) {
        console.error('Error actualizando unidad:', err);
        mostrarToast('No se pudo actualizar la unidad.', 'danger');
    }
}

async function quitarParametroResolucion(parametroId) {
    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`${API_URL}/resoluciones/${_currentResolucionId}/parametros/${parametroId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` },
        });
        if (res.status === 409) {
            mostrarToast('No se pudo desactivar: error de integridad inesperado.', 'danger');
            return;
        }
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        mostrarToast('Parámetro desactivado. El historial de análisis no se ve afectado.', 'success');
        await _cargarYRenderModalParams();
    } catch (err) {
        console.error('Error quitando parámetro:', err);
        mostrarToast('No se pudo desactivar el parámetro.', 'danger');
    }
}

function formatearLimite(p) {
    if (!p.tipoLimite) return p.limiteTexto || '-';
    if (p.tipoLimite === 'RANGO')    return `${p.valorMinimo ?? '?'} – ${p.valorMaximo ?? '?'}`;
    if (p.tipoLimite === 'MAX')      return `≤ ${p.valorMaximo}`;
    if (p.tipoLimite === 'MIN')      return `≥ ${p.valorMinimo}`;
    if (p.tipoLimite === 'TEXTO')    return p.limiteTexto || '-';
    if (p.tipoLimite === 'AUSENCIA') return 'Ausencia';
    if (p.tipoLimite === 'NE')       return 'No exigido';
    if (p.tipoLimite === 'NL')       return '—';
    return '-';
}

// ── Editor de límite de parámetro ──
function abrirEditorLimite(parametroId) {
    let p = null;
    for (const destino of (_currentDetalle?.destinos || [])) {
        p = (destino.parametros || []).find(x => x.id === parametroId);
        if (p) break;
    }
    if (!p) return;

    document.getElementById('limiteParamId').value          = parametroId;
    document.getElementById('limiteModalTitulo').textContent = `Límite — ${p.nombre}`;
    document.getElementById('limiteTipo').value             = p.tipoLimite || 'NE';
    document.getElementById('limiteMin').value              = p.valorMinimo ?? '';
    document.getElementById('limiteMax').value              = p.valorMaximo ?? '';
    document.getElementById('limiteTextoInput').value       = p.limiteTexto || '';
    onTipoLimiteCambio();
    document.getElementById('limiteOverlay').classList.add('visible');
}

function onTipoLimiteCambio() {
    const tipo = document.getElementById('limiteTipo').value;
    document.getElementById('limiteGrupoMax').style.display   = ['MAX', 'RANGO'].includes(tipo) ? '' : 'none';
    document.getElementById('limiteGrupoMin').style.display   = ['MIN', 'RANGO'].includes(tipo) ? '' : 'none';
    document.getElementById('limiteGrupoTexto').style.display = tipo === 'TEXTO' ? '' : 'none';
}

async function guardarLimiteParam() {
    const parametroId = document.getElementById('limiteParamId').value;
    const tipoLimite  = document.getElementById('limiteTipo').value;
    const minVal      = document.getElementById('limiteMin').value;
    const maxVal      = document.getElementById('limiteMax').value;
    const valorMinimo = minVal !== '' ? parseFloat(minVal) : null;
    const valorMaximo = maxVal !== '' ? parseFloat(maxVal) : null;
    const limiteTexto = document.getElementById('limiteTextoInput').value.trim() || null;

    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`${API_URL}/resoluciones/${_currentResolucionId}/parametros/${parametroId}/limite`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify({ tipoLimite, valorMinimo, valorMaximo, limiteTexto }),
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        document.getElementById('limiteOverlay').classList.remove('visible');
        mostrarToast('Límite actualizado.', 'success');
        await _cargarYRenderModalParams();
    } catch (err) {
        console.error('Error guardando límite:', err);
        mostrarToast('No se pudo guardar el límite.', 'danger');
    }
}

function cerrarLimiteOverlay(event) {
    if (event.target === event.currentTarget) {
        event.currentTarget.classList.remove('visible');
    }
}

function cerrarModalParams(event) {
    if (event.target === event.currentTarget) {
        event.currentTarget.classList.remove('visible');
    }
}

// ── Eliminar ──
async function eliminar(nombre, id) {
    const labels = { resoluciones: 'resolución', parametros: 'parámetro', metodologias: 'metodología' };
    const ok = await UI.confirmar({ titulo: `¿Eliminar esta ${labels[nombre]}?`, subtexto: 'Esta acción no se puede deshacer.', textoConfirmar: 'Eliminar', tipo: 'danger' });
    if (!ok) return;

    const endpoints = {
        resoluciones: `${API_URL}/resoluciones/${id}`,
        parametros:   `${API_URL}/parametros/${id}`,
        metodologias: `${API_URL}/metodologias/${id}`,
    };

    const token = localStorage.getItem('token');
    try {
        const res = await fetch(endpoints[nombre], {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` },
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        cargarTabla(nombre);
    } catch (err) {
        console.error(`Error eliminando ${nombre}:`, err);
        mostrarToast('No se pudo eliminar el elemento.', 'danger');
    }
}
