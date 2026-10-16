import { useState } from 'react';
import { formatoFecha, formatoHora } from '../utils';
import ListaCitas from './ListaCitas';
import NuevaPrescripcion from './NuevaPrescripcion';
import ResultadoLaboratorio from './ResultadoLaboratorio';

const PESTANAS = [
  { id: 'consultas', texto: 'Consultas' },
  { id: 'prescripciones', texto: 'Prescripciones' },
  { id: 'laboratorios', texto: 'Laboratorios' },
];

/** Historia clínica completa con pestañas: consultas, prescripciones, laboratorios. */
export default function HistoriaClinica({ historia, onCambio }) {
  const [pestana, setPestana] = useState('consultas');
  const { paciente, alergias, consultas, citasPasadas, prescripciones, laboratorios } = historia;

  return (
    <>
      <header className="encabezado">
        <h2>Historia clínica</h2>
        <p>{paciente.nombres} {paciente.apellidos} · Documento {paciente.documento}</p>
      </header>

      <div className="fila" style={{ marginBottom: 16 }}>
        <span className="vacio" style={{ padding: 0 }}>Alergias:</span>
        {alergias.length === 0
          ? <span className="etiqueta gris">Ninguna registrada</span>
          : alergias.map((a) => <span key={a} className="etiqueta alerta">{a}</span>)}
      </div>

      <nav className="pestanas">
        {PESTANAS.map((p) => (
          <button key={p.id} className={`pestana ${pestana === p.id ? 'activa' : ''}`} onClick={() => setPestana(p.id)}>
            {p.texto}
          </button>
        ))}
      </nav>

      {pestana === 'consultas' && (
        <div className="rejilla">
          <section className="tarjeta">
            <h3>Consultas y diagnósticos</h3>
            {consultas.length === 0 && <p className="vacio">Sin consultas registradas.</p>}
            {consultas.map((c) => (
              <div className="registro" key={c.id}>
                <small className="vacio" style={{ padding: 0 }}>{formatoFecha(c.fecha)} · {formatoHora(c.fecha)}</small>
                <h4>{c.diagnostico}</h4>
                <p>Motivo: {c.motivo}</p>
                <p>{c.medico} · {c.especialidad}</p>
              </div>
            ))}
          </section>
          <section className="tarjeta">
            <h3>Citas anteriores</h3>
            <ListaCitas citas={citasPasadas} pacienteId={paciente.id} onCambio={onCambio}
              vacio="Sin citas anteriores." permitirCancelar={false} />
          </section>
        </div>
      )}

      {pestana === 'prescripciones' && (
        <div className="rejilla">
          <section className="tarjeta">
            <h3>Prescripciones</h3>
            {prescripciones.length === 0 && <p className="vacio">Sin prescripciones.</p>}
            {prescripciones.map((p) => (
              <div className="registro" key={p.id}>
                <small className="vacio" style={{ padding: 0 }}>{formatoFecha(p.fecha)}</small>
                {p.medicamentos.map((m, i) => (
                  <div key={i} style={{ margin: '6px 0' }}>
                    <h4>{m.nombre}</h4>
                    <p>{m.dosis} · durante {m.duracion}</p>
                  </div>
                ))}
                <p><i>{p.indicaciones}</i></p>
              </div>
            ))}
          </section>
          <NuevaPrescripcion pacienteId={paciente.id} onCreada={onCambio} />
        </div>
      )}

      {pestana === 'laboratorios' && (
        <div className="rejilla">
          {laboratorios.length === 0 && <p className="vacio">Sin resultados de laboratorio.</p>}
          {laboratorios.map((r) => <ResultadoLaboratorio key={r.id} resultado={r} />)}
        </div>
      )}
    </>
  );
}
