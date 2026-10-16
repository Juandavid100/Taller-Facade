const fechaLarga = new Intl.DateTimeFormat('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });
const fechaCorta = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short', year: 'numeric' });
const hora = new Intl.DateTimeFormat('es-CO', { hour: '2-digit', minute: '2-digit', hour12: true });

export const formatoDia = (iso) => fechaLarga.format(new Date(iso));
export const formatoFecha = (iso) => fechaCorta.format(new Date(iso));
export const formatoHora = (iso) => hora.format(new Date(iso));

/** yyyy-mm-dd en hora local (sin desfase de zona horaria). */
export function diaISO(fecha) {
  const d = new Date(fecha);
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 10);
}

export function proximosDias(cantidad) {
  const dias = [];
  const d = new Date();
  while (dias.length < cantidad) {
    d.setDate(d.getDate() + 1);
    if (d.getDay() !== 0) dias.push(diaISO(d));
  }
  return dias;
}

export const enRango = (p) => p.dentroDeRango;
