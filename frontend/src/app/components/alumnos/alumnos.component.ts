// src/app/components/alumnos/alumnos.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Estudiante } from '../../models/models';

@Component({
  selector: 'app-alumnos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './alumnos.component.html',
  styleUrls: ['./alumnos.component.css']
})
export class AlumnosComponent implements OnInit {
  alumnos: Estudiante[] = [];
  busqueda = '';
  loading = false;
  error = '';
  exito = '';

  mostrarModal = false;
  modoEdicion = false;
  alumnoSeleccionado: Estudiante | null = null;

  form: Estudiante = this.formVacio();

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading = true;
    this.api.getEstudiantes().subscribe({
      next: list => { this.alumnos = list; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Error al cargar alumnos'; }
    });
  }

  buscar(): void {
    if (!this.busqueda.trim()) { this.cargar(); return; }
    this.api.buscarEstudiantes(this.busqueda).subscribe({
      next: list => this.alumnos = list,
      error: () => {}
    });
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.form = this.formVacio();
    this.error = '';
    this.mostrarModal = true;
  }

  abrirEditar(a: Estudiante): void {
    this.modoEdicion = true;
    this.alumnoSeleccionado = a;
    this.form = { ...a };
    this.error = '';
    this.mostrarModal = true;
  }

  guardar(): void {
    this.loading = true;
    this.error = '';
    const obs = this.modoEdicion && this.alumnoSeleccionado?.idestudiante
      ? this.api.actualizarEstudiante(this.alumnoSeleccionado.idestudiante, this.form)
      : this.api.crearEstudiante(this.form);

    obs.subscribe({
      next: () => {
        this.loading = false;
        this.mostrarModal = false;
        this.exito = this.modoEdicion ? 'Alumno actualizado' : 'Alumno registrado exitosamente';
        this.cargar();
        setTimeout(() => this.exito = '', 4000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Error al guardar el alumno';
      }
    });
  }

  eliminar(a: Estudiante): void {
    if (!confirm(`¿Desactivar al alumno ${a.nombre} ${a.paterno}?`)) return;
    this.api.eliminarEstudiante(a.idestudiante!).subscribe({
      next: () => { this.exito = 'Alumno desactivado'; this.cargar(); setTimeout(() => this.exito = '', 3000); },
      error: () => { this.error = 'Error al desactivar'; }
    });
  }

  cerrarModal(e: Event): void {
    if ((e.target as HTMLElement).classList.contains('modal-overlay')) this.mostrarModal = false;
  }

  private formVacio(): Estudiante {
    return { paterno: '', materno: '', nombre: '', fechanac: '', docidentidad: '', direccion: '', estado: 'ACTIVO' };
  }

  get alumnosFiltrados(): Estudiante[] {
    if (!this.busqueda.trim()) return this.alumnos;
    const q = this.busqueda.toLowerCase();
    return this.alumnos.filter(a =>
      (a.nombre + ' ' + a.paterno).toLowerCase().includes(q) ||
      (a.docidentidad || '').includes(q)
    );
  }
}
