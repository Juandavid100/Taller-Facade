import { useState } from 'react';
import { api } from '../api';
import { formatoDia, formatoHora } from '../utils';

/** Lista de citas con opción de cancelar las que aún no han pasado. */
export default function ListaCitas({ citas, pacienteId, onCambio, vacio, permitirCancelar = true }) {
  const [error, setError] = useState('');

  async function cancelar(cita) {
    if (!window.confirm(`¿Cancelar la cita de ${cita.especialidad} del ${formatoDia(cita.fecha)}?`)) return;
    try {
      await api.cancelarCita(cita.id, pacienteId);
      onCambio();
    } catch (err) {
      setError(err.message);
    }
  }

  if (!citas.length) return <p className="vacio">{vacio}</p>;

  return (
    <div>
      {error && <div className="mensaje error">{error}</div>}
      {citas.map((cita) => {
        const fecha = new Date(cita.fecha);
        const pasada = fecha < new Date();
        return (
          <div className="cita" key={cita.id}>
            <div className="cita-fecha">
              <strong>{fecha.getDate()}</strong>
              <span>{fecha.toLocaleDateString('es-CO', { month: 'short' })}</span>
            </div>
            <div>
              <strong>{cita.especialidad}</strong> · {cita.medicoNombre}
              <div><small>{formatoDia(cita.fecha)} · {formatoHora(cita.fecha)}</small></div>
              {cita.recordatorio && !pasada && <div className="recordatorio">🔔 {cita.recordatorio}</div>}
            </div>
            <div>
              {cita.estado === 'CANCELADA' ? (
                <span className="etiqueta gris">Cancelada</span>
              ) : pasada ? (
                <span className="etiqueta ok">Atendida</span>
              ) : permitirCancelar ? (
                <button className="boton peligro" onClick={() => cancelar(cita)}>Cancelar</button>
              ) : (
                <span className="etiqueta">Programada</span>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
