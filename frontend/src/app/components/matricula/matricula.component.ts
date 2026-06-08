// src/app/components/matricula/matricula.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Estudiante, Seccion, Matricula } from '../../models/models';

@Component({
  selector: 'app-matricula',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './matricula.component.html',
  styleUrls: ['./matricula.component.css']
})
export class MatriculaComponent implements OnInit {
  matriculas: Matricula[] = [];
  secciones: Seccion[] = [];
  loading = false;
  error = '';
  exito = '';
  mostrarFormulario = false;
  busquedaTabla = '';

  // Formulario nueva matrícula
  dniBusqueda = '';
  estudianteEncontrado: Estudiante | null = null;
  buscandoDni = false;
  form = {
    idseccion: 0,
    aniolectivo: '2026',
    montoMatricula: 500,
    metodopago: 'EFECTIVO'
  };

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.cargarMatriculas();
    this.api.getSecciones().subscribe({ next: s => this.secciones = s, error: () => {} });
  }

  cargarMatriculas(): void {
    this.loading = true;
    this.api.getMatriculas('2026').subscribe({
      next: list => { this.matriculas = list; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  buscarPorDni(): void {
    if (!this.dniBusqueda.trim()) return;
    this.buscandoDni = true;
    this.estudianteEncontrado = null;
    this.api.buscarPorDni(this.dniBusqueda.trim()).subscribe({
      next: e => { this.estudianteEncontrado = e; this.buscandoDni = false; },
      error: () => { this.buscandoDni = false; this.error = 'Alumno no encontrado con ese DNI'; }
    });
  }

  registrarMatricula(): void {
    if (!this.estudianteEncontrado || !this.form.idseccion) {
      this.error = 'Complete todos los campos requeridos';
      return;
    }
    this.loading = true;
    this.error = '';
    this.api.registrarMatricula({
      idestudiante: this.estudianteEncontrado.idestudiante!,
      idseccion: this.form.idseccion,
      aniolectivo: this.form.aniolectivo,
      montoMatricula: this.form.montoMatricula,
      metodopago: this.form.metodopago
    }).subscribe({
      next: () => {
        this.loading = false;
        this.exito = 'Matrícula registrada exitosamente';
        this.mostrarFormulario = false;
        this.resetForm();
        this.cargarMatriculas();
        setTimeout(() => this.exito = '', 4000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error || 'Error al registrar la matrícula';
      }
    });
  }

  resetForm(): void {
    this.dniBusqueda = '';
    this.estudianteEncontrado = null;
    this.form = { idseccion: 0, aniolectivo: '2026', montoMatricula: 500, metodopago: 'EFECTIVO' };
    this.error = '';
  }

  get totalActivas(): number { return this.matriculas.filter(m => m.codestado === 'ACTIVO').length; }
  get totalPendientes(): number { return this.matriculas.filter(m => m.codestado === 'PENDIENTE').length; }
  get totalIngresos(): number {
    return this.matriculas.reduce((acc, m) => acc + (m.pago?.importe || 0), 0);
  }

  get matriculasFiltradas(): Matricula[] {
    if (!this.busquedaTabla.trim()) return this.matriculas;
    const q = this.busquedaTabla.toLowerCase();
    return this.matriculas.filter(m =>
      (m.estudiante?.nombre + ' ' + m.estudiante?.paterno).toLowerCase().includes(q) ||
      (m.estudiante?.docidentidad || '').toLowerCase().includes(q) ||
      (m.seccion?.grado?.nombre || '').toLowerCase().includes(q)
    );
  }

  cerrarModal(event: Event): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.mostrarFormulario = false;
      this.resetForm();
    }
  }
}
