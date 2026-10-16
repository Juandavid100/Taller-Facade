import { useState } from 'react';
import { api } from '../api';

const FILA = { nombre: '', dosis: '', duracion: '' };

/** Formulario de prescripción. El backend valida las alergias antes de generarla. */
export default function NuevaPrescripcion({ pacienteId, onCreada }) {
  const [medicamentos, setMedicamentos] = useState([{ ...FILA }]);
  const [indicaciones, setIndicaciones] = useState('');
  const [mensaje, setMensaje] = useState(null);

  const editar = (i, campo, valor) =>
    setMedicamentos((lista) => lista.map((m, j) => (j === i ? { ...m, [campo]: valor } : m)));

  async function enviar(e) {
    e.preventDefault();
    setMensaje(null);
    try {
      await api.prescripcion(pacienteId, medicamentos, indicaciones);
      setMensaje({ tipo: 'exito', texto: 'Prescripción generada correctamente.' });
      setMedicamentos([{ ...FILA }]);
      setIndicaciones('');
      onCreada();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  }

  return (
    <form className="tarjeta pila" onSubmit={enviar}>
      <h3 style={{ marginBottom: 0 }}>Nueva prescripción</h3>
      {medicamentos.map((m, i) => (
        <div className="campos-3" key={i}>
          <label className="campo">Medicamento
            <input value={m.nombre} onChange={(e) => editar(i, 'nombre', e.target.value)} placeholder="Ej: Loratadina 10 mg" required />
          </label>
          <label className="campo">Dosis
            <input value={m.dosis} onChange={(e) => editar(i, 'dosis', e.target.value)} placeholder="1 tableta cada 24 h" required />
          </label>
          <label className="campo">Duración
            <input value={m.duracion} onChange={(e) => editar(i, 'duracion', e.target.value)} placeholder="7 días" required />
          </label>
        </div>
      ))}
      <div className="fila">
        <button type="button" className="boton secundario" onClick={() => setMedicamentos([...medicamentos, { ...FILA }])}>+ Medicamento</button>
        {medicamentos.length > 1 && (
          <button type="button" className="boton peligro" onClick={() => setMedicamentos(medicamentos.slice(0, -1))}>Quitar último</button>
        )}
      </div>
      <label className="campo">Indicaciones
        <textarea rows={2} value={indicaciones} onChange={(e) => setIndicaciones(e.target.value)} />
      </label>
      {mensaje && <div className={`mensaje ${mensaje.tipo}`}>{mensaje.texto}</div>}
      <button className="boton">Generar prescripción</button>
    </form>
  );
}
