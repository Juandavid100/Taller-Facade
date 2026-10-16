import { useState } from 'react';
import { api } from '../api';

const VACIO = {
  documento: '', nombres: '', apellidos: '', email: '', telefono: '',
  fechaNacimiento: '', password: '', alergias: '',
};

/** Registro e inicio de sesión del paciente. */
export default function Acceso({ onIngreso }) {
  const [modo, setModo] = useState('login');
  const [login, setLogin] = useState({ documento: '', password: '' });
  const [registro, setRegistro] = useState(VACIO);
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  const cambiar = (setter) => (e) => setter((prev) => ({ ...prev, [e.target.name]: e.target.value }));

  async function enviar(e) {
    e.preventDefault();
    setError('');
    setCargando(true);
    try {
      if (modo === 'login') {
        onIngreso(await api.login(login.documento.trim(), login.password));
      } else {
        const alergias = registro.alergias.split(',').map((a) => a.trim()).filter(Boolean);
        const paciente = await api.registrar({
          ...registro,
          fechaNacimiento: registro.fechaNacimiento || null,
          alergias,
        });
        onIngreso(paciente);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className="acceso">
      <section className="acceso-arte">
        <div className="marca" style={{ padding: 0 }}>
          <div className="marca-icono">+</div>
          <h1 style={{ fontSize: '1.3rem' }}>Clínica Vital</h1>
        </div>
        <div>
          <h1>Su salud, en un solo lugar.</h1>
          <p>Agende citas, consulte su historia clínica, revise sus prescripciones y vea sus resultados de laboratorio sin filas ni llamadas.</p>
        </div>
        <ul>
          <li>Citas con Medicina General, Cardiología y Dermatología</li>
          <li>Resultados de laboratorio con rangos de referencia</li>
          <li>Validación automática de alergias en sus fórmulas</li>
        </ul>
      </section>

      <section className="acceso-panel">
        <form className="acceso-caja pila" onSubmit={enviar}>
          <h2>{modo === 'login' ? 'Iniciar sesión' : 'Crear cuenta'}</h2>
          <p className="vacio" style={{ padding: 0 }}>
            {modo === 'login' ? '¿Primera vez aquí? ' : '¿Ya tiene cuenta? '}
            <a href="#" onClick={(e) => { e.preventDefault(); setModo(modo === 'login' ? 'registro' : 'login'); setError(''); }}>
              {modo === 'login' ? 'Regístrese' : 'Inicie sesión'}
            </a>
          </p>

          {modo === 'login' ? (
            <>
              <label className="campo">Documento
                <input name="documento" inputMode="numeric" value={login.documento} onChange={cambiar(setLogin)} required />
              </label>
              <label className="campo">Contraseña
                <input name="password" type="password" value={login.password} onChange={cambiar(setLogin)} required />
              </label>
              <div className="demo">Cuenta de prueba: documento <b>1085000000</b> · contraseña <b>demo123</b></div>
            </>
          ) : (
            <>
              <div className="campos-2">
                <label className="campo">Nombres
                  <input name="nombres" value={registro.nombres} onChange={cambiar(setRegistro)} required />
                </label>
                <label className="campo">Apellidos
                  <input name="apellidos" value={registro.apellidos} onChange={cambiar(setRegistro)} required />
                </label>
              </div>
              <div className="campos-2">
                <label className="campo">Documento
                  <input name="documento" inputMode="numeric" pattern="\d{6,12}" title="Entre 6 y 12 dígitos"
                    value={registro.documento} onChange={cambiar(setRegistro)} required />
                </label>
                <label className="campo">Fecha de nacimiento
                  <input name="fechaNacimiento" type="date" value={registro.fechaNacimiento} onChange={cambiar(setRegistro)} />
                </label>
              </div>
              <div className="campos-2">
                <label className="campo">Correo
                  <input name="email" type="email" value={registro.email} onChange={cambiar(setRegistro)} required />
                </label>
                <label className="campo">Teléfono
                  <input name="telefono" value={registro.telefono} onChange={cambiar(setRegistro)} />
                </label>
              </div>
              <label className="campo">Alergias (separadas por coma)
                <input name="alergias" placeholder="Ej: Penicilina, AINES" value={registro.alergias} onChange={cambiar(setRegistro)} />
              </label>
              <label className="campo">Contraseña
                <input name="password" type="password" minLength={6} value={registro.password} onChange={cambiar(setRegistro)} required />
              </label>
            </>
          )}

          {error && <div className="mensaje error">{error}</div>}
          <button className="boton bloque" disabled={cargando}>
            {cargando ? 'Un momento…' : modo === 'login' ? 'Entrar' : 'Crear cuenta'}
          </button>
        </form>
      </section>
    </div>
  );
}
