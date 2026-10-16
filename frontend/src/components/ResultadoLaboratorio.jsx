import { formatoFecha } from '../utils';

/**
 * Tarjeta de un examen con indicador visual: verde si el valor está dentro
 * del rango normal y rojo si está por fuera.
 */
export default function ResultadoLaboratorio({ resultado }) {
  const fuera = resultado.parametros.filter((p) => !p.dentroDeRango).length;

  return (
    <article className="tarjeta">
      <div className="fila" style={{ justifyContent: 'space-between', marginBottom: 8 }}>
        <div>
          <h3 style={{ marginBottom: 2 }}>{resultado.nombreExamen}</h3>
          <small className="vacio" style={{ padding: 0 }}>{formatoFecha(resultado.fecha)}</small>
        </div>
        <span className={`etiqueta ${fuera ? 'alerta' : 'ok'}`}>
          {fuera ? `${fuera} fuera de rango` : 'Todo normal'}
        </span>
      </div>

      {resultado.parametros.map((p) => {
        // Escala de la barra: un poco más ancha que el rango normal para ver valores por fuera.
        const max = Math.max(p.maximo * 1.35, p.valor * 1.05);
        const pos = (v) => `${Math.min(100, Math.max(0, (v / max) * 100))}%`;
        const estado = p.dentroDeRango ? 'dentro' : 'fuera';
        return (
          <div className="parametro" key={p.nombre}>
            <span>{p.nombre}</span>
            <span className={`parametro-valor ${estado}`}>
              {p.valor} <small style={{ fontWeight: 400 }}>{p.unidad}</small> {p.dentroDeRango ? '✓' : '▲'}
            </span>
            <div className="rango" aria-hidden="true">
              <div className="rango-normal" style={{ left: pos(p.minimo), width: `calc(${pos(p.maximo)} - ${pos(p.minimo)})` }} />
              <div className={`rango-marca ${estado}`} style={{ left: pos(p.valor) }} />
            </div>
            <span className="rango-texto">
              Referencia: {p.minimo > 0 ? `${p.minimo} – ${p.maximo}` : `menor a ${p.maximo}`} {p.unidad}
              {!p.dentroDeRango && ` · ${p.valor > p.maximo ? 'Por encima' : 'Por debajo'} del rango normal`}
            </span>
          </div>
        );
      })}
    </article>
  );
}
