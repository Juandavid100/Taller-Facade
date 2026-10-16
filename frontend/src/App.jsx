import { useCallback, useEffect, useState } from 'react';
import { api } from './api';
import Acceso from './components/Acceso';
import AgendarCita from './components/AgendarCita';
import Dashboard from './components/Dashboard';
import HistoriaClinica from './components/HistoriaClinica';
import Laboratorio from './components/Laboratorio';

const VISTAS = [
  { id: 'inicio', texto: 'Inicio' },
  { id: 'agendar', texto: 'Agendar cita' },
  { id: 'historia', texto: 'Historia clínica' },
  { id: 'laboratorio', texto: 'Laboratorio' },
];

const CLAVE_SESION = 'clinica-paciente';

function leerSesion() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_SESION));
  } catch {
    return null;
  }
}

function guardarSesion(paciente) {
  try {
    if (paciente) localStorage.setItem(CLAVE_SESION, JSON.stringify(paciente));
    else localStorage.removeItem(CLAVE_SESION);
  } catch {
    /* el navegador no permite guardar; la sesión dura mientras la pestaña esté abierta */
  }
}

export default function App() {
  const [paciente, setPaciente] = useState(leerSesion);
  const [vista, setVista] = useState('inicio');
  const [historia, setHistoria] = useState(null);
  const [error, setError] = useState('');

  const salir = useCallback(() => {
    guardarSesion(null);
    setPaciente(null);
    setHistoria(null);
    setVista('inicio');
  }, []);

  // Toda la información del paciente llega en un solo objeto, armado por la ClinicaFacade.
  const cargarHistoria = useCallback(async () => {
    if (!paciente) return;
    try {
      setHistoria(await api.historia(paciente.id));
      setError('');
    } catch (e) {
      // Los datos viven en memoria: si el backend se reinició, el paciente ya no existe.
      if (e.status === 404) salir();
      else setError(e.message);
    }
  }, [paciente, salir]);

  useEffect(() => { cargarHistoria(); }, [cargarHistoria]);

  function ingresar(p) {
    guardarSesion(p);
    setPaciente(p);
  }

  if (!paciente) return <Acceso onIngreso={ingresar} />;

  return (
    <div className="app">
      <aside className="lateral">
        <div className="marca">
          <div className="marca-icono">+</div>
          <h1>Clínica Vital <small>Portal del paciente</small></h1>
        </div>
        {VISTAS.map((v) => (
          <button key={v.id} className={`nav-boton ${vista === v.id ? 'activo' : ''}`} onClick={() => setVista(v.id)}>
            {v.texto}
          </button>
        ))}
        <div className="lateral-pie">
          <div><strong>{paciente.nombres} {paciente.apellidos}</strong>CC {paciente.documento}</div>
          <button onClick={salir}>Cerrar sesión</button>
        </div>
      </aside>

      <main className="contenido">
        {error && <div className="mensaje error" style={{ marginBottom: 16 }}>{error} <button className="boton secundario" onClick={cargarHistoria}>Reintentar</button></div>}
        {!historia && !error && <p className="vacio">Cargando su información…</p>}
        {historia && vista === 'inicio' && <Dashboard historia={historia} onCambio={cargarHistoria} onIr={setVista} />}
        {historia && vista === 'agendar' && <AgendarCita pacienteId={paciente.id} onAgendada={cargarHistoria} />}
        {historia && vista === 'historia' && <HistoriaClinica historia={historia} onCambio={cargarHistoria} />}
        {historia && vista === 'laboratorio' && <Laboratorio historia={historia} onCambio={cargarHistoria} />}
      </main>
    </div>
  );
}
