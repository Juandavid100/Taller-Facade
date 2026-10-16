import ListaCitas from './ListaCitas';
import ResultadoLaboratorio from './ResultadoLaboratorio';

/** Panel principal: próximas citas y últimos resultados. */
export default function Dashboard({ historia, onCambio, onIr }) {
  const { paciente, proximasCitas, laboratorios, prescripciones, alergias } = historia;
  const fueraDeRango = laboratorios.flatMap((l) => l.parametros).filter((p) => !p.dentroDeRango).length;

  return (
    <>
      <header className="encabezado">
        <h2>Hola, {paciente.nombres.split(' ')[0]}</h2>
        <p>Este es el resumen de su salud y sus próximas citas.</p>
      </header>

      <div className="resumen">
        <div className="dato"><strong>{proximasCitas.length}</strong><span>Próximas citas</span></div>
        <div className="dato"><strong>{laboratorios.length}</strong><span>Exámenes de laboratorio</span></div>
        <div className={`dato ${fueraDeRango ? 'alerta' : ''}`}><strong>{fueraDeRango}</strong><span>Valores fuera de rango</span></div>
        <div className="dato"><strong>{prescripciones.length}</strong><span>Prescripciones</span></div>
      </div>

      {alergias.length > 0 && (
        <div className="mensaje error" style={{ marginBottom: 18 }}>
          <b>Alergias registradas:</b> {alergias.join(', ')}
        </div>
      )}

      <div className="rejilla">
        <section className="tarjeta">
          <div className="fila" style={{ justifyContent: 'space-between' }}>
            <h3>Próximas citas</h3>
            <button className="boton secundario" onClick={() => onIr('agendar')}>+ Agendar</button>
          </div>
          <ListaCitas citas={proximasCitas} pacienteId={paciente.id} onCambio={onCambio}
            vacio="No tiene citas programadas." />
        </section>

        <section className="pila">
          <div className="fila" style={{ justifyContent: 'space-between' }}>
            <h3>Últimos resultados</h3>
            <button className="boton secundario" onClick={() => onIr('laboratorio')}>Ver laboratorio</button>
          </div>
          {laboratorios.length === 0 && <p className="vacio">Aún no tiene resultados de laboratorio.</p>}
          {laboratorios.slice(0, 2).map((r) => <ResultadoLaboratorio key={r.id} resultado={r} />)}
        </section>
      </div>
    </>
  );
}
