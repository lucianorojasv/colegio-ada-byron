// src/app/components/grados/grados.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { GradoAcademico, Nivel } from '../../models/models';

@Component({
  selector: 'app-grados',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './grados.component.html',
  styleUrls: ['./grados.component.css']
})
export class GradosComponent implements OnInit {
  grados: GradoAcademico[] = [];
  niveles: Nivel[] = [
    { idnivel: 1, nombre: 'Inicial' },
    { idnivel: 2, nombre: 'Primaria' },
    { idnivel: 3, nombre: 'Secundaria' }
  ];
  loading = false;
  error = '';
  exito = '';
  mostrarModal = false;
  modoEdicion = false;
  gradoSeleccionado: GradoAcademico | null = null;

  form: any = this.formVacio();

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading = true;
    this.api.getGrados().subscribe({
      next: list => { this.grados = list; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.form = this.formVacio();
    this.error = '';
    this.mostrarModal = true;
  }

  abrirEditar(g: GradoAcademico): void {
    this.modoEdicion = true;
    this.gradoSeleccionado = g;
    this.form = { nombre: g.nombre, idnivel: g.nivel?.idnivel };
    this.error = '';
    this.mostrarModal = true;
  }

  guardar(): void {
    if (!this.form.nombre || !this.form.idnivel) {
      this.error = 'Nombre y nivel son requeridos'; return;
    }
    this.loading = true;
    const payload = { nombre: this.form.nombre, nivel: { idnivel: this.form.idnivel } };
    const obs = this.modoEdicion && this.gradoSeleccionado?.idgrado
      ? this.api.actualizarGrado(this.gradoSeleccionado.idgrado, payload as GradoAcademico)
      : this.api.crearGrado(payload as GradoAcademico);

    obs.subscribe({
      next: () => {
        this.loading = false;
        this.mostrarModal = false;
        this.exito = this.modoEdicion ? 'Grado actualizado' : 'Grado creado exitosamente';
        this.cargar();
        setTimeout(() => this.exito = '', 4000);
      },
      error: () => { this.loading = false; this.error = 'Error al guardar el grado'; }
    });
  }

  eliminar(g: GradoAcademico): void {
    if (!confirm(`¿Eliminar el grado "${g.nombre}"?`)) return;
    this.api.eliminarGrado(g.idgrado).subscribe({
      next: () => { this.exito = 'Grado eliminado'; this.cargar(); setTimeout(() => this.exito = '', 3000); },
      error: () => { this.error = 'No se puede eliminar: tiene secciones asignadas'; }
    });
  }

  cerrarModal(e: Event): void {
    if ((e.target as HTMLElement).classList.contains('modal-overlay')) this.mostrarModal = false;
  }

  getNivelNombre(idnivel: number): string {
    return this.niveles.find(n => n.idnivel === idnivel)?.nombre || '';
  }

  gradosPorNivel(idnivel: number): GradoAcademico[] {
    return this.grados.filter(g => g.nivel?.idnivel === idnivel);
  }

  private formVacio() { return { nombre: '', idnivel: 0 }; }
}
