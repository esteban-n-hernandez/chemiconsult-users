const API_CLIENTES_BASE = `${API_BASE}/api`;

// ── Guard ──
const rol = (localStorage.getItem("userRole") || "").toUpperCase();
if (!localStorage.getItem("token") || rol !== "ROLE_CLIENTE") {
    window.location.href = "login.html";
}

// ── Decodificar JWT para obtener userId ──
function getPayload() {
    try {
        const token = localStorage.getItem("token");
        return JSON.parse(atob(token.split(".")[1]));
    } catch { return {}; }
}
const payload = getPayload();
const userId  = payload.userID;

// ── Datos del usuario ──
const userEmail = localStorage.getItem("userEmail");
const userName  = localStorage.getItem("userName");
const nombre    = userName || userEmail || "Cliente";
const iniciales = nombre.split(" ").map(p => p[0]).join("").substring(0, 2).toUpperCase();

document.getElementById("header-nombre").textContent = nombre;
document.getElementById("header-avatar").textContent = iniciales;
document.getElementById("cliente-nombre").textContent = nombre;

// ── Fecha ──
const hoy = new Date().toLocaleDateString("es-AR", {
    weekday: "long", year: "numeric", month: "long", day: "numeric"
});
document.getElementById("fecha-hoy").textContent = hoy.charAt(0).toUpperCase() + hoy.slice(1);

// ──────────────────────────────────────────
// FETCH ESTUDIOS DESDE EL BACKEND
// ──────────────────────────────────────────
const token = localStorage.getItem("token");

let todasLasMuestras = [];
let datosFiltrados   = [];
let paginaActual     = 1;
const POR_PAGINA     = 10;

async function cargarEstudios() {
    try {
        const res = await fetch(`${API_CLIENTES_BASE}/estudios/user/${userId}`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Error al obtener estudios");

        const data = await res.json();

        // Mapear respuesta al formato interno (se muestran todos los estados)
        todasLasMuestras = data.map(e => ({
            id:       e.id,
            codigo:   e.numeroProtocolo || `ID-${e.id}`,
            tipo:     e.tipo || "—",
            fecha:    e.createdDate
                ? new Date(e.createdDate).toLocaleDateString("es-AR", { day:"2-digit", month:"short", year:"numeric" })
                : "—",
            estado:   e.estado || "—",
            informe:  e.estado === "COMPLETO"  // solo COMPLETO tiene PDF disponible
        }));

        // KPIs
        document.getElementById("kpi-total").textContent   = todasLasMuestras.length;
        document.getElementById("kpi-proceso").textContent = todasLasMuestras.filter(m => m.estado === "EN_PROCESO" || m.estado === "DEMORADA").length;
        document.getElementById("kpi-listos").textContent  = todasLasMuestras.filter(m => m.informe).length;

        datosFiltrados = [...todasLasMuestras];
        renderTabla();

    } catch (err) {
        console.error(err);
        document.getElementById("tablaBody").innerHTML = "";
        document.getElementById("errorState").style.display = "block";
    }
}

// ──────────────────────────────────────────
// RENDER TABLA
// ──────────────────────────────────────────
function badgeHTML(estado) {
    const map = {
        "PENDIENTE":            `<span class="badge-estado badge-pendiente">⏳ Pendiente</span>`,
        "EN_PROCESO":           `<span class="badge-estado badge-proceso">🔬 En análisis</span>`,
        "DEMORADA":             `<span class="badge-estado badge-demorada">⏸ Demorada</span>`,
        "COMPLETO_SIN_INFORME": `<span class="badge-estado badge-sin-informe">✔ Analizado</span>`,
        "COMPLETO":             `<span class="badge-estado badge-informe">📄 Informe listo</span>`,
    };
    return map[estado] || `<span class="badge-estado">${estado}</span>`;
}

function btnInformeHTML(muestra) {
    if (muestra.informe) {
        return `<button class="btn-descargar" onclick="abrirInformes(${muestra.id}, '${muestra.codigo}')">
                    <i class="bi bi-eye"></i> Ver informe
                </button>`;
    }
    return `<button class="btn-descargar disabled" disabled>
                <i class="bi bi-clock"></i> En proceso
            </button>`;
}

function renderTabla() {
    const tbody   = document.getElementById("tablaBody");
    const sinRes  = document.getElementById("sinResultados");
    const pagInfo = document.getElementById("pag-info");
    const pagBtns = document.getElementById("paginacion-btns");

    const total     = datosFiltrados.length;
    const totalPags = Math.ceil(total / POR_PAGINA);
    const inicio    = (paginaActual - 1) * POR_PAGINA;
    const fin       = Math.min(inicio + POR_PAGINA, total);
    const slice     = datosFiltrados.slice(inicio, fin);

    if (total === 0) {
        tbody.innerHTML = "";
        sinRes.style.display = "block";
        pagInfo.textContent  = "Sin resultados";
        pagBtns.innerHTML    = "";
        return;
    }

    sinRes.style.display = "none";

    tbody.innerHTML = slice.map(m => `
        <tr>
            <td><span class="cod-badge">${m.codigo}</span></td>
            <td>${m.tipo}</td>
            <td>${m.fecha}</td>
            <td>${badgeHTML(m.estado)}</td>
            <td>${btnInformeHTML(m)}</td>
        </tr>
    `).join("");

    pagInfo.textContent = `Mostrando ${inicio + 1}–${fin} de ${total} registro${total !== 1 ? "s" : ""}`;

    // Paginación
    pagBtns.innerHTML = "";

    const btnPrev = document.createElement("button");
    btnPrev.className = "pag-btn";
    btnPrev.innerHTML = `<i class="bi bi-chevron-left"></i>`;
    btnPrev.disabled  = paginaActual === 1;
    btnPrev.onclick   = () => { paginaActual--; renderTabla(); };
    pagBtns.appendChild(btnPrev);

    for (let i = 1; i <= totalPags; i++) {
        const btn = document.createElement("button");
        btn.className = "pag-btn" + (i === paginaActual ? " active" : "");
        btn.textContent = i;
        btn.onclick = () => { paginaActual = i; renderTabla(); };
        pagBtns.appendChild(btn);
    }

    const btnNext = document.createElement("button");
    btnNext.className = "pag-btn";
    btnNext.innerHTML = `<i class="bi bi-chevron-right"></i>`;
    btnNext.disabled  = paginaActual === totalPags;
    btnNext.onclick   = () => { paginaActual++; renderTabla(); };
    pagBtns.appendChild(btnNext);
}

// ──────────────────────────────────────────
// MODAL PDF — soporte multi-archivo
// ──────────────────────────────────────────
const pdfIframe  = document.getElementById("pdfIframe");
const pdfLoading = document.getElementById("pdfLoading");

async function abrirInformes(estudioId, protocolo) {
    document.getElementById("pdfModalTitulo").textContent = `Informe ${protocolo}`;

    // Reset estado visual
    pdfLoading.style.display = "flex";
    pdfLoading.innerHTML     = `<div class="spinner"></div><span>Cargando...</span>`;
    pdfIframe.style.display  = "none";
    pdfIframe.src            = "";
    document.getElementById("pdfArchivosNav").style.display = "none";
    document.getElementById("pdfArchivosBtns").innerHTML = "";

    bootstrap.Modal.getOrCreateInstance(document.getElementById("pdfModal")).show();

    try {
        const res = await fetch(`${API_CLIENTES_BASE}/estudios/${estudioId}/archivos`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error();
        const archivos = await res.json();

        if (archivos.length === 0) {
            pdfLoading.innerHTML = `<i class="bi bi-exclamation-circle" style="font-size:32px;color:#ef4444;"></i>
                                    <span style="color:#ef4444;">No hay archivos disponibles.</span>`;
            return;
        }

        if (archivos.length > 1) {
            const nav  = document.getElementById("pdfArchivosNav");
            const btns = document.getElementById("pdfArchivosBtns");
            nav.style.display = "flex";

            archivos.forEach((a, i) => {
                const btn = document.createElement("button");
                btn.className = "btn-archivo-nav" + (i === 0 ? " activo" : "");
                btn.textContent = a.nombre || `Archivo ${i + 1}`;
                btn.dataset.archivoId = a.id;
                btn.onclick = () => {
                    document.querySelectorAll(".btn-archivo-nav").forEach(b => b.classList.remove("activo"));
                    btn.classList.add("activo");
                    cargarArchivoEnModal(estudioId, a.id, a.nombre || `Archivo ${i + 1}`, protocolo);
                };
                btns.appendChild(btn);
            });
        }

        // Cargar el primer archivo automáticamente
        const primero = archivos[0];
        cargarArchivoEnModal(estudioId, primero.id, primero.nombre || "Archivo", protocolo);

    } catch {
        pdfLoading.innerHTML = `<i class="bi bi-exclamation-circle" style="font-size:32px;color:#ef4444;"></i>
                                <span style="color:#ef4444;">No se pudo cargar el informe.</span>`;
    }
}

function cargarArchivoEnModal(estudioId, archivoId, nombre, protocolo) {
    const url = `${API_CLIENTES_BASE}/estudios/${estudioId}/archivos/${archivoId}`;

    pdfLoading.style.display = "flex";
    pdfLoading.innerHTML     = `<div class="spinner"></div><span>Cargando...</span>`;
    pdfIframe.style.display  = "none";
    pdfIframe.src            = "";

    document.getElementById("btnDescargarPdf").href     = "#";
    document.getElementById("btnDescargarPdf").download = nombre;
    document.getElementById("btnAbrirNueva").href       = "#";

    fetch(url, { headers: { "Authorization": `Bearer ${token}` } })
        .then(res => { if (!res.ok) throw new Error(); return res.blob(); })
        .then(blob => {
            const blobUrl = URL.createObjectURL(blob);
            pdfIframe.src = blobUrl;
            pdfIframe.style.display  = "block";
            pdfLoading.style.display = "none";
            document.getElementById("btnDescargarPdf").href = blobUrl;
            document.getElementById("btnAbrirNueva").href   = blobUrl;
        })
        .catch(() => {
            pdfLoading.innerHTML = `<i class="bi bi-exclamation-circle" style="font-size:32px;color:#ef4444;"></i>
                                    <span style="color:#ef4444;">No se pudo cargar el archivo.</span>`;
        });
}

document.getElementById("pdfModal").addEventListener("hidden.bs.modal", () => {
    pdfIframe.src = "";
    document.getElementById("pdfArchivosNav").style.display = "none";
});

// ── Buscador ──
document.getElementById("buscadorProtocolo").addEventListener("input", function () {
    const texto = this.value.trim().toLowerCase();
    datosFiltrados = todasLasMuestras.filter(m => m.codigo.toLowerCase().includes(texto));
    paginaActual = 1;
    renderTabla();
});

// ── Init ──
cargarEstudios();