"use strict";

// ── Header ──
const rol    = (localStorage.getItem("userRole") || "").toUpperCase();
const nombre = localStorage.getItem("userName") || localStorage.getItem("userEmail") || "Usuario";
const iniciales = nombre.split(" ").map(p => p[0]).join("").substring(0, 2).toUpperCase();
document.getElementById("header-nombre").textContent = nombre;
document.getElementById("header-rol").textContent    = rol === "ROLE_IT" ? "IT" : "Empleado";
document.getElementById("header-avatar").textContent = iniciales;
const hoy = new Date().toLocaleDateString("es-AR", { weekday:"long", year:"numeric", month:"long", day:"numeric" });
document.getElementById("fecha-hoy").textContent = hoy.charAt(0).toUpperCase() + hoy.slice(1);

// ── Fetch autenticado ──
async function apiFetch(url, opts = {}) {
    const token = localStorage.getItem("token");
    const res = await fetch(url, {
        ...opts,
        headers: { ...(opts.headers || {}), "Authorization": `Bearer ${token}` },
    });
    if (res.status === 401) { window.location.href = "login.html"; }
    return res;
}

// ── Toast ──
function toast(msg, tipo = "success") {
    const el  = document.getElementById("toastFact");
    const ico = document.getElementById("toastIcon");
    document.getElementById("toastMsg").textContent = msg;
    ico.className = tipo === "error"
        ? "bi bi-x-circle-fill danger"
        : "bi bi-check-circle-fill success";
    el.style.display = "flex";
    setTimeout(() => el.style.display = "none", 3500);
}

// ── Helpers ──
function formatPrecio(val) {
    if (!val && val !== 0) return "$0";
    return "$" + Number(val).toLocaleString("es-AR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatFecha(iso) {
    if (!iso) return "—";
    const [y, m, d] = iso.split("-");
    return `${d}/${m}/${y}`;
}

function esc(str) {
    return String(str || "").replace(/[&<>"']/g,
        c => ({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":`&#39;` }[c]));
}

function badgeEstado(estado) {
    const cls = estado === "AUTORIZADA" ? "badge-autorizada"
              : estado === "RECHAZADA"  ? "badge-rechazada"
              : "badge-anulada";
    const ico = estado === "AUTORIZADA" ? "bi-check-circle-fill"
              : estado === "RECHAZADA"  ? "bi-x-circle-fill"
              : "bi-slash-circle-fill";
    return `<span class="badge-fact ${cls}"><i class="bi ${ico}"></i> ${estado}</span>`;
}

function tipoBadge(tipo) {
    return `<span class="tipo-badge tipo-${tipo}">${tipo}</span>`;
}

function nroFmt(pv, num) {
    return `${String(pv).padStart(4,"0")}-${String(num).padStart(8,"0")}`;
}

function condIvaLabel(c) {
    const m = {
        RESPONSABLE_INSCRIPTO: "Responsable Inscripto",
        MONOTRIBUTISTA:        "Monotributista",
        EXENTO:                "Exento",
        CONSUMIDOR_FINAL:      "Consumidor Final",
        NO_RESPONSABLE:        "No Responsable",
    };
    return m[c] || c || "—";
}

// ================================================================
// LISTA DE FACTURAS
// ================================================================

let todasFacturas = [];
let filtroEstado  = "";
let factPagina    = 1;
let factPageSize  = 10;

async function cargarFacturas() {
    try {
        const res = await apiFetch(`${API_BASE}/api/factura`);
        if (!res.ok) throw new Error();
        todasFacturas = await res.json();
        factPagina = 1;
        renderTabla();
    } catch {
        document.getElementById("factTablaBody").innerHTML =
            `<tr><td colspan="8" style="text-align:center;color:#ef4444;padding:20px;">Error al cargar facturas</td></tr>`;
    }
}

function renderTabla() {
    const tbody = document.getElementById("factTablaBody");
    const lista = filtroEstado
        ? todasFacturas.filter(f => f.estado === filtroEstado)
        : todasFacturas;

    if (!lista.length) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:24px;color:var(--text-secondary);">Sin comprobantes</td></tr>`;
        renderPaginacion(0);
        return;
    }

    const total   = lista.length;
    const desde   = (factPagina - 1) * factPageSize;
    const hasta   = Math.min(desde + factPageSize, total);
    const pagina  = lista.slice(desde, hasta);

    tbody.innerHTML = pagina.map(f => {
        const nroCol = f.esExterna
            ? (f.numero && f.numero > 0
                ? `<span style="font-family:monospace;font-size:.82rem;">${nroFmt(f.puntoVenta || 0, f.numero)}</span>
                   <span title="Adjuntada externamente" style="margin-left:4px;font-size:.7rem;
                     color:var(--color-text-secondary);"><i class="bi bi-paperclip"></i></span>`
                : `<span style="color:var(--color-text-tertiary);">—</span>
                   <span title="Adjuntada externamente" style="margin-left:4px;font-size:.7rem;
                     color:var(--color-text-secondary);"><i class="bi bi-paperclip"></i></span>`)
            : `<span style="font-family:monospace;font-size:.82rem;">${nroFmt(f.puntoVenta, f.numero)}</span>`;

        const caeCol = f.esExterna
            ? `<span style="color:var(--color-text-tertiary);">—</span>`
            : (f.cae ? f.cae.substring(0,8) + "…" : "—");

        const pdfBtn = (f.esExterna && f.archivoNombre)
            ? `<button class="btn-accion btn-pdf btn-pdf-fact" data-id="${f.id}"
                       data-externa="true" title="Ver PDF adjunto">
                   <i class="bi bi-file-pdf-fill" style="color:#ef4444;"></i>
               </button>`
            : (!f.esExterna
                ? `<button class="btn-accion btn-pdf btn-pdf-fact" data-id="${f.id}"
                           data-externa="false" title="Descargar PDF">
                       <i class="bi bi-file-pdf"></i>
                   </button>`
                : `<span style="color:var(--color-text-tertiary);font-size:.75rem;">Sin archivo</span>`);

        return `
        <tr style="cursor:pointer;" data-id="${f.id}">
            <td>${tipoBadge(f.tipoComprobante)}</td>
            <td>${nroCol}</td>
            <td>${formatFecha(f.fechaEmision)}</td>
            <td>${esc(f.clienteNombre || "—")}</td>
            <td style="font-weight:600;">${f.total ? formatPrecio(f.total) : "—"}</td>
            <td class="cae-chip">${caeCol}</td>
            <td>${badgeEstado(f.estado)}</td>
            <td>${pdfBtn}</td>
        </tr>`;
    }).join("");

    tbody.querySelectorAll("tr[data-id]").forEach(tr => {
        tr.addEventListener("click", e => {
            if (e.target.closest(".btn-pdf-fact")) return;
            abrirDetalle(+tr.dataset.id);
        });
    });

    tbody.querySelectorAll(".btn-pdf-fact").forEach(btn => {
        btn.addEventListener("click", () => {
            const id = +btn.dataset.id;
            if (btn.dataset.externa === "true") {
                abrirArchivoExterno(id);
            } else {
                descargarPDF(id);
            }
        });
    });

    renderPaginacion(total);
}

function renderPaginacion(total) {
    const totalPags = Math.ceil(total / factPageSize) || 1;
    const desde     = total === 0 ? 0 : (factPagina - 1) * factPageSize + 1;
    const hasta     = Math.min(factPagina * factPageSize, total);

    document.getElementById("factPagInfo").textContent =
        `Mostrando ${desde}–${hasta} de ${total}`;

    const controls = document.getElementById("factPagControls");
    controls.innerHTML = "";

    const mkBtn = (label, page, active, disabled) => {
        const b = document.createElement("button");
        b.className = "pag-btn" + (active ? " pag-btn-active" : "") + (disabled ? " pag-btn-disabled" : "");
        b.textContent = label;
        b.disabled = disabled;
        if (!disabled && !active) b.addEventListener("click", () => { factPagina = page; renderTabla(); });
        return b;
    };

    controls.appendChild(mkBtn("‹", factPagina - 1, false, factPagina === 1));

    const range = [];
    for (let i = 1; i <= totalPags; i++) {
        if (i === 1 || i === totalPags || Math.abs(i - factPagina) <= 1) range.push(i);
        else if (range[range.length - 1] !== "…") range.push("…");
    }
    range.forEach(p => {
        if (p === "…") {
            const s = document.createElement("span");
            s.textContent = "…";
            s.style.cssText = "padding:0 4px;color:var(--color-text-secondary);font-size:12px;";
            controls.appendChild(s);
        } else {
            controls.appendChild(mkBtn(p, p, p === factPagina, false));
        }
    });

    controls.appendChild(mkBtn("›", factPagina + 1, false, factPagina === totalPags));
}

// Filtros
document.querySelectorAll(".fact-filter-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".fact-filter-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        filtroEstado = btn.dataset.estado;
        factPagina = 1;
        renderTabla();
    });
});

document.getElementById("factPageSize").addEventListener("change", e => {
    factPageSize = +e.target.value;
    factPagina   = 1;
    renderTabla();
});

document.getElementById("btnRefresh").addEventListener("click", cargarFacturas);

// ================================================================
// PDF / ARCHIVO
// ================================================================

async function descargarPDF(id) {
    try {
        const res = await apiFetch(`${API_BASE}/api/factura/${id}/pdf`);
        if (!res.ok) throw new Error();
        const blob = await res.blob();
        const url  = URL.createObjectURL(blob);
        const a    = document.createElement("a");
        const f    = todasFacturas.find(x => x.id === id);
        a.href     = url;
        a.download = f ? `Factura${f.tipoComprobante}-${nroFmt(f.puntoVenta, f.numero)}.pdf` : "factura.pdf";
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);
    } catch {
        toast("Error al generar el PDF", "error");
    }
}

async function abrirArchivoExterno(id) {
    try {
        const res = await apiFetch(`${API_BASE}/api/factura/${id}/archivo`);
        if (!res.ok) throw new Error();
        const blob = await res.blob();
        const url  = URL.createObjectURL(blob);
        window.open(url, "_blank");
        setTimeout(() => URL.revokeObjectURL(url), 30000);
    } catch {
        toast("Error al abrir el archivo adjunto", "error");
    }
}

// ================================================================
// MODAL DETALLE
// ================================================================

let factDetalleActual = null;

function abrirDetalle(id) {
    factDetalleActual = todasFacturas.find(f => f.id === id);
    if (!factDetalleActual) return;
    const f = factDetalleActual;

    const nroLabel = f.esExterna
        ? (f.numero && f.numero > 0 ? `N° ${nroFmt(f.puntoVenta || 0, f.numero)}` : "Adjuntada")
        : `N° ${nroFmt(f.puntoVenta, f.numero)}`;
    document.getElementById("detTitulo").textContent =
        `Factura ${f.tipoComprobante} — ${nroLabel}`;

    document.getElementById("detCliente").textContent  = f.clienteNombre || "—";
    document.getElementById("detCuit").textContent     = f.clienteCuit   || "—";
    document.getElementById("detCondIVA").textContent  = condIvaLabel(f.clienteCondicionIVA);
    document.getElementById("detFecha").textContent    = formatFecha(f.fechaEmision);
    document.getElementById("detEstado").innerHTML     = badgeEstado(f.estado);
    document.getElementById("detVtoCAE").textContent   = f.esExterna ? "—" : formatFecha(f.caeFechaVencimiento);
    document.getElementById("detCAE").textContent      = f.esExterna ? "—" : (f.cae || "SIN CAE");

    const discrimina = !f.esExterna && f.tipoComprobante === "A";
    document.getElementById("detRowSubtotal").style.display = discrimina ? "" : "none";
    document.getElementById("detRowIVA").style.display      = discrimina ? "" : "none";
    document.getElementById("detSubtotal").textContent = formatPrecio(f.subtotal);
    document.getElementById("detIva").textContent      = formatPrecio(f.totalIva);
    document.getElementById("detTotal").textContent    = f.total ? formatPrecio(f.total) : "—";

    if (f.esExterna) {
        document.getElementById("detItemsBody").innerHTML =
            `<tr><td colspan="5" style="text-align:center;color:var(--color-text-secondary);padding:16px;">
                <i class="bi bi-paperclip"></i> Factura adjuntada externamente
                ${f.archivoNombre ? `— ${esc(f.archivoNombre)}` : ""}
             </td></tr>`;
    } else {
        document.getElementById("detItemsBody").innerHTML =
            (f.items || []).map(i => `
                <tr>
                    <td>${esc(i.descripcion || "—")}</td>
                    <td style="text-align:center;">${i.cantidad ?? 1}</td>
                    <td style="text-align:right;">${formatPrecio(i.precioUnitario)}</td>
                    <td style="text-align:center;">${i.alicuotaIva ?? 0}%</td>
                    <td style="text-align:right;font-weight:600;">${formatPrecio(i.subtotal)}</td>
                </tr>`).join("") ||
            `<tr><td colspan="5" style="text-align:center;color:var(--color-text-secondary);">Sin ítems</td></tr>`;
    }

    document.getElementById("detBtnAnular").style.display = f.estado === "AUTORIZADA" ? "" : "none";
    document.getElementById("detBtnPDF").style.display    = (f.esExterna && !f.archivoNombre) ? "none" : "";

    document.getElementById("modalDetalle").classList.add("visible");
}

function cerrarDetalle() {
    document.getElementById("modalDetalle").classList.remove("visible");
}

document.getElementById("detBtnCerrar").addEventListener("click", cerrarDetalle);

document.getElementById("detBtnPDF").addEventListener("click", () => {
    if (!factDetalleActual) return;
    if (factDetalleActual.esExterna) {
        abrirArchivoExterno(factDetalleActual.id);
    } else {
        descargarPDF(factDetalleActual.id);
    }
});

document.getElementById("detBtnAnular").addEventListener("click", async () => {
    if (!factDetalleActual) return;
    const label = `Factura ${factDetalleActual.tipoComprobante} N° ${nroFmt(factDetalleActual.puntoVenta, factDetalleActual.numero)}`;
    if (!confirm(`¿Anular ${label}?\nEsta acción no es reversible.`)) return;
    try {
        const res = await apiFetch(`${API_BASE}/api/factura/${factDetalleActual.id}/anular`, { method: "PUT" });
        if (!res.ok) throw new Error();
        toast("Factura anulada");
        cerrarDetalle();
        await cargarFacturas();
    } catch {
        toast("Error al anular la factura", "error");
    }
});

// ================================================================
// AUTOCOMPLETE CLIENTE — búsqueda live con debounce
// ================================================================

function clienteLabel(c) {
    return c.tipoCliente === "PERSONA_FISICA"
        ? `${c.nombre || ""} ${c.apellido || ""}`.trim()
        : (c.razonSocial || c.email || "");
}

function clienteCuit(c) { return c.cuit || c.cuil || ""; }

function buildClienteItem(c) {
    return {
        id:       c.id,
        label:    clienteLabel(c),
        cuit:     clienteCuit(c),
        direccion:c.direccion || "",
        condIVA:  c.condicionIVA || "CONSUMIDOR_FINAL",
    };
}

function debounce(fn, ms) {
    let t;
    return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
}

function makeClienteAutocomplete({ inputEl, dropEl, onSelect, onClear }) {
    let abortCtrl = null;

    function showDrop(items) {
        if (!items.length) { dropEl.style.display = "none"; return; }
        dropEl.innerHTML = items.map((c, i) => `
            <div class="np-cliente-item" data-idx="${i}" style="
                padding:9px 14px;cursor:pointer;display:flex;align-items:center;
                gap:8px;border-bottom:1px solid var(--color-border-soft);
                transition:background .1s;">
                <div>
                    <div style="font-size:13px;font-weight:600;color:var(--text-main);">${esc(c.label)}</div>
                    ${c.cuit
                        ? `<div style="font-size:11px;color:var(--color-text-secondary);">${esc(c.cuit)}</div>`
                        : ""}
                </div>
            </div>`).join("");

        const rect = inputEl.getBoundingClientRect();
        Object.assign(dropEl.style, {
            top: (rect.bottom + 4) + "px",
            left: rect.left + "px",
            width: rect.width + "px",
            display: "block",
        });

        dropEl.querySelectorAll(".np-cliente-item").forEach(el => {
            el.addEventListener("mouseover",  () => el.style.background = "var(--verde-suave)");
            el.addEventListener("mouseleave", () => el.style.background = "");
            el.addEventListener("mousedown", e => {
                e.preventDefault();
                onSelect(items[+el.dataset.idx]);
                dropEl.style.display = "none";
            });
        });
    }

    function hideDrop() { dropEl.style.display = "none"; }

    async function buscarClientes(q) {
        if (abortCtrl) abortCtrl.abort();
        abortCtrl = new AbortController();
        try {
            const res = await apiFetch(
                `${API_BASE}/api/clientes/buscar?q=${encodeURIComponent(q)}`,
                { signal: abortCtrl.signal }
            );
            if (!res.ok) return;
            const data = await res.json();
            showDrop(data.map(buildClienteItem));
        } catch (e) {
            if (e.name !== "AbortError") hideDrop();
        }
    }

    const buscarDebounced = debounce(buscarClientes, 220);

    inputEl.addEventListener("input", () => {
        if (onClear) onClear();
        const q = inputEl.value.trim();
        if (q.length < 2) { hideDrop(); return; }
        // Spinner visual
        dropEl.innerHTML = `<div style="padding:12px 14px;font-size:12px;color:var(--color-text-secondary);">
            <i class="bi bi-arrow-repeat" style="animation:spin .6s linear infinite;display:inline-block;"></i>
            Buscando…</div>`;
        const rect = inputEl.getBoundingClientRect();
        Object.assign(dropEl.style, {
            top: (rect.bottom + 4) + "px",
            left: rect.left + "px",
            width: rect.width + "px",
            display: "block",
        });
        buscarDebounced(q);
    });

    inputEl.addEventListener("blur", () => setTimeout(hideDrop, 180));
}

// ================================================================
// MODAL NUEVA FACTURA
// ================================================================

let nfItemCount = 0;
let nfClienteId = null;

// ── Combobox cliente — Modal Nueva Factura ──
const nfClienteInput = document.getElementById("nfClienteInput");
const nfClienteDrop  = document.getElementById("nfClienteDrop");

makeClienteAutocomplete({
    inputEl:  nfClienteInput,
    dropEl:   nfClienteDrop,
    onSelect: c => {
        nfClienteId = c.id;
        nfClienteInput.value = c.label;
        nfClienteInput.style.borderColor = "";
        document.getElementById("nfCuit").value         = c.cuit      || "";
        document.getElementById("nfDireccion").value    = c.direccion || "";
        document.getElementById("nfCondicionIVA").value = c.condIVA   || "CONSUMIDOR_FINAL";
    },
    onClear: () => {
        nfClienteId = null;
        nfClienteInput.style.borderColor = "";
    },
});

// ── Abrir / cerrar modal ──
document.getElementById("btnNuevaFactura").addEventListener("click", () => {
    resetModalNF();
    document.getElementById("modalNuevaFactura").classList.add("visible");
});

function cerrarModalNF() {
    document.getElementById("modalNuevaFactura").classList.remove("visible");
}

document.getElementById("nfBtnCerrar").addEventListener("click", cerrarModalNF);
document.getElementById("nfBtnCancelarP1").addEventListener("click", cerrarModalNF);

function resetModalNF() {
    nfClienteId = null;
    nfItemCount  = 0;
    document.getElementById("nfClienteInput").value    = "";
    document.getElementById("nfCuit").value            = "";
    document.getElementById("nfDireccion").value       = "";
    document.getElementById("nfCondicionIVA").value    = "CONSUMIDOR_FINAL";
    document.getElementById("nfTipoComprobante").value = "";
    document.getElementById("nfFechaEmision").value    = new Date().toISOString().split("T")[0];
    document.getElementById("nfItemsBody").innerHTML   = "";
    document.getElementById("nfPaso1").style.display   = "";
    document.getElementById("nfPaso2").style.display   = "none";
    document.getElementById("nfTitulo").textContent    = "Nueva Factura";
    recalcularTotales();
    agregarItem();
}

// ── Paso 1 → 2 ──
document.getElementById("nfBtnSiguiente").addEventListener("click", () => {
    const tipo    = document.getElementById("nfTipoComprobante").value;
    const cliente = document.getElementById("nfClienteInput").value.trim();
    if (!tipo)    { toast("Seleccioná el tipo de comprobante", "error"); return; }
    if (!cliente) { toast("Ingresá el receptor de la factura", "error"); return; }
    document.getElementById("nfPaso1").style.display = "none";
    document.getElementById("nfPaso2").style.display = "";
    document.getElementById("nfTitulo").textContent  = `Nueva Factura ${tipo} — Ítems`;
    actualizarVisibilidadTotales();
});

document.getElementById("nfBtnAtras").addEventListener("click", () => {
    document.getElementById("nfPaso2").style.display = "none";
    document.getElementById("nfPaso1").style.display = "";
    document.getElementById("nfTitulo").textContent  = "Nueva Factura";
});

// ── Tabla de ítems ──
function agregarItem() {
    const tbody = document.getElementById("nfItemsBody");
    const tr    = document.createElement("tr");
    tr.innerHTML = `
        <td><input type="text"   class="nf-desc" placeholder="Descripción del servicio"/></td>
        <td><input type="number" class="nf-cant" value="1" min="0.01" step="0.01" style="width:60px;"/></td>
        <td><input type="number" class="nf-pu"   value="" min="0" step="0.01" placeholder="0.00"/></td>
        <td>
            <select class="nf-aliq" style="width:70px;">
                <option value="0">0%</option>
                <option value="10.5">10.5%</option>
                <option value="21" selected>21%</option>
                <option value="27">27%</option>
            </select>
        </td>
        <td class="nf-subtotal-cell" style="text-align:right;font-weight:600;">$0</td>
        <td><button type="button" class="btn-del-item" title="Eliminar"><i class="bi bi-trash3"></i></button></td>`;
    tbody.appendChild(tr);

    tr.querySelector(".nf-cant").addEventListener("input",  recalcularTotales);
    tr.querySelector(".nf-pu").addEventListener("input",    recalcularTotales);
    tr.querySelector(".nf-aliq").addEventListener("change", recalcularTotales);
    tr.querySelector(".btn-del-item").addEventListener("click", () => { tr.remove(); recalcularTotales(); });
    nfItemCount++;
}

document.getElementById("nfBtnAgregarItem").addEventListener("click", agregarItem);

function recalcularTotales() {
    let subtotal = 0, totalIva = 0;
    document.querySelectorAll("#nfItemsBody tr").forEach(tr => {
        const cant = parseFloat(tr.querySelector(".nf-cant")?.value) || 0;
        const pu   = parseFloat(tr.querySelector(".nf-pu")?.value)   || 0;
        const aliq = parseFloat(tr.querySelector(".nf-aliq")?.value) || 0;
        const sub  = cant * pu;
        totalIva += sub * aliq / 100;
        subtotal += sub;
        const cell = tr.querySelector(".nf-subtotal-cell");
        if (cell) cell.textContent = formatPrecio(sub);
    });
    document.getElementById("nfSubtotal").textContent = formatPrecio(subtotal);
    document.getElementById("nfIva").textContent      = formatPrecio(totalIva);
    document.getElementById("nfTotal").textContent    = formatPrecio(subtotal + totalIva);
}

function actualizarVisibilidadTotales() {
    const disc = document.getElementById("nfTipoComprobante").value === "A";
    document.getElementById("nfRowSubtotal").style.display = disc ? "" : "none";
    document.getElementById("nfRowIVA").style.display      = disc ? "" : "none";
}

// ── Emitir ──
document.getElementById("nfBtnEmitir").addEventListener("click", async () => {
    const items = [];
    let valido  = true;

    document.querySelectorAll("#nfItemsBody tr").forEach(tr => {
        const desc = tr.querySelector(".nf-desc")?.value.trim();
        const cant = parseFloat(tr.querySelector(".nf-cant")?.value) || 0;
        const pu   = parseFloat(tr.querySelector(".nf-pu")?.value)   || 0;
        const aliq = parseFloat(tr.querySelector(".nf-aliq")?.value) || 0;
        if (!desc || pu <= 0 || cant <= 0) { valido = false; return; }
        items.push({ descripcion: desc, cantidad: cant, precioUnitario: pu, alicuotaIva: aliq });
    });

    if (!nfClienteId) {
        toast("Seleccioná un cliente del listado", "error");
        nfClienteInput.style.borderColor = "var(--color-error, #ef4444)";
        return;
    }

    if (!valido || items.length === 0) {
        toast("Completá todos los ítems (descripción y precio > 0)", "error");
        return;
    }

    const btn = document.getElementById("nfBtnEmitir");
    btn.disabled = true;
    btn.innerHTML = `<i class="bi bi-hourglass-split"></i> Emitiendo…`;

    const payload = {
        clienteId:           nfClienteId,
        clienteNombre:       document.getElementById("nfClienteInput").value.trim(),
        clienteCuit:         document.getElementById("nfCuit").value.trim(),
        clienteDireccion:    document.getElementById("nfDireccion").value.trim(),
        clienteCondicionIVA: document.getElementById("nfCondicionIVA").value,
        tipoComprobante:     document.getElementById("nfTipoComprobante").value,
        puntoVenta:          1,
        fechaEmision:        document.getElementById("nfFechaEmision").value || null,
        items,
    };

    try {
        const res  = await apiFetch(`${API_BASE}/api/factura/emitir`, {
            method:  "POST",
            headers: { "Content-Type": "application/json" },
            body:    JSON.stringify(payload),
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || "Error del servidor");

        toast(data.autorizada
            ? `Factura autorizada — CAE: ${data.cae}`
            : "Factura rechazada por ARCA (ver detalle)", "error");
        cerrarModalNF();
        await cargarFacturas();
    } catch (e) {
        toast(e.message || "Error al emitir la factura", "error");
    } finally {
        btn.disabled = false;
        btn.innerHTML = `<i class="bi bi-send-check"></i> Emitir factura`;
    }
});

// ================================================================
// MODAL ADJUNTAR FACTURA EXTERNA
// ================================================================

let adjClienteId    = null;
let adjArchivoFile  = null;

// ── Autocomplete cliente — Modal Adjuntar ──
const adjClienteInput = document.getElementById("adjClienteInput");
const adjClienteDrop  = document.getElementById("adjClienteDrop");

makeClienteAutocomplete({
    inputEl:  adjClienteInput,
    dropEl:   adjClienteDrop,
    onSelect: c => {
        adjClienteId = c.id;
        adjClienteInput.value = c.label;
        adjClienteInput.style.borderColor = "";
    },
    onClear: () => {
        adjClienteId = null;
        adjClienteInput.style.borderColor = "";
    },
});

// ── Dropzone de archivo ──
const adjDropzone     = document.getElementById("adjDropzone");
const adjArchivoInput = document.getElementById("adjArchivo");
const adjArchivoNombreDiv = document.getElementById("adjArchivoNombre");
const adjArchivoLabel = document.getElementById("adjArchivoLabel");

adjDropzone.addEventListener("click", () => adjArchivoInput.click());

adjDropzone.addEventListener("dragover", e => {
    e.preventDefault();
    adjDropzone.style.borderColor = "var(--verde)";
    adjDropzone.style.background  = "var(--verde-suave)";
});
adjDropzone.addEventListener("dragleave", () => {
    adjDropzone.style.borderColor = "";
    adjDropzone.style.background  = "";
});
adjDropzone.addEventListener("drop", e => {
    e.preventDefault();
    adjDropzone.style.borderColor = "";
    adjDropzone.style.background  = "";
    const file = e.dataTransfer.files[0];
    if (file && file.type === "application/pdf") setArchivoAdj(file);
    else toast("Solo se aceptan archivos PDF", "error");
});

adjArchivoInput.addEventListener("change", () => {
    const file = adjArchivoInput.files[0];
    if (file) setArchivoAdj(file);
});

function setArchivoAdj(file) {
    adjArchivoFile = file;
    adjArchivoLabel.textContent = file.name;
    Object.assign(adjArchivoNombreDiv.style, { display: "flex" });
    adjDropzone.style.display = "none";
}

document.getElementById("adjArchivoQuitar").addEventListener("click", () => {
    adjArchivoFile = null;
    adjArchivoInput.value = "";
    adjArchivoNombreDiv.style.display = "none";
    adjDropzone.style.display = "";
});

// ── Abrir / cerrar ──
document.getElementById("btnAdjuntarFactura").addEventListener("click", () => {
    resetModalAdj();
    document.getElementById("modalAdjuntar").classList.add("visible");
});

function cerrarModalAdj() {
    document.getElementById("modalAdjuntar").classList.remove("visible");
}

document.getElementById("adjBtnCerrar").addEventListener("click", cerrarModalAdj);
document.getElementById("adjBtnCancelar").addEventListener("click", cerrarModalAdj);

function resetModalAdj() {
    adjClienteId   = null;
    adjArchivoFile = null;
    adjClienteInput.value = "";
    document.getElementById("adjTipo").value        = "B";
    document.getElementById("adjFecha").value       = new Date().toISOString().split("T")[0];
    document.getElementById("adjPuntoVenta").value  = "";
    document.getElementById("adjNumero").value      = "";
    document.getElementById("adjTotal").value       = "";
    adjArchivoInput.value = "";
    adjArchivoNombreDiv.style.display = "none";
    adjDropzone.style.display = "";
    adjDropzone.style.borderColor = "";
    adjDropzone.style.background  = "";
}

// ── Guardar ──
document.getElementById("adjBtnGuardar").addEventListener("click", async () => {
    if (!adjClienteId) {
        toast("Seleccioná un cliente del listado", "error");
        adjClienteInput.style.borderColor = "var(--color-error, #ef4444)";
        return;
    }
    const clienteNombre = adjClienteInput.value.trim();

    const btn = document.getElementById("adjBtnGuardar");
    btn.disabled = true;
    btn.innerHTML = `<i class="bi bi-hourglass-split"></i> Guardando…`;

    try {
        const fd = new FormData();
        if (adjClienteId)   fd.append("clienteId",    adjClienteId);
        fd.append("clienteNombre", clienteNombre);
        fd.append("tipo",          document.getElementById("adjTipo").value);
        fd.append("fechaEmision",  document.getElementById("adjFecha").value);

        const pv  = document.getElementById("adjPuntoVenta").value;
        const num = document.getElementById("adjNumero").value;
        const tot = document.getElementById("adjTotal").value;
        if (pv)  fd.append("puntoVenta", pv);
        if (num) fd.append("numero",     num);
        if (tot) fd.append("total",      tot);
        if (adjArchivoFile) fd.append("archivo", adjArchivoFile);

        const res = await apiFetch(`${API_BASE}/api/factura/adjuntar`, {
            method: "POST",
            body:   fd,
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || "Error del servidor");
        }
        toast("Factura adjuntada correctamente");
        cerrarModalAdj();
        await cargarFacturas();
    } catch (e) {
        toast(e.message || "Error al guardar", "error");
    } finally {
        btn.disabled = false;
        btn.innerHTML = `<i class="bi bi-paperclip"></i> Guardar`;
    }
});

// ================================================================
// INIT
// ================================================================
cargarFacturas();
