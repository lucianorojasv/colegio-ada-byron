// src/app/models/models.ts

export interface LoginRequest {
  codusuario: string;
  clave: string;
}

export interface LoginResponse {
  token: string;
  codusuario: string;
  rol: string;
  mensaje: string;
}

export interface Estudiante {
  idestudiante?: number;
  paterno: string;
  materno: string;
  nombre: string;
  fechanac: string;
  docidentidad?: string;
  direccion?: string;
  estado?: string;
}

export interface Nivel {
  idnivel: number;
  nombre: string;
}

export interface GradoAcademico {
  idgrado: number;
  nombre: string;
  nivel: Nivel;
}

export interface Seccion {
  idseccion?: number;
  nombre: string;
  capacidad: number;
  turno: string;
  aula?: string;
  grado: GradoAcademico;
}

export interface Pago {
  idpago?: number;
  importe: number;
  metodopago: string;
  codestado?: string;
  fecharegisto?: string;
}

export interface Matricula {
  idmatricula?: number;
  aniolectivo: string;
  fecharegisto?: string;
  estudiante: Estudiante;
  seccion: Seccion;
  pago?: Pago;
  codestado?: string;
}

export interface MatriculaRequest {
  idestudiante: number;
  idseccion: number;
  aniolectivo: string;
  montoMatricula: number;
  metodopago: string;
}

export interface DashboardStats {
  totalAlumnos: number;
  totalMatriculas: number;
  pendientes: number;
  totalIngresos: number;
}
