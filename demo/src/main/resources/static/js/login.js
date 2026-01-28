const form = document.getElementById('loginForm');
const btnLogin = document.getElementById('btnLogin');
const inputs = form.querySelectorAll('input');


// Validar en tiempo real
inputs.forEach(input => {
    input.addEventListener('input', validateForm);
    input.addEventListener('blur', validateField);
});

function validateField(e) {
    const field = e.target;
    const fieldId = field.id;
    const value = field.value.trim();

    // Limpiar error del campo
    document.getElementById('error' + capitalize(fieldId)).textContent = '';
    field.classList.remove('input-error');

    switch(fieldId) {
        case 'correo':
            if (value === '') {
                showError(fieldId, 'El correo es obligatorio');
            } else if (!value.includes('@')) {
                showError(fieldId, 'El correo debe contener @');
            }
            break;
        case 'password':
            if (value === '') {
                showError(fieldId, 'La contraseña es obligatoria');
            } else if (value.length < 6) {
                showError(fieldId, 'La contraseña debe tener al menos 6 caracteres');
            }
            break;
    }
}

function validateForm() {
    const correo = document.getElementById('correo').value.trim();
    const password = document.getElementById('password').value;

    const isCorreoValid = correo.includes('@');
    const isPasswordValid = password.length >= 6;

    const allValid = isCorreoValid && isPasswordValid;

    btnLogin.disabled = !allValid;
}

function showError(fieldId, message) {
    document.getElementById('error' + capitalize(fieldId)).textContent = message;
    document.getElementById(fieldId).classList.add('input-error');
}

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}