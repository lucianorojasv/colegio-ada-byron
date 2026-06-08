// src/app/services/api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Estudiante, GradoAcademico, Seccion,
  Matricula, MatriculaRequest, Pago, DashboardStats
} from '../models/models';

@Injectable({ providedIn: 'root' })
export class ApiService {

  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ── Estudiantes ──────────────────────────────────────────
  getEstudiantes(): Observable<Estudiante[]> {
    return this.http.get<Estudiante[]>(`${this.api}/estudiantes`);
  }
  buscarEstudiantes(q: string): Observable<Estudiante[]> {
    return this.http.get<Estudiante[]>(`${this.api}/estudiantes/buscar`, { params: { q } });
  }
  buscarPorDni(dni: string): Observable<Estudiante> {
    return this.http.get<Estudiante>(`${this.api}/estudiantes/dni/${dni}`);
  }
  crearEstudiante(e: Estudiante): Observable<Estudiante> {
    return this.http.post<Estudiante>(`${this.api}/estudiantes`, e);
  }
  actualizarEstudiante(id: number, e: Estudiante): Observable<Estudiante> {
    return this.http.put<Estudiante>(`${this.api}/estudiantes/${id}`, e);
  }
  eliminarEstudiante(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/estudiantes/${id}`);
  }

  // ── Grados ───────────────────────────────────────────────
  getGrados(): Observable<GradoAcademico[]> {
    return this.http.get<GradoAcademico[]>(`${this.api}/grados`);
  }
  crearGrado(g: GradoAcademico): Observable<GradoAcademico> {
    return this.http.post<GradoAcademico>(`${this.api}/grados`, g);
  }
  actualizarGrado(id: number, g: GradoAcademico): Observable<GradoAcademico> {
    return this.http.put<GradoAcademico>(`${this.api}/grados/${id}`, g);
  }
  eliminarGrado(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/grados/${id}`);
  }

  // ── Secciones ────────────────────────────────────────────
  getSecciones(): Observable<Seccion[]> {
    return this.http.get<Seccion[]>(`${this.api}/secciones`);
  }
  getVacantes(idseccion: number, anio: string): Observable<any> {
    return this.http.get(`${this.api}/secciones/${idseccion}/vacantes`, { params: { anio } });
  }
  crearSeccion(s: Seccion): Observable<Seccion> {
    return this.http.post<Seccion>(`${this.api}/secciones`, s);
  }
  actualizarSeccion(id: number, s: Seccion): Observable<Seccion> {
    return this.http.put<Seccion>(`${this.api}/secciones/${id}`, s);
  }
  eliminarSeccion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/secciones/${id}`);
  }

  // ── Matrículas ───────────────────────────────────────────
  getMatriculas(anio: string = '2026'): Observable<Matricula[]> {
    return this.http.get<Matricula[]>(`${this.api}/matriculas`, { params: { anio } });
  }
  registrarMatricula(req: MatriculaRequest): Observable<Matricula> {
    return this.http.post<Matricula>(`${this.api}/matriculas`, req);
  }
  getStats(anio: string = '2026'): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.api}/matriculas/stats`, { params: { anio } });
  }

  // ── Pagos ────────────────────────────────────────────────
  getPagos(): Observable<Pago[]> {
    return this.http.get<Pago[]>(`${this.api}/pagos`);
  }

  confirmarPago(id: number): Observable<Pago> {
    return this.http.put<Pago>(`${this.api}/pagos/${id}/confirmar`, {});
  }

  // ── Reportes ─────────────────────────────────────────────
  exportarExcel(anio: string = '2026'): Observable<Blob> {
    return this.http.get(`${this.api}/reportes/matriculas/excel`,
      { responseType: 'blob', params: { anio } });
  }
}
