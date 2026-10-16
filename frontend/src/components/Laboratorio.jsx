import { useEffect, useState } from 'react';
import { api } from '../api';
import ResultadoLaboratorio from './ResultadoLaboratorio';

/** Solicitar exámenes y ver resultados con indicador dentro/fuera de rango. */
export default function Laboratorio({ historia, onCambio }) {
  const [catalogo, setCatalogo] = useState({});
  const [elegidos, setElegidos] = useState([]);
  const [mensaje, setMensaje] = useState(null);
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    api.examenesDisponibles().then(setCatalogo).catch((e) => setMensaje({ tipo: 'error', texto: e.message }));
  }, []);

  const alternar = (codigo) =>
    setElegidos((l) => (l.includes(codigo) ? l.filter((c) => c !== codigo) : [...l, codigo]));

  async function solicitar() {
    setCargando(true);
    setMensaje(null);
    try {
      const nuevos = await api.solicitarExamenes(historia.paciente.id, elegidos);
      setMensaje({ tipo: 'exito', texto: `${nuevos.length} examen(es) procesado(s). Los resultados ya están abajo.` });
      setElegidos([]);
      onCambio();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: e.message });
    } finally {
      setCargando(false);
    }
  }

  return (
    <>
      <header className="encabezado">
        <h2>Laboratorio</h2>
        <p>Solicite exámenes y revise sus resultados frente a los valores de referencia.</p>
      </header>

      <section className="tarjeta pila" style={{ marginBottom: 22 }}>
        <span className="paso">Solicitar exámenes</span>
        <div className="opciones">
          {Object.entries(catalogo).map(([codigo, nombre]) => (
            <button key={codigo} className={`opcion ${elegidos.includes(codigo) ? 'elegida' : ''}`} onClick={() => alternar(codigo)}>
              {elegidos.includes(codigo) ? '✓ ' : ''}{nombre}
            </button>
          ))}
        </div>
        {mensaje && <div className={`mensaje ${mensaje.tipo}`}>{mensaje.texto}</div>}
        <div>
          <button className="boton" disabled={!elegidos.length || cargando} onClick={solicitar}>
            {cargando ? 'Procesando…' : 'Solicitar'}
          </button>
        </div>
      </section>

      <div className="fila" style={{ marginBottom: 12 }}>
        <span className="etiqueta ok">✓ Dentro del rango normal</span>
        <span className="etiqueta alerta">▲ Fuera del rango normal</span>
      </div>
      <div className="rejilla">
        {historia.laboratorios.length === 0 && <p className="vacio">Aún no tiene resultados.</p>}
        {historia.laboratorios.map((r) => <ResultadoLaboratorio key={r.id} resultado={r} />)}
      </div>
    </>
  );
}
