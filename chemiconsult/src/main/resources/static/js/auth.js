(function () {
    const token = localStorage.getItem('token');

    if (!token) {
        window.location.replace('login.html');
        return;
    }

    const role   = (localStorage.getItem('userRole') || '').toUpperCase();
    const script = document.currentScript;

    // Chequeo de rol
    const rolesAttr = script && script.getAttribute('data-roles');
    if (rolesAttr) {
        const allowed = rolesAttr.split(',').map(r => r.trim().toUpperCase());
        if (!allowed.includes(role)) {
            window.location.replace('unauthorized.html');
            return;
        }
    }

    // Chequeo de módulo (IT siempre pasa; múltiples módulos separados por coma = OR)
    const moduloAttr = script && script.getAttribute('data-modulo');
    if (moduloAttr && role !== 'ROLE_IT') {
        const userModulos  = JSON.parse(localStorage.getItem('userModulos') || '[]');
        const requeridos   = moduloAttr.split(',').map(m => m.trim().toUpperCase());
        if (!requeridos.some(m => userModulos.includes(m))) {
            window.location.replace('unauthorized.html');
        }
    }
})();
