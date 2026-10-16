import { useEffect, useState } from 'react';
import { api } from '../api';
import { formatoDia, proximosDias } from '../utils';

/** Agendar cita: especialidad → día → médico y horario disponible. */
export default function AgendarCita({ pacienteId, onAgendada }) {
  const dias = proximosDias(12);
  const [especialidades, setEspecialidades] = useState([]);
  const [especialidad, setEspecialidad] = useState('');
  const [dia, setDia] = useState(dias[0]);
  const [medicos, setMedicos] = useState([]);
  const [eleccion, setEleccion] = useState(null); // { medicoId, hora }
  const [mensaje, setMensaje] = useState(null);
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    api.especialidades().then((lista) => {
      setEspecialidades(lista);
      setEspecialidad(lista[0] || '');
    }).catch((e) => setMensaje({ tipo: 'error', texto: e.message }));
  }, []);

  useEffect(() => {
    if (!especialidad) return;
    setEleccion(null);
    api.medicos(especialidad, dia).then(setMedicos).catch((e) => setMensaje({ tipo: 'error', texto: e.message }));
  }, [especialidad, dia]);

  async function confirmar() {
    setCargando(true);
    setMensaje(null);
    try {
      const cita = await api.agendarCita({
        pacienteId,
        especialidad,
        medicoId: eleccion.medicoId,
        fecha: `${dia}T${eleccion.hora}`,
      });
      setMensaje({ tipo: 'exito', texto: cita.recordatorio });
      setEleccion(null);
      setMedicos(await api.medicos(especialidad, dia));
      onAgendada();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: e.message });
    } finally {
      setCargando(false);
    }
  }

  return (
    <>
      <header className="encabezado">
        <h2>Agendar cita</h2>
        <p>Elija la especialidad, el día y el horario con el médico de su preferencia.</p>
      </header>

      <div className="pila">
        <section className="tarjeta pila">
          <span className="paso">Paso 1 · Especialidad</span>
          <div className="opciones">
            {especialidades.map((e) => (
              <button key={e} className={`opcion ${e === especialidad ? 'elegida' : ''}`} onClick={() => setEspecialidad(e)}>{e}</button>
            ))}
          </div>

          <span className="paso">Paso 2 · Día</span>
          <div className="opciones">
            {dias.map((d) => (
              <button key={d} className={`opcion ${d === dia ? 'elegida' : ''}`} onClick={() => setDia(d)}>
                {new Date(`${d}T00:00`).toLocaleDateString('es-CO', { weekday: 'short', day: 'numeric', month: 'short' })}
              </button>
            ))}
          </div>
        </section>

        <section className="tarjeta pila">
          <span className="paso">Paso 3 · Médico y horario</span>
          <div className="rejilla">
            {medicos.map((m) => (
              <div key={m.id} className={`medico ${eleccion?.medicoId === m.id ? 'elegido' : ''}`}>
                <strong>{m.nombre}</strong>
                <div><small>{m.especialidad}</small></div>
                <div className="opciones" style={{ marginTop: 10 }}>
                  {m.horariosDisponibles.length === 0 && <small>Sin horarios este día</small>}
                  {m.horariosDisponibles.map((h) => (
                    <button key={h}
                      className={`opcion ${eleccion?.medicoId === m.id && eleccion.hora === h ? 'elegida' : ''}`}
                      onClick={() => setEleccion({ medicoId: m.id, hora: h })}>{h}</button>
                  ))}
                </div>
              </div>
            ))}
          </div>

          {mensaje && <div className={`mensaje ${mensaje.tipo}`}>{mensaje.texto}</div>}

          <button className="boton" disabled={!eleccion || cargando} onClick={confirmar}>
            {eleccion
              ? `Confirmar cita · ${formatoDia(`${dia}T${eleccion.hora}`)} a las ${eleccion.hora}`
              : 'Seleccione un horario'}
          </button>
        </section>
      </div>
    </>
  );
}
