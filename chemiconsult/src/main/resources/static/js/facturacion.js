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

async function cargarFacturas() {
    try {
        const res = await apiFetch(`${API_BASE}/api/factura`);
        if (!res.ok) throw new Error();
        todasFacturas = await res.json();
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
        return;
    }

    tbody.innerHTML = lista.map(f => `
        <tr style="cursor:pointer;" data-id="${f.id}">
            <td>${tipoBadge(f.tipoComprobante)}</td>
            <td style="font-size:.82rem;font-family:monospace;">${nroFmt(f.puntoVenta, f.numero)}</td>
            <td>${formatFecha(f.fechaEmision)}</td>
            <td>${esc(f.clienteNombre || "—")}</td>
            <td style="font-weight:600;">${formatPrecio(f.total)}</td>
            <td class="cae-chip">${f.cae ? f.cae.substring(0,8) + "…" : "—"}</td>
            <td>${badgeEstado(f.estado)}</td>
            <td>
                <button class="btn-pres-secondary btn-pdf-fact" data-id="${f.id}" title="PDF" style="padding:4px 8px;">
                    <i class="bi bi-file-pdf"></i>
                </button>
            </td>
        </tr>`).join("");

    tbody.querySelectorAll("tr[data-id]").forEach(tr => {
        tr.addEventListener("click", e => {
            if (e.target.closest(".btn-pdf-fact")) return;
            abrirDetalle(+tr.dataset.id);
        });
    });

    tbody.querySelectorAll(".btn-pdf-fact").forEach(btn => {
        btn.addEventListener("click", () => descargarPDF(+btn.dataset.id));
    });
}

// Filtros
document.querySelectorAll(".fact-filter-btn").forEach(btn => {
    btn.addEventListener("click", () => {
        document.querySelectorAll(".fact-filter-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        filtroEstado = btn.dataset.estado;
        renderTabla();
    });
});

document.getElementById("btnRefresh").addEventListener("click", cargarFacturas);

// ================================================================
// DESCARGAR PDF
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

// ================================================================
// MODAL DETALLE
// ================================================================

let factDetalleActual = null;

function abrirDetalle(id) {
    factDetalleActual = todasFacturas.find(f => f.id === id);
    if (!factDetalleActual) return;
    const f = factDetalleActual;

    document.getElementById("detTitulo").textContent =
        `Factura ${f.tipoComprobante} N° ${nroFmt(f.puntoVenta, f.numero)}`;
    document.getElementById("detCliente").textContent  = f.clienteNombre || "—";
    document.getElementById("detCuit").textContent     = f.clienteCuit   || "—";
    document.getElementById("detCondIVA").textContent  = condIvaLabel(f.clienteCondicionIVA);
    document.getElementById("detFecha").textContent    = formatFecha(f.fechaEmision);
    document.getElementById("detEstado").innerHTML     = badgeEstado(f.estado);
    document.getElementById("detVtoCAE").textContent   = formatFecha(f.caeFechaVencimiento);
    document.getElementById("detCAE").textContent      = f.cae || "SIN CAE";

    const discrimina = f.tipoComprobante === "A";
    document.getElementById("detRowSubtotal").style.display = discrimina ? "" : "none";
    document.getElementById("detRowIVA").style.display      = discrimina ? "" : "none";
    document.getElementById("detSubtotal").textContent = formatPrecio(f.subtotal);
    document.getElementById("detIva").textContent      = formatPrecio(f.totalIva);
    document.getElementById("detTotal").textContent    = formatPrecio(f.total);

    document.getElementById("detItemsBody").innerHTML =
        (f.items || []).map(i => `
            <tr>
                <td>${esc(i.descripcion || "—")}</td>
                <td style="text-align:center;">${i.cantidad ?? 1}</td>
                <td style="text-align:right;">${formatPrecio(i.precioUnitario)}</td>
                <td style="text-align:center;">${i.alicuotaIva ?? 0}%</td>
                <td style="text-align:right;font-weight:600;">${formatPrecio(i.subtotal)}</td>
            </tr>`).join("") ||
        `<tr><td colspan="5" style="text-align:center;color:var(--text-secondary);">Sin ítems</td></tr>`;

    document.getElementById("detBtnAnular").style.display = f.estado === "AUTORIZADA" ? "" : "none";

    document.getElementById("modalDetalle").classList.add("visible");
}

function cerrarDetalle() {
    document.getElementById("modalDetalle").classList.remove("visible");
}

document.getElementById("detBtnCerrar").addEventListener("click", cerrarDetalle);

document.getElementById("detBtnPDF").addEventListener("click", () => {
    if (factDetalleActual) descargarPDF(factDetalleActual.id);
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
// MODAL NUEVA FACTURA
// ================================================================

let nfItemCount      = 0;
let nfClienteId      = null;
let clientesLista    = [];
let clientesCargados = false;

// ── Cargar lista de clientes para autocomplete ──
async function cargarClientes() {
    if (clientesCargados) return;
    try {
        const res = await apiFetch(`${API_BASE}/api/clientes`);
        if (!res.ok) return;
        clientesLista = (await res.json()).map(c => ({
            id:       c.id,
            label:    c.tipoCliente === "PERSONA_FISICA"
                          ? `${c.nombre || ""} ${c.apellido || ""}`.trim()
                          : (c.razonSocial || c.email || ""),
            cuit:     c.cuit || c.cuil || "",
            direccion:c.direccion || "",
            condIVA:  c.condicionIVA || "CONSUMIDOR_FINAL",
        })).filter(c => c.label);
        clientesCargados = true;
    } catch { /* silencioso */ }
}

// ── Combobox cliente (position:fixed, igual que otras pantallas) ──
const nfClienteInput = document.getElementById("nfClienteInput");
const nfClienteDrop  = document.getElementById("nfClienteDrop");

function mostrarDropCliente(lista) {
    if (!lista.length) { nfClienteDrop.style.display = "none"; return; }
    nfClienteDrop.innerHTML = lista.slice(0, 8).map(c =>
        `<div class="np-cliente-item" style="padding:8px 12px;cursor:pointer;">
             <span style="font-weight:500;">${esc(c.label)}</span>
             ${c.cuit ? `<span style="font-size:.75rem;color:var(--text-secondary);margin-left:6px;">${c.cuit}</span>` : ""}
         </div>`).join("");
    const rect = nfClienteInput.getBoundingClientRect();
    Object.assign(nfClienteDrop.style, {
        top: (rect.bottom + 4) + "px",
        left: rect.left + "px",
        width: rect.width + "px",
        display: "block",
    });
    nfClienteDrop.querySelectorAll(".np-cliente-item").forEach((el, i) => {
        el.addEventListener("mousedown", e => { e.preventDefault(); seleccionarCliente(lista[i]); });
    });
}

function seleccionarCliente(c) {
    nfClienteId = c.id;
    nfClienteInput.value = c.label;
    document.getElementById("nfCuit").value         = c.cuit      || "";
    document.getElementById("nfDireccion").value    = c.direccion || "";
    document.getElementById("nfCondicionIVA").value = c.condIVA   || "CONSUMIDOR_FINAL";
    nfClienteDrop.style.display = "none";
}

nfClienteInput.addEventListener("input", () => {
    nfClienteId = null;
    const q = nfClienteInput.value.toLowerCase().trim();
    if (!q) { nfClienteDrop.style.display = "none"; return; }
    mostrarDropCliente(clientesLista.filter(c => c.label.toLowerCase().includes(q)));
});

nfClienteInput.addEventListener("focus", () => {
    const q = nfClienteInput.value.trim().toLowerCase();
    if (q) mostrarDropCliente(clientesLista.filter(c => c.label.toLowerCase().includes(q)));
});

nfClienteInput.addEventListener("blur", () => {
    setTimeout(() => nfClienteDrop.style.display = "none", 150);
});

// ── Abrir / cerrar modal ──
document.getElementById("btnNuevaFactura").addEventListener("click", () => {
    resetModalNF();
    cargarClientes();
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
// INIT
// ================================================================
cargarFacturas();
