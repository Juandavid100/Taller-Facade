// Cliente HTTP del portal. Todas las llamadas van a los endpoints de la ClinicaFacade.
const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_URL}/api/clinica${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    throw new Error('No se pudo conectar con el servidor. Si está en Railway, espere unos segundos e intente de nuevo.');
  }
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const error = new Error(data?.error || `Error ${response.status}`);
    error.status = response.status;
    throw error;
  }
  return data;
}

const post = (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) });

export const api = {
  login: (documento, password) => post('/login', { documento, password }),
  registrar: (datos) => post('/paciente', datos),
  perfil: (pacienteId) => request(`/paciente/${pacienteId}`),
  especialidades: () => request('/especialidades'),
  medicos: (especialidad, fecha) =>
    request(`/medicos?especialidad=${encodeURIComponent(especialidad)}${fecha ? `&fecha=${fecha}` : ''}`),
  agendarCita: (cita) => post('/cita', cita),
  cancelarCita: (citaId, pacienteId) => request(`/cita/${citaId}?pacienteId=${pacienteId}`, { method: 'DELETE' }),
  historia: (pacienteId) => request(`/historia/${pacienteId}`),
  prescripcion: (pacienteId, medicamentos, indicaciones) =>
    post('/prescripcion', { pacienteId, medicamentos, indicaciones }),
  examenesDisponibles: () => request('/laboratorio/examenes'),
  solicitarExamenes: (pacienteId, examenes) => post('/laboratorio', { pacienteId, examenes }),
};
