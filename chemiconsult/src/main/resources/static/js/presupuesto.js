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

// ── Fecha default ──
document.getElementById("inputFecha").value = new Date().toISOString().slice(0, 10);

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
function toast(msg, type = "success") {
    const el  = document.getElementById("toastPres");
    const ico = el.querySelector("i");
    document.getElementById("toastPresMsg").textContent = msg;
    ico.className = type === "error"
        ? "bi bi-x-circle-fill danger"
        : "bi bi-check-circle-fill success";
    el.classList.add("visible");
    setTimeout(() => el.classList.remove("visible"), 3500);
}

// ── Formato de precio ARS ──
function formatPrecio(val) {
    if (!val && val !== 0) return "";
    return "$" + Number(val).toLocaleString("es-AR", { maximumFractionDigits: 0 });
}

function parsePrecio(str) {
    return parseFloat(String(str).replace(/\./g, "").replace(",", ".")) || 0;
}

// ── Clientes ──
async function cargarClientes() {
    const sel = document.getElementById("inputCliente");
    try {
        const res = await apiFetch(`${API_BASE}/api/clientes`);
        if (!res.ok) throw new Error();
        const clientes = await res.json();
        sel.innerHTML = '<option value="">— Seleccioná un cliente —</option>';
        clientes.forEach(c => {
            const opt = document.createElement("option");
            opt.value = JSON.stringify({
                razonSocial: c.razonSocial || null,
                nombre:      c.nombre      || null,
                apellido:    c.apellido    || null,
            });
            opt.textContent = c.tipoCliente === "PERSONA_FISICA"
                ? `${c.nombre || ""} ${c.apellido || ""}`.trim()
                : (c.razonSocial || c.nombre || c.email);
            sel.appendChild(opt);
        });
    } catch {
        sel.innerHTML = '<option value="">Error al cargar clientes</option>';
    }
}

// ── Ítems ──
let itemCount = 0;

function agregarItem(data = {}) {
    const id = ++itemCount;
    document.getElementById("presEmpty").style.display = "none";

    const tr = document.createElement("tr");
    tr.dataset.id = id;
    tr.innerHTML = `
        <td><input class="pres-cell-input" data-field="matriz"
                   value="${esc(data.matriz || "")}" placeholder="Ej: Efluente"/></td>
        <td><textarea class="pres-cell-textarea" data-field="determinacion"
                      rows="2" placeholder="Lista de parámetros...">${esc(data.determinacion || "")}</textarea></td>
        <td><input class="pres-cell-input pres-cell-money" data-field="precioUnitario"
                   type="text" value="${data.precioUnitario || ""}" placeholder="0"/></td>
        <td><input class="pres-cell-input pres-cell-cant" data-field="cantidadMuestras"
                   type="number" min="1" value="${data.cantidadMuestras || 1}"/></td>
        <td><input class="pres-cell-input pres-cell-money pres-cell-total" data-field="total"
                   type="text" value="${data.total || ""}" placeholder="0" readonly/></td>
        <td><button class="btn-del-item" data-id="${id}" title="Eliminar ítem">
                <i class="bi bi-trash"></i>
            </button></td>`;

    const precioInput = tr.querySelector('[data-field="precioUnitario"]');
    const cantInput   = tr.querySelector('[data-field="cantidadMuestras"]');
    const totalInput  = tr.querySelector('[data-field="total"]');

    function recalcular() {
        const precio   = parsePrecio(precioInput.value);
        const cantidad = parseInt(cantInput.value) || 0;
        const total    = precio * cantidad;
        totalInput.value = total ? formatPrecio(total) : "";
        actualizarTotalGeneral();
    }

    precioInput.addEventListener("input", recalcular);
    cantInput.addEventListener("input",   recalcular);

    if (data.precioUnitario && data.cantidadMuestras) recalcular();

    document.getElementById("itemsBody").appendChild(tr);
    actualizarTotalGeneral();
}

function actualizarTotalGeneral() {
    let total = 0;
    document.querySelectorAll('[data-field="total"]').forEach(inp => {
        total += parsePrecio(inp.value);
    });
    document.getElementById("totalGeneral").textContent = formatPrecio(total);
}

document.getElementById("itemsBody").addEventListener("click", e => {
    const btn = e.target.closest(".btn-del-item");
    if (!btn) return;
    btn.closest("tr").remove();
    actualizarTotalGeneral();
    if (!document.getElementById("itemsBody").querySelector("tr")) {
        document.getElementById("presEmpty").style.display = "flex";
    }
});

document.getElementById("btnAgregarItem").addEventListener("click", () => agregarItem());

// ── Limpiar ──
document.getElementById("btnLimpiar").addEventListener("click", () => {
    document.getElementById("inputNumero").value    = "";
    document.getElementById("inputFecha").value     = new Date().toISOString().slice(0, 10);
    document.getElementById("inputCliente").value   = "";
    document.getElementById("inputSolicitado").value= "";
    document.getElementById("itemsBody").innerHTML  = "";
    document.getElementById("presEmpty").style.display = "flex";
    document.getElementById("totalGeneral").textContent = "$0";
    itemCount = 0;
});

// ── Generar PDF ──
document.getElementById("btnGenerar").addEventListener("click", async () => {
    const clienteVal = document.getElementById("inputCliente").value;
    if (!clienteVal) { toast("Seleccioná un cliente.", "error"); return; }

    const filas = document.querySelectorAll("#itemsBody tr");
    if (filas.length === 0) { toast("Agregá al menos un ítem.", "error"); return; }

    const cliente = JSON.parse(clienteVal);

    const items = Array.from(filas).map(tr => ({
        matriz:           tr.querySelector('[data-field="matriz"]').value.trim()            || null,
        determinacion:    tr.querySelector('[data-field="determinacion"]').value.trim()     || null,
        precioUnitario:   parsePrecio(tr.querySelector('[data-field="precioUnitario"]').value) || 0,
        cantidadMuestras: parseInt(tr.querySelector('[data-field="cantidadMuestras"]').value) || 1,
        total:            parsePrecio(tr.querySelector('[data-field="total"]').value) || 0,
    }));

    const payload = {
        numeroPresupuesto: parseInt(document.getElementById("inputNumero").value) || null,
        cliente,
        solicitadoPor: document.getElementById("inputSolicitado").value.trim() || null,
        fecha:         document.getElementById("inputFecha").value || null,
        items,
    };

    const btn = document.getElementById("btnGenerar");
    btn.disabled = true;
    btn.innerHTML = '<i class="bi bi-hourglass-split"></i> Generando...';

    try {
        const res = await apiFetch(`${API_BASE}/api/presupuesto/generar`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const blob = await res.blob();
        const url  = URL.createObjectURL(blob);
        const a    = document.createElement("a");
        const nro  = payload.numeroPresupuesto
            ? String(payload.numeroPresupuesto).padStart(7, "0")
            : "nuevo";
        a.href     = url;
        a.download = `presupuesto-${nro}.pdf`;
        a.click();
        setTimeout(() => URL.revokeObjectURL(url), 60000);
        toast("PDF generado correctamente.");
    } catch (err) {
        console.error(err);
        toast("Error al generar el PDF.", "error");
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-file-earmark-pdf"></i> Generar PDF';
    }
});

function esc(str) {
    return String(str || "").replace(/[&<>"']/g, c =>
        ({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":`&#39;` }[c]));
}

// ── Init ──
cargarClientes();
agregarItem(); // arranca con un ítem vacío
