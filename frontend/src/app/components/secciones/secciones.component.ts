// src/app/components/secciones/secciones.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Seccion, GradoAcademico } from '../../models/models';

@Component({
  selector: 'app-secciones',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './secciones.component.html',
  styleUrls: ['./secciones.component.css']
})
export class SeccionesComponent implements OnInit {
  secciones: Seccion[] = [];
  grados: GradoAcademico[] = [];
  loading = false;
  error = '';
  exito = '';
  mostrarModal = false;
  modoEdicion = false;
  seccionSeleccionada: Seccion | null = null;

  form: any = this.formVacio();

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.cargar();
    this.api.getGrados().subscribe({ next: g => this.grados = g, error: () => {} });
  }

  cargar(): void {
    this.loading = true;
    this.api.getSecciones().subscribe({
      next: list => { this.secciones = list; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.form = this.formVacio();
    this.error = '';
    this.mostrarModal = true;
  }

  abrirEditar(s: Seccion): void {
    this.modoEdicion = true;
    this.seccionSeleccionada = s;
    this.form = { nombre: s.nombre, capacidad: s.capacidad, turno: s.turno, aula: s.aula, idgrado: s.grado?.idgrado };
    this.error = '';
    this.mostrarModal = true;
  }

  guardar(): void {
    if (!this.form.nombre || !this.form.idgrado) {
      this.error = 'Nombre y grado son requeridos'; return;
    }
    this.loading = true;
    const payload = {
      nombre: this.form.nombre, capacidad: this.form.capacidad,
      turno: this.form.turno, aula: this.form.aula,
      grado: { idgrado: this.form.idgrado }
    } as Seccion;

    const obs = this.modoEdicion && this.seccionSeleccionada?.idseccion
      ? this.api.actualizarSeccion(this.seccionSeleccionada.idseccion, payload)
      : this.api.crearSeccion(payload);

    obs.subscribe({
      next: () => {
        this.loading = false;
        this.mostrarModal = false;
        this.exito = this.modoEdicion ? 'Sección actualizada' : 'Sección creada exitosamente';
        this.cargar();
        setTimeout(() => this.exito = '', 4000);
      },
      error: () => { this.loading = false; this.error = 'Error al guardar la sección'; }
    });
  }

  eliminar(s: Seccion): void {
    if (!confirm(`¿Eliminar la sección "${s.nombre}"?`)) return;
    this.api.eliminarSeccion(s.idseccion!).subscribe({
      next: () => { this.exito = 'Sección eliminada'; this.cargar(); setTimeout(() => this.exito = '', 3000); },
      error: () => { this.error = 'No se puede eliminar: tiene matrículas asignadas'; }
    });
  }

  cerrarModal(e: Event): void {
    if ((e.target as HTMLElement).classList.contains('modal-overlay')) this.mostrarModal = false;
  }

  getTurnoColor(turno: string): string {
    return turno === 'Mañana' ? 'turno-manana' : turno === 'Tarde' ? 'turno-tarde' : 'turno-noche';
  }

  private formVacio() {
    return { nombre: '', capacidad: 30, turno: 'Mañana', aula: '', idgrado: 0 };
  }
}
