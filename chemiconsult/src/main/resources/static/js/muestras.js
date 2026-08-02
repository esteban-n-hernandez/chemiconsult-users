// js/muestras.js
"use strict";

const API_URL = `${API_BASE}/api`;

// FIX: endpoint real ya disponible con datos cargados (matriz Líquida) — mock desactivado.
// Volver a true solo si necesitás developear sin backend levantado.
const USAR_MOCK_NORMATIVAS = false;

// FIX: endpoint real ya disponible — mock desactivado.
// Volver a true solo si necesitás developear sin backend levantado.
const USAR_MOCK_DETALLE = false;

// Cache de parámetros para el buscador individual (evita llamadas repetidas a la API)
let todosLosParametrosCache = [];

// Estado de selección de destinos (Set de IDs de ResolucionDestino tildados)
let destinosSeleccionados = new Set();

// Cache de parámetros por destino (para poder recalcular la lista al tildar/destildar)
// Map<destinoId, ParametroNormaTO[]>
let parametrosPorDestinoCache = new Map();

// Cache de etiquetas de destino para mostrar en los chips de cada parámetro
// Map<destinoId, string>
let destinosNombresCache = new Map();

// Instancias de Tom Select para autocomplete
let tomSelectCliente = null;
let tomSelectMatriz  = null;

// ============================================================
// MOCK: árbol Matriz → Resoluciones → Destinos → Parámetros
// Simula la respuesta real de GET /api/resoluciones/por-matriz/{matrizId}
// ============================================================
function mockArbolPorMatriz(matrizId) {
    // Simula matriz "Líquida" (id=1) con Res 336/06, Res 283/19, CAA, Ley 19587
    if (String(matrizId) === "1") {
        return {
            matrizId: 1,
            matrizNombre: "Líquida",
            resoluciones: [
                {
                    id: 1,
                    nombre: "Res 336/06",
                    tieneDestino: true,
                    destinos: [
                        {
                            id: 1, nombre: "Colectora cloacal",
                            parametros: [
                                { id: 101, nombre: "Aluminio", unidad: "mg/l", tipoLimite: "MAX", valorMaximo: 5.0, metodologia: { nombre: "St. Methods 3113 B" } },
                                { id: 102, nombre: "Arsénico", unidad: "mg/l", tipoLimite: "MAX", valorMaximo: 0.5, metodologia: { nombre: "St. Methods 3113 B" } },
                                { id: 103, nombre: "pH", unidad: "UpH", tipoLimite: "RANGO", valorMinimo: 7.0, valorMaximo: 10, metodologia: { nombre: "St. Methods 4500-H+ B" } }
                            ]
                        },
                        {
                            id: 2, nombre: "Cond. pluvial o cuerpo de agua superficial",
                            parametros: [
                                { id: 101, nombre: "Aluminio", unidad: "mg/l", tipoLimite: "MAX", valorMaximo: 2.0, metodologia: { nombre: "St. Methods 3113 B" } },
                                { id: 104, nombre: "Cadmio", unidad: "mg/l", tipoLimite: "MAX", valorMaximo: 0.1, metodologia: { nombre: "St. Methods 3111 B" } }
                            ]
                        },
                        {
                            id: 3, nombre: "Absorción por el suelo",
                            parametros: [
                                { id: 104, nombre: "Cadmio", unidad: "mg/l", tipoLimite: "TEXTO", limiteTexto: "Ausente", metodologia: { nombre: "St. Methods 3111 B" } }
                            ]
                        }
                    ]
                },
                {
                    id: 2,
                    nombre: "Res 283/19",
                    tieneDestino: true,
                    destinos: [
                        {
                            id: 4, nombre: "Colectora cloacal",
                            parametros: [
                                { id: 102, nombre: "Arsénico", unidad: "mg/l", tipoLimite: "MAX", valorMaximo: 0.5, metodologia: { nombre: "St. Methods 3113 B" } }
                            ]
                        },
                        {
                            id: 5, nombre: "Cond. pluvial o cuerpo de agua superficial",
                            parametros: [
                                { id: 102, nombre: "Arsénico", unidad: "mg/l", tipoLimite: "MAX", valorMaximo: 0.5, metodologia: { nombre: "St. Methods 3113 B" } }
                            ]
                        },
                        {
                            id: 6, nombre: "Absorción por el suelo",
                            parametros: [
                                { id: 102, nombre: "Arsénico", unidad: "mg/l", tipoLimite: "MAX", valorMaximo: 0.1, metodologia: { nombre: "St. Methods 3113 B" } }
                            ]
                        }
                    ]
                },
                {
                    id: 3,
                    nombre: "CAA",
                    tieneDestino: false,
                    destinos: [
                        {
                            id: 7, nombre: "Único",
                            parametros: [
                                { id: 105, nombre: "Boro", unidad: "mg/l", tipoLimite: "MAX", valorMaximo: 2.40, metodologia: { nombre: "St. Methods 4500 B B" } }
                            ]
                        }
                    ]
                },
                {
                    id: 4,
                    nombre: "Ley 19587 - Decreto 351/79",
                    tieneDestino: false,
                    destinos: [
                        {
                            id: 8, nombre: "Único",
                            parametros: [
                                { id: 103, nombre: "pH", unidad: "UpH", tipoLimite: "RANGO", valorMinimo: 6.5, valorMaximo: 8.5, metodologia: { nombre: "St. Methods 4500-H+ B" } }
                            ]
                        }
                    ]
                }
            ]
        };
    }
    // Otras matrices: sin normativas cargadas todavía en el mock
    return { matrizId: Number(matrizId), matrizNombre: "—", resoluciones: [] };
}

// ============================================================
// MOCK: detalle de una muestra (AnalisisDetalleTO)
// Simula GET /api/estudios/{id}/detalle — incluye un parámetro con
// límites en paralelo de dos destinos distintos (caso real que nos contaron).
// ============================================================
function mockDetalleMuestra(id) {
    return {
        id: Number(id),
        nroProtocolo: "CHQ-2026-014",
        estado: "EN_PROCESO",
        cliente: "Industrias del Sur S.A.",
        userId: 12,
        puntoMuestreo: "Salida planta - pileta norte",
        fechaIngreso: "2026-07-10",
        fechaEntrega: "2026-07-17",
        observaciones: "Cliente solicita evaluar ambos destinos posibles hasta confirmar vuelco.",
        tipoMuestraNombre: "Efluente industrial",
        matrizNombre: "Líquida",
        resolucionesAplicadas: [
            "Res 336/06 - Colectora cloacal",
            "Res 336/06 - Cond. pluvial o cuerpo de agua superficial"
        ],
        parametros: [
            {
                id: 102,
                nombre: "Arsénico",
                unidad: "mg/l",
                metodologiaNombre: "St. Methods 3113 B",
                valorResultado: "0,32",
                observacion: null,
                limites: [
                    {
                        origenNombre: "Res 336/06 - Colectora cloacal",
                        tipoLimite: "MAX",
                        limiteMin: null,
                        limiteMax: 0.5,
                        limiteTexto: null,
                        cumple: true
                    },
                    {
                        origenNombre: "Res 336/06 - Cond. pluvial o cuerpo de agua superficial",
                        tipoLimite: "MAX",
                        limiteMin: null,
                        limiteMax: 0.5,
                        limiteTexto: null,
                        cumple: true
                    }
                ]
            },
            {
                id: 103,
                nombre: "pH",
                unidad: "UpH",
                metodologiaNombre: "St. Methods 4500-H+ B",
                valorResultado: "8,1",
                observacion: null,
                limites: [
                    {
                        origenNombre: "Res 336/06 - Colectora cloacal",
                        tipoLimite: "RANGO",
                        limiteMin: 7.0,
                        limiteMax: 10.0,
                        limiteTexto: null,
                        cumple: true
                    },
                    {
                        origenNombre: "Res 336/06 - Cond. pluvial o cuerpo de agua superficial",
                        tipoLimite: "RANGO",
                        limiteMin: 6.5,
                        limiteMax: 10.0,
                        limiteTexto: null,
                        cumple: true
                    }
                ]
            },
            {
                id: 101,
                nombre: "Aluminio",
                unidad: "mg/l",
                metodologiaNombre: "St. Methods 3113 B",
                valorResultado: null,
                observacion: "Pendiente de ensayo",
                limites: [
                    {
                        origenNombre: "Res 336/06 - Colectora cloacal",
                        tipoLimite: "MAX",
                        limiteMin: null,
                        limiteMax: 5.0,
                        limiteTexto: null,
                        cumple: null
                    }
                ]
            }
        ]
    };
}

// Estado de la tabla: filtro activo y página actual
let estadoActivo = "todos";
let paginaActual = 0;
const ITEMS_POR_PAGINA = 20;

// Snapshot de todas las muestras cargadas (para filtrar/buscar en cliente)
let todasLasMuestras = [];

// Estado del ordenamiento de la tabla
let sortCol = "nroProtocolo";
let sortDir = "desc";

// ID del análisis abierto actualmente en el modal de detalle
let detalleAnalisisId = null;
let _autoGuardarTimer = null;

// ID de la muestra que se está editando (null = modo alta)
let editandoMuestraId = null;

// Paso actual del wizard (1 = Datos, 2 = Normativas, 3 = Parámetros)
let currentStep = 1;


// ============================================================
// 1b. WIZARD — navegación entre pasos
// ============================================================
function renderWizardStep() {
    [1, 2, 3].forEach(i => {
        const panel   = document.getElementById(`wizardPanel${i}`);
        const stepEl  = document.getElementById(`wstep-${i}`);
        const dotEl   = document.getElementById(`wdot-${i}`);

        panel.style.display = i === currentStep ? '' : 'none';

        stepEl.classList.remove('active', 'done');
        if (i === currentStep)   stepEl.classList.add('active');
        else if (i < currentStep) stepEl.classList.add('done');

        dotEl.innerHTML = i < currentStep
            ? '<i class="bi bi-check"></i>'
            : String(i);
    });

    const btnBack = document.getElementById('btnBack');
    btnBack.style.display = currentStep > 1 ? '' : 'none';

    const btnGuardar = document.getElementById('btnGuardar');
    if (currentStep === 3) {
        const esEdicion = !!editandoMuestraId;
        btnGuardar.innerHTML = `<i class="bi bi-check-lg"></i> ${esEdicion ? 'Guardar cambios' : 'Guardar muestra'}`;
    } else {
        btnGuardar.innerHTML = 'Siguiente <i class="bi bi-arrow-right"></i>';
    }
}

function wizardNext() {
    if (currentStep === 1 && !validarFormulario()) return;
    if (currentStep < 3) {
        currentStep++;
        renderWizardStep();
        return;
    }
    // Paso 3 → submit
    if (editandoMuestraId) {
        guardarEdicionMuestra();
    } else {
        altaMuestra();
    }
}

function wizardPrev() {
    if (currentStep > 1) {
        currentStep--;
        renderWizardStep();
    }
}

window.wizardGoTo = function(step) {
    if (step >= currentStep) return; // solo se puede volver a pasos completados
    currentStep = step;
    renderWizardStep();
};

// ============================================================
// 2. VINCULACIÓN DE EVENTOS (centralizada, sin inline en HTML)
// ============================================================
function vincularEventos() {

    // — Modal —
    document.getElementById("btnAbrirModal").addEventListener("click", abrirModal);
    document.getElementById("btnCancelar").addEventListener("click", cerrarModal);
    document.getElementById("modalClose").addEventListener("click", cerrarModal);

    // Cerrar haciendo clic en el fondo oscuro
    document.getElementById("modalAltaMuestra").addEventListener("click", (e) => {
        if (e.target === document.getElementById("modalAltaMuestra")) cerrarModal();
    });

    // — Modal de detalle (solo lectura) —
    document.getElementById("btnCerrarDetalle").addEventListener("click", cerrarModalDetalle);
    document.getElementById("modalDetalleClose").addEventListener("click", cerrarModalDetalle);
    document.getElementById("modalDetalleMuestra").addEventListener("click", (e) => {
        if (e.target === document.getElementById("modalDetalleMuestra")) cerrarModalDetalle();
    });

    // Cerrar con Escape (cualquier modal abierto)
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            cerrarModal();
            cerrarModalDetalle();
            cerrarAltaInforme();
            cerrarModalCancelar();
        }
    });

    // — Selectores dependientes —
    // onCambioMatriz se dispara desde el callback onChange de TomSelect (no desde el <select> nativo)
    document.getElementById("checkSinNormativa").addEventListener("change", onToggleSinNormativa);

    // — Parámetros: selección masiva y buscador individual —
    document.getElementById("btnSelectAll").addEventListener("click", () => selectAllParams(true));
    document.getElementById("btnSelectNone").addEventListener("click", () => selectAllParams(false));
    document.getElementById("btnAddParam").addEventListener("click", abrirBuscadorIndividual);
    document.getElementById("btnCerrarBuscadorIndividual").addEventListener("click", cerrarPanelBuscador);
    document.getElementById("inputBuscarParametroIndividual").addEventListener("input", onBuscarParametroIndividual);

    // — Navegación wizard —
    document.getElementById("btnGuardar").addEventListener("click", wizardNext);
    document.getElementById("btnBack").addEventListener("click", wizardPrev);
    renderWizardStep();

    // — Filtros de estado (delegación desde el contenedor) —
    document.querySelector(".filtros").addEventListener("click", (e) => {
        const btn = e.target.closest(".filtro-btn");
        if (!btn) return;
        document.querySelectorAll(".filtro-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        estadoActivo = btn.dataset.estado;
        paginaActual = 0;
        aplicarFiltrosYBusqueda();
    });

    // — Búsqueda por texto —
    document.getElementById("inputBuscarCodigo").addEventListener("input", aplicarFiltrosYBusqueda);
    document.getElementById("inputBuscarCliente").addEventListener("input", aplicarFiltrosYBusqueda);

    // — Generar informe PDF —
    document.getElementById("btnGenerarInforme").addEventListener("click", onGenerarInforme);

    // Live re-evaluation de badges y auto-guardado
    function onResultadoChange(e) {
        if (e.target.classList.contains("param-resultado-input")) {
            e.target.closest(".param-card").querySelectorAll(".badge-cumple[data-tipo]").forEach(badge => {
                actualizarBadge(badge, e.target.value);
            });
            recalcularEstadoBtnGenerarInforme();
        }
        if (e.target.classList.contains("param-resultado-input") || e.target.classList.contains("param-obs-input")) {
            dispararAutoGuardar();
        }
    }
    const contParam = document.getElementById("detalleParametros");
    contParam.addEventListener("input", onResultadoChange);
    contParam.addEventListener("change", onResultadoChange);
    contParam.addEventListener("click", e => {
        const toggle = e.target.closest(".param-toggle");
        if (!toggle) return;
        const colapsable = toggle.nextElementSibling;
        const isOpen = toggle.getAttribute("aria-expanded") === "true";
        toggle.setAttribute("aria-expanded", String(!isOpen));
        colapsable.style.display = isOpen ? "none" : "block";
        toggle.querySelector(".param-toggle-icon").style.transform = isOpen ? "" : "rotate(180deg)";
        toggle.querySelector("span").textContent = isOpen
            ? `Ver normativas (${toggle.dataset.count})`
            : "Ocultar normativas";
    });

    // — Modal Cancelar Muestra —
    document.getElementById("modalCancelarClose").addEventListener("click", cerrarModalCancelar);
    document.getElementById("btnCancelarCancelar").addEventListener("click", cerrarModalCancelar);
    document.getElementById("modalCancelarMuestra").addEventListener("click", (e) => {
        if (e.target === document.getElementById("modalCancelarMuestra")) cerrarModalCancelar();
    });
    document.getElementById("btnConfirmarCancelar").addEventListener("click", confirmarCancelarMuestra);

    // — Modal Alta Informe —
    document.getElementById("altaInformeClose").addEventListener("click", cerrarAltaInforme);
    document.getElementById("altaInformeCancelar").addEventListener("click", cerrarAltaInforme);
    document.getElementById("modalAltaInforme").addEventListener("click", (e) => {
        if (e.target === document.getElementById("modalAltaInforme")) cerrarAltaInforme();
    });
    document.getElementById("btnUploadAltaInforme").addEventListener("click", onUploadAltaInforme);
}


async function abrirModal() {
    currentStep = 1;
    renderWizardStep();
    document.getElementById("modalAltaMuestra").classList.add("visible");
    document.getElementById("inputFecha").value = new Date().toISOString().slice(0, 10);

    // Pre-llenar el número de protocolo con el próximo sugerido (editable)
    const inputProtocolo = document.getElementById("inputProtocolo");
    inputProtocolo.value = "";
    inputProtocolo.placeholder = "Cargando...";
    try {
        const resp = await fetchConAuth(`${API_URL}/numeradores/preview/NUMERO_PROTOCOLO`);
        if (resp.ok) {
            const data = await resp.json();
            inputProtocolo.value = String(data.siguiente);
        }
    } catch (e) {
        // si falla, el usuario puede ingresarlo manualmente
    } finally {
        inputProtocolo.placeholder = "Nº de protocolo";
        inputProtocolo.focus();
    }
}

function cerrarModal() {
    document.getElementById("modalAltaMuestra").classList.remove("visible");
    document.getElementById("formAltaMuestra").reset();
    tomSelectCliente?.clear();
    tomSelectMatriz?.clear();
    document.getElementById("parametrosLista").innerHTML = "";
    document.getElementById("parametrosVacio").style.display = "flex";
    document.getElementById("normativasContainer").innerHTML =
        '<span class="text-muted small">Seleccioná una matriz en el paso anterior para ver las normativas aplicables...</span>';
    document.getElementById("checkSinNormativa").checked = false;
    document.getElementById("normativasContainer").classList.remove("disabled-panel");
    destinosSeleccionados.clear();
    parametrosPorDestinoCache.clear();
    cerrarPanelBuscador();
    limpiarErrores();

    // Restaurar modo alta
    editandoMuestraId = null;
    currentStep = 1;
    document.getElementById("modalTitulo").textContent = "Alta de muestra";
    document.getElementById("inputProtocolo").disabled = false;
    tomSelectCliente?.enable();
    document.getElementById("modalAltaLoading").classList.add("d-none");
    renderWizardStep();
}

window.abrirEdicionMuestra = async function(id) {
    currentStep = 1;
    editandoMuestraId = id;

    document.getElementById("modalTitulo").textContent = "Editar muestra";
    document.getElementById("inputProtocolo").disabled = true;
    tomSelectCliente?.disable();

    renderWizardStep();
    document.getElementById("modalAltaMuestra").classList.add("visible");
    document.getElementById("modalAltaLoading").classList.remove("d-none");

    try {
        const detalle = await obtenerDetalleMuestra(id);

        // Poblar todos los campos visibles
        document.getElementById("inputProtocolo").value     = detalle.nroProtocolo || "";
        document.getElementById("inputFecha").value         = detalle.fechaIngreso  || "";
        document.getElementById("inputPuntoMuestreo").value = detalle.puntoMuestreo || "";
        document.getElementById("inputFechaEntrega").value  = detalle.fechaEntrega  || "";

        if (detalle.clienteId) {
            tomSelectCliente?.setValue(String(detalle.clienteId));
        }

        if (detalle.matrizId) {
            tomSelectMatriz
                ? tomSelectMatriz.setValue(String(detalle.matrizId), true)
                : (document.getElementById("inputTipoMuestra").value = detalle.matrizId);
            // onCambioMatriz carga el árbol de normativas y resetea destinosSeleccionados
            await onCambioMatriz({ target: { value: String(detalle.matrizId) } });
            if (detalle.tipoMuestraId) {
                document.getElementById("inputTipoMuestraEspecifica").value = detalle.tipoMuestraId;
            }
        }

        // Pre-seleccionar los destinos que ya tenía la muestra
        const resolucionDestinoIds = detalle.resolucionDestinoIds || [];
        resolucionDestinoIds.forEach(destinoId => {
            const checkbox = document.getElementById(`check-destino-${destinoId}`);
            if (checkbox) {
                checkbox.checked = true;
                destinosSeleccionados.add(destinoId);
            }
        });

        // Recalcular la lista de parámetros derivados de los destinos pre-seleccionados
        recalcularParametrosSeleccionados();

        // IDs de parámetros actualmente en la muestra
        const paramIdsEnMuestra = new Set((detalle.parametros || []).map(p => p.id));

        // Desmarcar los que vinieron de destinos pero no estaban en la muestra
        document.querySelectorAll(".check-parametro").forEach(cb => {
            if (!paramIdsEnMuestra.has(parseInt(cb.value))) {
                cb.checked = false;
            }
        });

        // Agregar manualmente los parámetros que no vinieron de ningún destino
        const idsYaEnLista = new Set(
            Array.from(document.querySelectorAll(".parametro-item-row"))
                .map(el => parseInt(el.dataset.parametroId))
        );
        for (const p of (detalle.parametros || [])) {
            if (!idsYaEnLista.has(p.id)) {
                agregarParametroALaLista(
                    { id: p.id, nombre: p.nombre, unidad: p.unidad,
                      metodologia: { nombre: p.metodologiaNombre } },
                    "manual"
                );
            }
        }

        if (document.querySelectorAll(".parametro-item-row").length > 0) {
            document.getElementById("parametrosVacio").style.display = "none";
        }

    } catch (err) {
        console.error("Error cargando muestra para editar:", err);
        mostrarToast("No se pudo cargar la muestra.", true);
        cerrarModal();
        return;
    } finally {
        document.getElementById("modalAltaLoading").classList.add("d-none");
    }
};


// ============================================================
// 4. CARGA DE DATOS INICIALES
// ============================================================

// FIX: usa /api/clientes (ClienteDE), no /api/users — un cliente puede no tener
// usuario asignado todavía, y el nombre a mostrar depende de tipoCliente.
async function cargarClientes() {
    const select = document.getElementById("inputCliente");
    try {
        const response = await fetchConAuth(`${API_URL}/clientes`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const clientes = await response.json();
        select.innerHTML = '<option value="">Seleccioná un cliente...</option>';
        clientes.forEach(c => {
            const opt = document.createElement("option");
            opt.value = c.id;
            const nombreMostrado = c.tipoCliente === "PERSONA_FISICA"
                ? `${c.nombre || ""} ${c.apellido || ""}`.trim()
                : (c.razonSocial || c.nombre || c.email);
            opt.textContent = nombreMostrado || c.email;
            select.appendChild(opt);
        });
    } catch (error) {
        console.warn("No se pudieron cargar clientes:", error);
        select.innerHTML = '<option value="">Sin clientes disponibles</option>';
    }

    if (tomSelectCliente) {
        tomSelectCliente.destroy();
    }
    tomSelectCliente = new TomSelect('#inputCliente', {
        placeholder: 'Buscar cliente...',
        allowEmptyOption: false,
        maxOptions: null,
        sortField: { field: 'text', direction: 'asc' }
    });
}

async function cargarMatrices() {
    const select = document.getElementById("inputTipoMuestra");
    try {
        const response = await fetchConAuth(`${API_URL}/matrices`);
        const matrices = await response.json();

        select.innerHTML = '<option value="">Seleccioná una matriz...</option>';
        matrices
            .filter(m => m.activo)
            .forEach(m => {
                const opt = document.createElement("option");
                opt.value = m.id;
                opt.textContent = m.nombre;
                select.appendChild(opt);
            });
    } catch (error) {
        console.error("Error al cargar matrices:", error);
    }

    if (tomSelectMatriz) tomSelectMatriz.destroy();
    tomSelectMatriz = new TomSelect('#inputTipoMuestra', {
        placeholder: 'Buscar matriz...',
        allowEmptyOption: true,
        maxOptions: null,
        sortField: { field: 'text', direction: 'asc' },
        onChange: (value) => onCambioMatriz({ target: { value: String(value) } })
    });
}

async function cargarMuestrasActivas() {
    const tbody = document.getElementById("tablaMuestrasBody");
    tbody.innerHTML = `<tr><td colspan="7" class="text-center">Cargando...</td></tr>`;
    try {
        const response = await fetchConAuth(`${API_URL}/estudios/all`);
        todasLasMuestras = await response.json();
        aplicarFiltrosYBusqueda();
    } catch (error) {
        console.error("Error al cargar muestras:", error);
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-danger">
                    Error al cargar la cola de trabajo
                </td>
            </tr>`;
    }
}


// ============================================================
// 5. SELECTORES DEPENDIENTES: Matriz → Normativas (múltiple) → Parámetros
// ============================================================

// Trae el árbol completo Matriz → Resoluciones → Destinos → Parámetros.
// Usa el mock local mientras el endpoint real no está disponible (ver flag USAR_MOCK_NORMATIVAS).
async function obtenerArbolPorMatriz(matrizId) {
    if (USAR_MOCK_NORMATIVAS) {
        return mockArbolPorMatriz(matrizId);
    }
    const response = await fetchConAuth(`${API_URL}/resoluciones/por-matriz/${matrizId}`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
}

async function cargarTiposMuestra(matrizId) {
    const select = document.getElementById("inputTipoMuestraEspecifica");
    select.innerHTML = '<option value="">— sin especificar —</option>';
    if (!matrizId) return;
    try {
        const res = await fetchConAuth(`${API_URL}/tipos-muestra?matrizId=${matrizId}`);
        if (!res.ok) return;
        const tipos = await res.json();
        tipos.forEach(t => {
            const opt = document.createElement("option");
            opt.value = t.id;
            opt.textContent = t.nombre;
            select.appendChild(opt);
        });
    } catch (err) {
        console.error("Error cargando tipos de muestra:", err);
    }
}

async function onCambioMatriz(e) {
    const matrizId = e.target.value;
    const contenedor = document.getElementById("normativasContainer");

    // Reset total: cambiar de matriz invalida las normativas y parámetros elegidos
    destinosSeleccionados.clear();
    parametrosPorDestinoCache.clear();
    destinosNombresCache.clear();
    contenedor.innerHTML = "";
    recalcularParametrosSeleccionados();
    cargarTiposMuestra(matrizId);

    // Si tildaron "sin normativa", no tiene sentido ir a buscar el árbol
    if (document.getElementById("checkSinNormativa").checked) {
        return;
    }

    if (!matrizId) {
        contenedor.innerHTML = '<span class="text-muted small">Seleccioná una matriz para ver las normativas aplicables...</span>';
        return;
    }

    contenedor.innerHTML = '<span class="text-muted small">Cargando normativas...</span>';

    try {
        const arbol = await obtenerArbolPorMatriz(matrizId);

        if (!arbol.resoluciones || arbol.resoluciones.length === 0) {
            contenedor.innerHTML = '<span class="text-muted small">No hay normativas cargadas para esta matriz todavía.</span>';
            return;
        }

        renderizarNormativas(arbol.resoluciones);

    } catch (error) {
        console.error("Error al cargar normativas de la matriz:", error);
        contenedor.innerHTML = '<span class="text-danger small">Error al cargar las normativas.</span>';
    }
}

// Al tildar "No aplica ninguna normativa": oculta y deshabilita el bloque de destinos,
// limpia cualquier destino tildado (y sus parámetros derivados), dejando solo el buscador individual.
function onToggleSinNormativa(e) {
    const contenedor = document.getElementById("normativasContainer");
    const sinNormativa = e.target.checked;

    if (sinNormativa) {
        destinosSeleccionados.clear();
        parametrosPorDestinoCache.clear();
        contenedor.innerHTML = '<span class="text-muted small fst-italic">Normativa desactivada para esta muestra — agregá parámetros con el buscador individual.</span>';
        contenedor.classList.add("disabled-panel");
        recalcularParametrosSeleccionados();
    } else {
        contenedor.classList.remove("disabled-panel");
        // Volvemos a pedir el árbol si ya había una matriz elegida
        const matrizId = document.getElementById("inputTipoMuestra").value;
        if (matrizId) {
            onCambioMatriz({ target: { value: matrizId } });
        } else {
            contenedor.innerHTML = '<span class="text-muted small">Seleccioná una matriz para ver las normativas aplicables...</span>';
        }
    }
}

// Pinta, por cada Resolución, sus Destinos como checkboxes (o "Único" si tieneDestino=false)
function renderizarNormativas(resoluciones) {
    const contenedor = document.getElementById("normativasContainer");
    contenedor.innerHTML = "";

    resoluciones.forEach(res => {
        const bloque = document.createElement("div");
        bloque.className = "normativa-bloque mb-2 pb-2 border-bottom";

        const titulo = document.createElement("div");
        titulo.className = "fw-semibold small mb-1";
        titulo.textContent = res.nombre;
        bloque.appendChild(titulo);

        const destinosWrap = document.createElement("div");
        destinosWrap.className = "d-flex flex-wrap gap-3";

        (res.destinos || []).forEach(destino => {
            parametrosPorDestinoCache.set(destino.id, destino.parametros || []);
            // Etiqueta corta: si la resolución no tiene destino real ("Único"), solo mostrar el nombre de la resolución
            destinosNombresCache.set(destino.id, res.tieneDestino
                ? `${res.nombre} — ${destino.nombre}`
                : res.nombre);

            const wrapper = document.createElement("div");
            wrapper.className = "form-check";

            const checkboxId = `check-destino-${destino.id}`;
            wrapper.innerHTML = `
                <input class="form-check-input check-destino" type="checkbox"
                       id="${checkboxId}" value="${destino.id}">
                <label class="form-check-label small" for="${checkboxId}">
                    ${destino.nombre}
                </label>
            `;

            wrapper.querySelector("input").addEventListener("change", (ev) => {
                if (ev.target.checked) {
                    destinosSeleccionados.add(destino.id);
                } else {
                    destinosSeleccionados.delete(destino.id);
                }
                recalcularParametrosSeleccionados();
            });

            destinosWrap.appendChild(wrapper);
        });

        bloque.appendChild(destinosWrap);
        contenedor.appendChild(bloque);
    });
}

// Recalcula la lista de parámetros a partir de todos los destinos tildados,
// evitando duplicados cuando el mismo parámetro aparece en más de un destino.
function recalcularParametrosSeleccionados() {
    const contenedorLista = document.getElementById("parametrosLista");
    const panelVacio = document.getElementById("parametrosVacio");

    // Conservamos los parámetros agregados a mano (buscador individual) que no vinieron de un destino
    const idsManuales = Array.from(contenedorLista.querySelectorAll(".parametro-item-row"))
        .filter(fila => fila.dataset.origen === "manual")
        .map(fila => fila.dataset.parametroId);

    contenedorLista.innerHTML = "";

    const parametrosUnicos   = new Map(); // id -> parametro
    const paramDestinoLabels = new Map(); // id -> string[]

    destinosSeleccionados.forEach(destinoId => {
        const parametros = parametrosPorDestinoCache.get(destinoId) || [];
        const label      = destinosNombresCache.get(destinoId) || String(destinoId);
        parametros.forEach(p => {
            parametrosUnicos.set(p.id, p);
            if (!paramDestinoLabels.has(p.id)) paramDestinoLabels.set(p.id, []);
            paramDestinoLabels.get(p.id).push(label);
        });
    });

    if (parametrosUnicos.size === 0 && idsManuales.length === 0) {
        const sinNormativa = document.getElementById("checkSinNormativa").checked;
        panelVacio.innerHTML = sinNormativa
            ? '<i class="bi bi-list-ul param-empty-icon"></i> Esta muestra no tiene normativa asociada — agregá parámetros con "Agregar individual".'
            : '<i class="bi bi-list-ul param-empty-icon"></i> Seleccioná un destino de vuelco o agregá parámetros manualmente';
        panelVacio.style.display = "flex";
        return;
    }

    panelVacio.style.display = "none";
    [...parametrosUnicos.values()]
        .sort((a, b) => a.nombre.localeCompare(b.nombre, "es"))
        .forEach(p => agregarParametroALaLista(p, "norma", paramDestinoLabels.get(p.id) || []));

    // Reponer los agregados manualmente (si el usuario ya había buscado alguno antes)
    idsManuales.forEach(id => {
        const param = todosLosParametrosCache.find(p => String(p.id) === String(id));
        if (param && !parametrosUnicos.has(param.id)) {
            agregarParametroALaLista(param, "manual");
        }
    });
}


// ============================================================
// 6. BUSCADOR INDIVIDUAL DE PARÁMETROS
// ============================================================
async function abrirBuscadorIndividual() {
    const buscadorContainer = document.getElementById("buscadorIndividualContainer");
    buscadorContainer.classList.remove("d-none");
    document.getElementById("inputBuscarParametroIndividual").focus();

    if (todosLosParametrosCache.length === 0) {
        try {
            const response = await fetchConAuth(`${API_URL}/parametros`);
            todosLosParametrosCache = await response.json();
        } catch (error) {
            console.error("Error cargando parámetros:", error);
        }
    }
}

function onBuscarParametroIndividual(e) {
    const termino = e.target.value.toLowerCase().trim();
    const contenedor = document.getElementById("resultadosBusquedaIndividual");
    contenedor.innerHTML = "";

    if (!termino) return;

    const filtrados = todosLosParametrosCache.filter(p =>
        p.nombre.toLowerCase().includes(termino)
    );

    if (filtrados.length === 0) {
        contenedor.innerHTML = `<div class="list-group-item text-muted small">Sin resultados para "${termino}"</div>`;
        return;
    }

    filtrados.slice(0, 10).forEach(param => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "list-group-item list-group-item-action py-2";
        const metodo = param.metodologia?.nombre || "Sin metodología";
        btn.innerHTML = `
            <strong>${param.nombre}</strong>
            <span class="text-muted small ms-1">(${param.unidad || '-'})</span>
            <span class="badge bg-light text-dark border float-end">${metodo}</span>
        `;
        btn.addEventListener("click", () => {
            if (document.getElementById(`check-param-${param.id}`)) {
                mostrarToast(`El parámetro "${param.nombre}" ya está agregado.`, true);
                return;
            }
            agregarParametroALaLista(param, "manual");
            cerrarPanelBuscador();
        });
        contenedor.appendChild(btn);
    });
}

function cerrarPanelBuscador() {
    document.getElementById("buscadorIndividualContainer").classList.add("d-none");
    document.getElementById("inputBuscarParametroIndividual").value = "";
    document.getElementById("resultadosBusquedaIndividual").innerHTML = "";
}


// ============================================================
// 7. AGREGAR PARÁMETRO A LA LISTA VISUAL
// ============================================================
function agregarParametroALaLista(parametro, origen = "manual", destinoLabels = []) {
    const contenedorLista = document.getElementById("parametrosLista");
    document.getElementById("parametrosVacio").style.display = "none";

    if (contenedorLista.querySelector(`[data-parametro-id="${parametro.id}"]`)) {
        return;
    }

    const fila = document.createElement("div");
    fila.className = "param-select-card selected";
    fila.dataset.parametroId = parametro.id;
    fila.dataset.origen = origen;

    const metodo = parametro.metodologia?.nombre || "";
    const normaChips = destinoLabels.map(l => `<span class="param-destino-chip">${l}</span>`).join("");

    fila.innerHTML = `
        <label class="param-select-label" for="check-param-${parametro.id}">
            <input type="checkbox" class="param-select-checkbox check-parametro"
                   value="${parametro.id}" id="check-param-${parametro.id}" checked>
            <span class="param-select-check-icon"><i class="bi bi-check-lg"></i></span>
            <div class="param-select-body">
                <div class="param-select-top">
                    <span class="param-select-nombre">${parametro.nombre}</span>
                    ${parametro.unidad ? `<span class="param-select-unidad">${parametro.unidad}</span>` : ""}
                </div>
                <div class="param-select-footer">
                    ${metodo ? `<span class="param-select-metodo">${metodo}</span>` : ""}
                    ${normaChips}
                </div>
            </div>
        </label>
    `;

    fila.querySelector(".param-select-checkbox").addEventListener("change", function () {
        fila.classList.toggle("selected", this.checked);
        actualizarContadorParams();
    });

    contenedorLista.appendChild(fila);
    actualizarContadorParams();
}

function actualizarContadorParams() {
    const total = document.querySelectorAll(".check-parametro").length;
    const sel   = document.querySelectorAll(".check-parametro:checked").length;
    const el    = document.getElementById("paramContador");
    if (el) el.textContent = total > 0 ? `${sel} de ${total} seleccionados` : "";
}

function selectAllParams(checked) {
    document.querySelectorAll(".check-parametro").forEach(cb => {
        cb.checked = checked;
        cb.closest(".param-select-card").classList.toggle("selected", checked);
    });
    actualizarContadorParams();
}


// ============================================================
// 8. SUBMIT DEL FORMULARIO
// ============================================================
async function guardarEdicionMuestra() {
    const parametrosIds = Array.from(document.querySelectorAll(".check-parametro:checked"))
        .map(cb => parseInt(cb.value));

    if (parametrosIds.length === 0) {
        mostrarToast("Seleccioná al menos un parámetro.", true);
        return;
    }

    const payload = {
        matrizId:             document.getElementById("inputTipoMuestra").value
                                  ? parseInt(document.getElementById("inputTipoMuestra").value) : null,
        tipoMuestraId:        document.getElementById("inputTipoMuestraEspecifica").value
                                  ? parseInt(document.getElementById("inputTipoMuestraEspecifica").value) : null,
        puntoMuestreo:        document.getElementById("inputPuntoMuestreo").value.trim() || null,
        fechaIngreso:         document.getElementById("inputFecha").value || null,
        fechaEntrega:         document.getElementById("inputFechaEntrega").value || null,
        resolucionDestinoIds: Array.from(destinosSeleccionados),
        parametrosIds,
    };

    const btn = document.getElementById("btnGuardar");
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Guardando...';

    try {
        const resp = await fetchConAuth(`${API_URL}/estudios/${editandoMuestraId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        mostrarToast("Muestra actualizada correctamente.");
        cerrarModal();
        await cargarMuestrasActivas();
    } catch (err) {
        console.error("Error actualizando muestra:", err);
        mostrarToast("No se pudo actualizar la muestra.", true);
    } finally {
        btn.disabled = false;
        renderWizardStep();
    }
}

async function altaMuestra() {
    const parametrosIds = Array.from(document.querySelectorAll(".check-parametro:checked"))
        .map(cb => parseInt(cb.value));

    if (parametrosIds.length === 0) {
        mostrarToast("Seleccioná al menos un parámetro para analizar.", true);
        return;
    }

    const payload = {
        nroProtocolo:         document.getElementById("inputProtocolo").value.trim(),
        fechaIngreso:         document.getElementById("inputFecha").value,
        fechaEntrega:         document.getElementById("inputFechaEntrega").value || null,
        clienteId:            parseInt(document.getElementById("inputCliente").value),
        puntoMuestreo:        document.getElementById("inputPuntoMuestreo").value.trim() || null,
        tipoMuestraId:        document.getElementById("inputTipoMuestraEspecifica").value
                                  ? parseInt(document.getElementById("inputTipoMuestraEspecifica").value) : null,
        matrizId:             parseInt(document.getElementById("inputTipoMuestra").value),
        resolucionDestinoIds: Array.from(destinosSeleccionados),
        observaciones:        document.getElementById("inputObservaciones").value.trim() || null,
        parametrosIds,
    };

    const btnGuardar = document.getElementById("btnGuardar");
    btnGuardar.disabled = true;
    btnGuardar.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Guardando...`;

    try {
        const response = await fetchConAuth(`${API_URL}/estudios`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const err = await response.json().catch(() => ({}));
            throw new Error(err.message || "Error en el servidor");
        }

        mostrarToast("Muestra guardada correctamente.");
        cerrarModal();
        await cargarMuestrasActivas();

    } catch (error) {
        console.error("Error al guardar muestra:", error);
        mostrarToast(`No se pudo guardar la muestra: ${error.message}`, true);
    } finally {
        btnGuardar.disabled = false;
        renderWizardStep();
    }
}


// ============================================================
// 9. VALIDACIÓN DEL FORMULARIO
// ============================================================
function validarFormulario() {
    let valido = true;
    limpiarErrores();

    const campos = [
        { id: "inputFecha",       errId: "errFecha" },
        { id: "inputTipoMuestra", errId: "errTipoMuestra" },
    ];

    // Validar cliente
    const clienteVal = document.getElementById("inputCliente").value;
    if (!clienteVal) {
        document.getElementById("errCliente").style.display = "block";
        valido = false;
    }

    campos.forEach(({ id, errId }) => {
        const el = document.getElementById(id);
        if (!el.value || el.value.trim() === "") {
            document.getElementById(errId).style.display = "block";
            valido = false;
        }
    });

    return valido;
}

function limpiarErrores() {
    document.querySelectorAll(".field-error").forEach(el => el.style.display = "none");
}


// ============================================================
// 10. FILTROS Y BÚSQUEDA EN LA TABLA
// ============================================================
function ordenarPor(col) {
    sortDir = sortCol === col && sortDir === "asc" ? "desc" : "asc";
    sortCol = col;
    aplicarFiltrosYBusqueda();
}

function ordenar(lista) {
    if (!sortCol) return lista;
    return [...lista].sort((a, b) => {
        const va = (a[sortCol] || "").toString();
        const vb = (b[sortCol] || "").toString();
        const cmp = va.localeCompare(vb, "es", { numeric: true });
        return sortDir === "asc" ? cmp : -cmp;
    });
}

function actualizarIconosOrden() {
    document.querySelectorAll(".th-sortable").forEach(th => {
        const icon = th.querySelector(".sort-icon");
        if (!icon) return;
        if (th.dataset.sort === sortCol) {
            icon.className = `sort-icon bi bi-chevron-${sortDir === "asc" ? "up" : "down"} sort-activo`;
        } else {
            icon.className = "sort-icon bi bi-chevron-expand";
        }
    });
}

function aplicarFiltrosYBusqueda() {
    const textoCodigo  = document.getElementById("inputBuscarCodigo").value.toLowerCase().trim();
    const textoCliente = document.getElementById("inputBuscarCliente").value.toLowerCase().trim();

    let filtradas = todasLasMuestras;

    // Filtro por estado
    if (estadoActivo !== "todos") {
        filtradas = filtradas.filter(m => m.estado === estadoActivo);
    }

    // Filtro por código
    if (textoCodigo) {
        filtradas = filtradas.filter(m =>
            (m.nroProtocolo || String(m.id) || "")
                .toLowerCase().includes(textoCodigo)
        );
    }

    // Filtro por cliente
    if (textoCliente) {
        filtradas = filtradas.filter(m =>
            (m.cliente || "").toLowerCase().includes(textoCliente)
        );
    }

    renderizarTablaMuestras(ordenar(filtradas));
    actualizarIconosOrden();
}


// ============================================================
// 11. RENDER DE LA TABLA CON PAGINACIÓN
// ============================================================
function renderizarTablaMuestras(lista) {
    const tbody = document.getElementById("tablaMuestrasBody");
    tbody.innerHTML = "";

    if (lista.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-muted py-4">
                    No hay muestras que coincidan con los filtros.
                </td>
            </tr>`;
        actualizarPaginacion(0, 0);
        return;
    }

    // Paginación en cliente (mientras el backend no tenga paginación propia)
    const inicio = paginaActual * ITEMS_POR_PAGINA;
    const fin = Math.min(inicio + ITEMS_POR_PAGINA, lista.length);
    const pagina = lista.slice(inicio, fin);

    pagina.forEach(m => {
        const fila = document.createElement("tr");
        const codigo = m.nroProtocolo || m.id || "S/N";
        const puedeGenerar  = m.estado === "COMPLETO_SIN_INFORME";
        const esCancelado   = m.estado === "CANCELADO";
        const protocolo     = (m.nroProtocolo || m.id || "").toString().replace(/'/g, "");
        fila.innerHTML = `
            <td><strong>${codigo}</strong></td>
            <td>${m.cliente || '—'}</td>
            <td>${m.matrizNombre || m.tipoAnalisis || '—'}</td>
            <td>${badgeHTML(m.estado)}</td>
            <td>${formatearFecha(m.fechaIngreso)}</td>
            <td>${formatearFecha(m.fechaEntrega)}</td>
            <td class="acciones-celda">
                <button class="btn-accion" title="Ver detalle"
                        onclick="verDetalleMuestra(${m.id})">
                    <i class="bi bi-eye"></i>
                </button>
                ${!esCancelado ? `
                <button class="btn-accion" title="Editar datos"
                        onclick="abrirEdicionMuestra(${m.id})">
                    <i class="bi bi-pencil"></i>
                </button>` : ''}
                ${puedeGenerar ? `
                <button class="btn-accion btn-accion-verde" title="Generar informe PDF"
                        onclick="onGenerarInformeDesdeTabla(${m.id})">
                    <i class="bi bi-file-earmark-pdf-fill"></i>
                </button>` : ''}
                ${!esCancelado ? `
                <button class="btn-accion btn-accion-gris" title="Ver / subir archivos"
                        onclick="abrirAltaInforme(${m.id}, '${protocolo}')">
                    <i class="bi bi-paperclip"></i>
                </button>
                <button class="btn-accion btn-accion-rojo" title="Cancelar muestra"
                        onclick="abrirModalCancelar(${m.id}, '${codigo}')">
                    <i class="bi bi-x-circle"></i>
                </button>` : ''}
            </td>
        `;
        tbody.appendChild(fila);
    });

    actualizarPaginacion(lista.length, fin);
}

function actualizarPaginacion(total, fin) {
    const inicio = paginaActual * ITEMS_POR_PAGINA;
    document.getElementById("pagInfoEmpleado").textContent =
        total === 0 ? "Sin resultados" : `Mostrando ${inicio + 1}–${fin} de ${total}`;

    const controles = document.getElementById("pagControlsEmpleado");
    controles.innerHTML = "";
    const totalPaginas = Math.ceil(total / ITEMS_POR_PAGINA);
    if (totalPaginas <= 1) return;

    if (paginaActual > 0) {
        const btnAnterior = document.createElement("button");
        btnAnterior.className = "btn btn-sm btn-outline-secondary me-1";
        btnAnterior.textContent = "← Anterior";
        btnAnterior.addEventListener("click", () => {
            paginaActual--;
            aplicarFiltrosYBusqueda();
        });
        controles.appendChild(btnAnterior);
    }

    if (paginaActual < totalPaginas - 1) {
        const btnSiguiente = document.createElement("button");
        btnSiguiente.className = "btn btn-sm btn-outline-secondary";
        btnSiguiente.textContent = "Siguiente →";
        btnSiguiente.addEventListener("click", () => {
            paginaActual++;
            aplicarFiltrosYBusqueda();
        });
        controles.appendChild(btnSiguiente);
    }
}


// ============================================================
// 12. HELPERS
// ============================================================

// Wrapper de fetch que inyecta el JWT automáticamente
async function fetchConAuth(url, opciones = {}) {
    const token = localStorage.getItem("token");
    const headers = {
        ...(opciones.headers || {}),
        "Authorization": `Bearer ${token}`
    };
    const response = await fetch(url, { ...opciones, headers });
    if (response.status === 401) {
        // Token vencido — redirigir al login
        window.location.href = "/login.html";
    }
    return response;
}

function establecerFechaHoy() {
    const el = document.getElementById("fecha-hoy");
    if (el) {
        el.textContent = new Date().toLocaleDateString("es-ES", {
            weekday: "long", year: "numeric", month: "long", day: "numeric"
        });
    }
}

function formatearFecha(fecha) {
    if (!fecha) return "—";
    try {
        return new Date(fecha).toLocaleDateString("es-AR");
    } catch {
        return fecha;
    }
}

function mostrarToast(mensaje, esError = false) {
    const toast = document.getElementById("toastConfirm");
    const msg   = document.getElementById("toastMsg");
    if (!toast || !msg) return;
    msg.textContent = mensaje;
    toast.style.backgroundColor = esError ? "#dc3545" : "";
    toast.classList.add("visible");
    setTimeout(() => {
        toast.classList.remove("visible");
        toast.style.backgroundColor = "";
    }, 3500);
}

// ============================================================
// DETALLE DE MUESTRA (modal de solo lectura)
// ============================================================
async function obtenerDetalleMuestra(id) {
    if (USAR_MOCK_DETALLE) {
        return mockDetalleMuestra(id);
    }
    const response = await fetchConAuth(`${API_URL}/estudios/${id}/detalle`);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
}

window.verDetalleMuestra = async function(id) {
    detalleAnalisisId = id;
    const modal = document.getElementById("modalDetalleMuestra");
    const loading = document.getElementById("detalleLoading");
    const contenido = document.getElementById("detalleContenido");
    const errorBox = document.getElementById("detalleError");

    modal.classList.add("visible");
    loading.classList.remove("d-none");
    contenido.classList.add("d-none");
    errorBox.classList.add("d-none");
    document.getElementById("detalleProtocolo").textContent = "…";

    try {
        const detalle = await obtenerDetalleMuestra(id);
        renderizarDetalleMuestra(detalle);
        loading.classList.add("d-none");
        contenido.classList.remove("d-none");
    } catch (error) {
        console.error("Error al cargar detalle de muestra:", error);
        loading.classList.add("d-none");
        errorBox.classList.remove("d-none");
    }
};

function cerrarModalDetalle() {
    if (_autoGuardarTimer) {
        clearTimeout(_autoGuardarTimer);
        _autoGuardarTimer = null;
        autoGuardar(); // flush sin await — guarda en background
    }
    mostrarAutoGuardadoStatus("");
    document.getElementById("modalDetalleMuestra").classList.remove("visible");
}

function badgeHTML(estado) {
    const e = (estado || "").toUpperCase();
    const classMap = {
        PENDIENTE:            "badge-pendiente",
        EN_PROCESO:           "badge-proceso",
        COMPLETO_SIN_INFORME: "badge-completo-sin-informe",
        DEMORADA:             "badge-demorada",
        COMPLETO:             "badge-informe",
        CANCELADO:            "badge-cancelado",
    };
    const cls = classMap[e] || "";
    const lbl = labelEstadoDetalle(e);
    return `<span class="badge-estado ${cls}"><span class="badge-dot"></span>${lbl}</span>`;
}

function labelEstadoDetalle(estado) {
    const map = {
        PENDIENTE: "Pendiente",
        EN_PROCESO: "En proceso",
        COMPLETO_SIN_INFORME: "Completo sin informe",
        DEMORADA: "Demorada",
        COMPLETO: "Completo",
        CANCELADO: "Cancelado",
    };
    return map[(estado || "").toUpperCase()] || (estado || "—");
}

function renderizarDetalleMuestra(d) {
    document.getElementById("detalleProtocolo").textContent = d.nroProtocolo || `#${d.id}`;

    const btnGenerar  = document.getElementById("btnGenerarInforme");
    const statusEl    = document.getElementById("autoGuardadoStatus");
    const esCancelado = d.estado === "CANCELADO";

    if (esCancelado) {
        btnGenerar.style.display = "none";
        if (statusEl) statusEl.style.display = "none";
    } else if (d.estado === "COMPLETO") {
        btnGenerar.style.display = "none";
        if (statusEl) statusEl.style.display = "";
    } else {
        btnGenerar.style.display = "";
        if (statusEl) statusEl.style.display = "";
        const todosCompletos = d.parametros && d.parametros.length > 0 &&
            d.parametros.every(p => p.valorResultado && p.valorResultado.trim() !== "");
        btnGenerar.disabled = !todosCompletos;
        btnGenerar.title = todosCompletos ? "" : "Faltan resultados en uno o más parámetros";
    }

    // Estado badge
    const estadoEl = document.getElementById("detalleEstado");
    estadoEl.className = "";
    estadoEl.innerHTML = badgeHTML(d.estado);

    document.getElementById("detalleCliente").textContent = d.cliente || "—";
    document.getElementById("detalleMatrizTipo").textContent = d.matrizNombre || "—";
    document.getElementById("detallePuntoMuestreo").textContent = d.puntoMuestreo || "—";
    document.getElementById("detalleTipoMuestra").textContent = d.tipoMuestraNombre || "—";
    document.getElementById("detalleFechas").textContent =
        `${formatearFecha(d.fechaIngreso)} → ${d.fechaEntrega ? formatearFecha(d.fechaEntrega) : "sin definir"}`;

    // Observaciones
    const wrapObs = document.getElementById("detalleObservacionesWrap");
    if (d.observaciones) {
        document.getElementById("detalleObservaciones").textContent = d.observaciones;
        wrapObs.style.display = "";
    } else {
        wrapObs.style.display = "none";
    }

    // Normativas (chips)
    const contResoluciones = document.getElementById("detalleResoluciones");
    contResoluciones.innerHTML = "";
    if (!d.resolucionesAplicadas || d.resolucionesAplicadas.length === 0) {
        contResoluciones.innerHTML = '<span style="color:var(--color-text-tertiary);font-size:13px">Sin normativa asociada</span>';
    } else {
        d.resolucionesAplicadas.forEach(nombre => {
            const chip = document.createElement("span");
            chip.className = "detalle-chip";
            chip.textContent = nombre;
            contResoluciones.appendChild(chip);
        });
    }

    // Parámetros
    const contParametros = document.getElementById("detalleParametros");
    contParametros.innerHTML = "";

    if (!d.parametros || d.parametros.length === 0) {
        contParametros.innerHTML = '<span style="color:var(--color-text-tertiary);font-size:13px">Sin parámetros cargados.</span>';
        return;
    }

    d.parametros.forEach(p => {
        const card = document.createElement("div");
        card.className = "param-card";

        let limitesHtml = "";
        if (!p.limites || p.limites.length === 0) {
            limitesHtml = `<div class="param-card-limites"><span style="color:var(--color-text-tertiary);font-size:12px">Sin límite normativo asociado</span></div>`;
        } else {
            const filas = p.limites.map(l => {
                const textoLimite = formatearLimite(l);
                let badgeClass, badgeText;
                if (l.cumple === null || l.cumple === undefined) {
                    badgeClass = "badge-cumple badge-cumple-nd";
                    badgeText = "Sin evaluar";
                } else if (l.cumple) {
                    badgeClass = "badge-cumple badge-cumple-si";
                    badgeText = "Cumple";
                } else {
                    badgeClass = "badge-cumple badge-cumple-no";
                    badgeText = "No cumple";
                }
                return `
                    <div class="param-limite-row">
                        <span class="param-limite-origen">${l.origenNombre}</span>
                        <span class="param-limite-valor">${textoLimite}</span>
                        <span class="${badgeClass}"
                              data-tipo="${esLimiteAusencia(l) ? 'AUSENCIA' : (l.tipoLimite || '')}"
                              data-min="${l.limiteMin ?? ''}"
                              data-max="${l.limiteMax ?? ''}"
                              data-texto="${l.limiteTexto ?? ''}"
                        >${badgeText}</span>
                    </div>`;
            }).join("");
            limitesHtml = `<div class="param-card-limites">${filas}</div>`;
        }

        const soloAusencia = p.limites && p.limites.length > 0 && p.limites.every(l => esLimiteAusencia(l));
        const val = p.valorResultado || "";
        const inputResultado = soloAusencia
            ? `<select class="param-resultado-input param-resultado-select" data-parametro-id="${p.id}" ${esCancelado ? 'disabled style="opacity:.6;"' : ''}>
                   <option value="">— Seleccionar —</option>
                   <option value="Ausente"  ${val === "Ausente"  ? "selected" : ""}>Ausente</option>
                   <option value="Presente" ${val === "Presente" ? "selected" : ""}>Presente</option>
               </select>`
            : `<input class="param-resultado-input" type="text" data-parametro-id="${p.id}"
                   value="${val}" placeholder="Resultado..."
                   ${esCancelado ? 'readonly style="opacity:.6;cursor:default"' : ''}>`;

        const hayLimites = p.limites && p.limites.length > 0;
        const nLimites   = hayLimites ? p.limites.length : 0;

        card.innerHTML = `
            <div class="param-card-header">
                <div>
                    <div class="param-card-nombre">${p.nombre} <span class="param-card-unidad">(${p.unidad || "—"})</span></div>
                    <div class="param-card-metodo">${p.metodologiaNombre || "Sin metodología"}</div>
                </div>
                <div class="param-resultado-wrap">
                    ${inputResultado}
                    <span class="param-resultado-unidad">${p.unidad || ""}</span>
                    ${hayLimites ? `<span class="param-norma-count">${nLimites} norma${nLimites !== 1 ? 's' : ''}</span>` : ''}
                </div>
            </div>
            ${hayLimites ? `<button class="param-toggle" type="button" aria-expanded="false" data-count="${nLimites}">
                <i class="bi bi-chevron-down param-toggle-icon"></i>
                <span>Ver normativas (${nLimites})</span>
            </button>` : ''}
            <div class="param-colapsable"${hayLimites ? ' style="display:none"' : ''}>
                <div class="param-obs-wrap">
                    <input
                        class="param-obs-input"
                        type="text"
                        id="obs-param-${p.id}"
                        value="${p.observacion || ''}"
                        placeholder="Observación..."
                    >
                </div>
                ${limitesHtml}
            </div>
        `;
        contParametros.appendChild(card);
    });
}

// Devuelve true si el límite representa "debe ser ausente" (tipo AUSENCIA o TEXTO con texto="Ausente")
function esLimiteAusencia(l) {
    if (l.tipoLimite === "AUSENCIA") return true;
    if (l.tipoLimite === "TEXTO" && l.limiteTexto && l.limiteTexto.trim().toLowerCase() === "ausente") return true;
    return false;
}

// Formatea un límite según su tipo (MAX, MIN, RANGO, TEXTO) para mostrarlo legible
function formatearLimite(l) {
    switch (l.tipoLimite) {
        case "MAX":
            return `≤ ${l.limiteMax}`;
        case "MIN":
            return `≥ ${l.limiteMin}`;
        case "RANGO":
            if (l.limiteMin != null && l.limiteMax != null) return `${l.limiteMin} – ${l.limiteMax}`;
            if (l.limiteTexto) return l.limiteTexto;
            return "—";
        case "TEXTO":
            return l.limiteTexto || "—";
        case "AUSENCIA":
            return "Ausente";
        default:
            return l.limiteTexto || `${l.limiteMin ?? ''} ${l.limiteMax ?? ''}`.trim() || "—";
    }
}

// Updates a badge-cumple element based on a typed result value
// Intenta parsear un texto tipo "0,01/0,05" como rango {min, max}
function parsearRangoTexto(texto) {
    if (!texto) return null;
    const parts = texto.replace(/\s/g, "").split("/");
    if (parts.length === 2) {
        const min = parseFloat(parts[0].replace(",", "."));
        const max = parseFloat(parts[1].replace(",", "."));
        if (!isNaN(min) && !isNaN(max)) return { min, max };
    }
    return null;
}

function actualizarBadge(badge, valorStr) {
    const tipo = badge.dataset.tipo;
    if (!tipo || !valorStr || !valorStr.trim()) {
        badge.className = "badge-cumple badge-cumple-nd";
        badge.textContent = "Sin evaluar";
        return;
    }

    // AUSENCIA: evaluación por string antes del parseo numérico
    if (tipo === "AUSENCIA") {
        const v = valorStr.trim().toLowerCase();
        let cumple = null;
        if (v === "ausente") cumple = true;
        else if (v === "presente") cumple = false;
        else { const n = parseFloat(valorStr.replace(",", ".")); if (!isNaN(n)) cumple = false; }
        badge.className = cumple === true  ? "badge-cumple badge-cumple-si"
                        : cumple === false ? "badge-cumple badge-cumple-no"
                        :                   "badge-cumple badge-cumple-nd";
        badge.textContent = cumple === true ? "Cumple" : cumple === false ? "No cumple" : "Sin evaluar";
        return;
    }

    // TEXTO: solo evalúa si el texto tiene formato de rango "X/Y"
    if (tipo === "TEXTO") {
        const rango = parsearRangoTexto(badge.dataset.texto);
        if (rango) {
            const valor = parseFloat(valorStr.replace(",", ".").trim());
            const cumple = isNaN(valor) ? null : (valor >= rango.min && valor <= rango.max);
            aplicarCumpleBadge(badge, cumple);
        } else {
            badge.className = "badge-cumple badge-cumple-nd";
            badge.textContent = "Sin evaluar";
        }
        return;
    }

    const valor = parseFloat(valorStr.replace(",", ".").trim());
    if (isNaN(valor)) {
        badge.className = "badge-cumple badge-cumple-nd";
        badge.textContent = "Sin evaluar";
        return;
    }
    let min = parseFloat(badge.dataset.min);
    let max = parseFloat(badge.dataset.max);
    // Si min/max no están como números, intenta parsear el limiteTexto como "X/Y"
    if ((isNaN(min) || isNaN(max)) && (tipo === "RANGO" || tipo === "MAX" || tipo === "MIN")) {
        const rango = parsearRangoTexto(badge.dataset.texto);
        if (rango) { min = rango.min; max = rango.max; }
    }
    let cumple;
    switch (tipo) {
        case "MAX":   cumple = isNaN(max) ? null : valor <= max; break;
        case "MIN":   cumple = isNaN(min) ? null : valor >= min; break;
        case "RANGO": cumple = (isNaN(min) || isNaN(max)) ? null : (valor >= min && valor <= max); break;
        default:      cumple = null;
    }
    aplicarCumpleBadge(badge, cumple);
}

function aplicarCumpleBadge(badge, cumple) {
    if (cumple === true) {
        badge.className = "badge-cumple badge-cumple-si";
        badge.textContent = "Cumple";
    } else if (cumple === false) {
        badge.className = "badge-cumple badge-cumple-no";
        badge.textContent = "No cumple";
    } else {
        badge.className = "badge-cumple badge-cumple-nd";
        badge.textContent = "Sin evaluar";
    }
}

function dispararAutoGuardar() {
    if (_autoGuardarTimer) clearTimeout(_autoGuardarTimer);
    mostrarAutoGuardadoStatus("pending");
    _autoGuardarTimer = setTimeout(autoGuardar, 1500);
}

async function autoGuardar() {
    _autoGuardarTimer = null;
    if (!detalleAnalisisId) return;
    mostrarAutoGuardadoStatus("saving");
    try {
        const inputs = document.querySelectorAll(".param-resultado-input");
        const resultados = [];
        inputs.forEach(input => {
            const parametroId = parseInt(input.dataset.parametroId);
            const obsInput = document.getElementById(`obs-param-${parametroId}`);
            resultados.push({
                parametroId,
                valorResultado: input.value.trim() || null,
                observacion: obsInput ? (obsInput.value.trim() || null) : null,
            });
        });
        const resp = await fetchConAuth(`${API_URL}/estudios/${detalleAnalisisId}/resultados`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(resultados),
        });
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        mostrarAutoGuardadoStatus("saved");
    } catch (err) {
        console.error("Error auto-guardando:", err);
        mostrarAutoGuardadoStatus("error");
    }
}

function mostrarAutoGuardadoStatus(estado) {
    const el = document.getElementById("autoGuardadoStatus");
    if (!el) return;
    el.className = "autosave-status";
    if (estado === "pending") {
        el.textContent = "";
    } else if (estado === "saving") {
        el.textContent = "Guardando...";
        el.classList.add("autosave-saving");
    } else if (estado === "saved") {
        el.textContent = "✓ Guardado";
        el.classList.add("autosave-saved");
        setTimeout(() => { if (el.classList.contains("autosave-saved")) el.textContent = ""; }, 3000);
    } else if (estado === "error") {
        el.textContent = "Error al guardar";
        el.classList.add("autosave-error");
    } else {
        el.textContent = "";
    }
}

async function onGenerarInforme() {
    if (!detalleAnalisisId) return;

    const btn = document.getElementById("btnGenerarInforme");
    const textoOriginal = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Generando...`;

    try {
        const resp = await fetchConAuth(`${API_URL}/estudios/${detalleAnalisisId}/generar-informe`, {
            method: "POST"
        });

        if (!resp.ok) {
            const err = await resp.json().catch(() => ({}));
            throw new Error(err.message || `Error HTTP ${resp.status}`);
        }

        // Descarga el PDF directamente en el navegador
        const blob = await resp.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `informe-${detalleAnalisisId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        mostrarToast("Informe generado y descargado correctamente.");
        cerrarModalDetalle();
        await cargarMuestrasActivas();

    } catch (err) {
        console.error("Error generando informe:", err);
        mostrarToast(`Error al generar el informe: ${err.message}`, true);
    } finally {
        btn.disabled = false;
        btn.innerHTML = textoOriginal;
    }
}

// Recalcula si el botón "Generar informe" debe estar habilitado
// según si todos los inputs de resultado tienen valor
function recalcularEstadoBtnGenerarInforme() {
    const btnGenerar = document.getElementById("btnGenerarInforme");
    if (!btnGenerar || btnGenerar.style.display === "none") return;
    const inputs = document.querySelectorAll(".param-resultado-input");
    const todosCompletos = inputs.length > 0 &&
        Array.from(inputs).every(inp => inp.value && inp.value.trim() !== "");
    btnGenerar.disabled = !todosCompletos;
    btnGenerar.title = todosCompletos ? "" : "Faltan resultados en uno o más parámetros";
}

// Genera el informe directamente desde la fila de la tabla (sin abrir el modal)
window.onGenerarInformeDesdeTabla = async function(id) {
    detalleAnalisisId = id;
    await onGenerarInforme();
};

// ============================================================
// ARCHIVOS DE MUESTRA (múltiples PDFs)
// ============================================================
let altaInformeAnalisisId = null;

window.abrirAltaInforme = async function(id, protocolo) {
    altaInformeAnalisisId = id;
    document.getElementById("altaInformeTitulo").textContent = `Archivos — ${protocolo}`;
    document.getElementById("inputAltaInformePdf").value = "";
    document.getElementById("altaInformeError").style.display = "none";
    document.getElementById("modalAltaInforme").classList.add("visible");
    await cargarListaArchivos(id);
};

function cerrarAltaInforme() {
    document.getElementById("modalAltaInforme").classList.remove("visible");
    altaInformeAnalisisId = null;
}

async function cargarListaArchivos(analisisId) {
    const contenedor = document.getElementById("listaArchivos");
    const vacia = document.getElementById("listaArchivosVacia");
    contenedor.innerHTML = `<span class="text-muted" style="font-size:13px;">Cargando...</span>`;

    try {
        const resp = await fetchConAuth(`${API_URL}/estudios/${analisisId}/archivos`);
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        const archivos = await resp.json();

        contenedor.innerHTML = "";
        if (archivos.length === 0) {
            contenedor.innerHTML = `<span class="text-muted" style="font-size:13px;">Sin archivos todavía.</span>`;
            return;
        }

        archivos.forEach(a => {
            const fila = document.createElement("div");
            fila.style.cssText = "display:flex; align-items:center; gap:8px; padding:6px 10px; border-radius:6px; background:var(--bg-card, #f8f9fa); border:1px solid var(--border-color, #dee2e6);";
            fila.innerHTML = `
                <i class="bi bi-file-earmark-pdf-fill" style="color:#dc3545; font-size:15px;"></i>
                <span style="flex:1; font-size:13px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;"
                      title="${a.nombre || ''}">${a.nombre || 'Archivo'}</span>
                <span style="font-size:11px; color:#888;">${formatearFecha(a.createdAt)}</span>
                <button class="btn-accion" title="Descargar"
                        onclick="descargarArchivoModal(${analisisId}, ${a.id}, '${(a.nombre || 'archivo').replace(/'/g, '')}')">
                    <i class="bi bi-download"></i>
                </button>
                <button class="btn-accion btn-accion-rojo" title="Eliminar"
                        onclick="eliminarArchivoModal(${analisisId}, ${a.id}, this)">
                    <i class="bi bi-trash3"></i>
                </button>
            `;
            contenedor.appendChild(fila);
        });
    } catch (err) {
        console.error("Error cargando archivos:", err);
        contenedor.innerHTML = `<span style="color:#dc3545; font-size:13px;">Error al cargar los archivos.</span>`;
    }
}

window.descargarArchivoModal = function(analisisId, archivoId, nombre) {
    const token = localStorage.getItem("token");
    fetch(`${API_URL}/estudios/${analisisId}/archivos/${archivoId}`, {
        headers: { Authorization: `Bearer ${token}` }
    })
    .then(r => r.blob())
    .then(blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = nombre;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    })
    .catch(err => mostrarToast("Error al descargar el archivo.", true));
};

window.eliminarArchivoModal = async function(analisisId, archivoId, btn) {
    if (!confirm("¿Eliminar este archivo? Esta acción no se puede deshacer.")) return;
    btn.disabled = true;
    try {
        const resp = await fetchConAuth(`${API_URL}/estudios/${analisisId}/archivos/${archivoId}`, {
            method: "DELETE"
        });
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        mostrarToast("Archivo eliminado.");
        await cargarListaArchivos(analisisId);
        await cargarMuestrasActivas();
    } catch (err) {
        console.error("Error eliminando archivo:", err);
        mostrarToast("Error al eliminar el archivo.", true);
        btn.disabled = false;
    }
};

// ── Cancelar muestra ──────────────────────────────────────
let _cancelarMuestraId = null;

window.abrirModalCancelar = function(id, codigo) {
    _cancelarMuestraId = id;
    document.getElementById("modalCancelarMsg").textContent =
        `¿Cancelar la muestra ${codigo}? Esta acción no se puede revertir.`;
    document.getElementById("inputMotivoCancelacionM").value = "";
    document.getElementById("modalCancelarMuestra").classList.add("visible");
    document.getElementById("inputMotivoCancelacionM").focus();
};

function cerrarModalCancelar() {
    document.getElementById("modalCancelarMuestra").classList.remove("visible");
    _cancelarMuestraId = null;
}

async function confirmarCancelarMuestra() {
    if (!_cancelarMuestraId) return;
    const motivo = document.getElementById("inputMotivoCancelacionM").value.trim();
    const btn = document.getElementById("btnConfirmarCancelar");
    btn.disabled = true;
    try {
        const resp = await fetchConAuth(`${API_URL}/estudios/${_cancelarMuestraId}/cancelar`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ motivo: motivo || null }),
        });
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
        mostrarToast("Muestra cancelada.");
        cerrarModalCancelar();
        await cargarMuestrasActivas();
    } catch (err) {
        console.error("Error cancelando muestra:", err);
        mostrarToast("No se pudo cancelar la muestra.", true);
    } finally {
        btn.disabled = false;
    }
}

async function onUploadAltaInforme() {
    const fileInput = document.getElementById("inputAltaInformePdf");
    const errEl = document.getElementById("altaInformeError");
    const file = fileInput.files[0];

    if (!file) {
        errEl.textContent = "Seleccioná un archivo PDF.";
        errEl.style.display = "block";
        return;
    }
    if (file.type && file.type !== "application/pdf") {
        errEl.textContent = "El archivo debe ser un PDF.";
        errEl.style.display = "block";
        return;
    }

    errEl.style.display = "none";
    const btn = document.getElementById("btnUploadAltaInforme");
    const textoOriginal = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Subiendo...`;

    try {
        const formData = new FormData();
        formData.append("file", file);

        const resp = await fetchConAuth(`${API_URL}/estudios/${altaInformeAnalisisId}/documento`, {
            method: "POST",
            body: formData
        });
        if (!resp.ok) throw new Error(`HTTP ${resp.status}`);

        fileInput.value = "";
        mostrarToast("Archivo subido correctamente.");
        await cargarListaArchivos(altaInformeAnalisisId);
        await cargarMuestrasActivas();
    } catch (err) {
        console.error("Error al subir archivo:", err);
        errEl.textContent = "Error al subir el archivo. Intentá nuevamente.";
        errEl.style.display = "block";
    } finally {
        btn.disabled = false;
        btn.innerHTML = textoOriginal;
    }
}

function init() {
    inicializarHeader();
    cargarClientes();
    cargarMatrices();
    cargarMuestrasActivas();
    vincularEventos();
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}