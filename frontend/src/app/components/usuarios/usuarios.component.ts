// usuarios.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './usuarios.component.html',
  styleUrls: ['./usuarios.component.css']
})
export class UsuariosComponent implements OnInit {

  usuarios: any[] = [];
  loading = false;
  error = '';
  exito = '';
  busqueda = '';
  mostrarModal = false;
  modoEdicion = false;
  usuarioSeleccionado: any = null;

  roles = ['ADMIN', 'SECRETARIA', 'DIRECTIVO', 'PADRE'];

  form: any = this.formVacio();

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading = true;
    this.http.get<any[]>(`${environment.apiUrl}/usuarios`).subscribe({
      next: list => { this.usuarios = list; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Error al cargar usuarios'; }
    });
  }

  abrirCrear(): void {
    this.modoEdicion = false;
    this.form = this.formVacio();
    this.error = '';
    this.mostrarModal = true;
  }

  abrirEditar(u: any): void {
    this.modoEdicion = true;
    this.usuarioSeleccionado = u;
    this.form = {
      codusuario: u.codusuario,
      clave: '',
      rol: u.rol,
      codestado: u.codestado
    };
    this.error = '';
    this.mostrarModal = true;
  }

  guardar(): void {
    if (!this.form.codusuario || !this.form.rol) {
      this.error = 'Usuario y rol son requeridos'; return;
    }
    if (!this.modoEdicion && !this.form.clave) {
      this.error = 'La contraseña es requerida para nuevos usuarios'; return;
    }
    this.loading = true;
    this.error = '';

    const obs = this.modoEdicion
      ? this.http.put(`${environment.apiUrl}/usuarios/${this.usuarioSeleccionado.idusuario}`, this.form)
      : this.http.post(`${environment.apiUrl}/usuarios`, this.form);

    obs.subscribe({
      next: () => {
        this.loading = false;
        this.mostrarModal = false;
        this.exito = this.modoEdicion ? 'Usuario actualizado' : 'Usuario creado exitosamente';
        this.cargar();
        setTimeout(() => this.exito = '', 4000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error || 'Error al guardar el usuario';
      }
    });
  }

  cambiarEstado(u: any): void {
    const nuevoEstado = u.codestado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    const accion = nuevoEstado === 'ACTIVO' ? 'activar' : 'desactivar';
    if (!confirm(`¿Desea ${accion} al usuario "${u.codusuario}"?`)) return;

    this.http.put(`${environment.apiUrl}/usuarios/${u.idusuario}/estado`, { codestado: nuevoEstado }).subscribe({
      next: () => {
        this.exito = `Usuario ${nuevoEstado === 'ACTIVO' ? 'activado' : 'desactivado'} correctamente`;
        this.cargar();
        setTimeout(() => this.exito = '', 3000);
      },
      error: () => { this.error = 'Error al cambiar estado'; }
    });
  }

  resetPassword(u: any): void {
    const nueva = prompt(`Nueva contraseña para "${u.codusuario}":`);
    if (!nueva || nueva.length < 4) { alert('La contraseña debe tener al menos 4 caracteres'); return; }
    this.http.put(`${environment.apiUrl}/usuarios/${u.idusuario}/password`, { clave: nueva }).subscribe({
      next: () => { this.exito = 'Contraseña actualizada'; setTimeout(() => this.exito = '', 3000); },
      error: () => { this.error = 'Error al cambiar contraseña'; }
    });
  }

  cerrarModal(e: Event): void {
    if ((e.target as HTMLElement).classList.contains('modal-overlay')) {
      this.mostrarModal = false;
    }
  }

  get usuariosFiltrados(): any[] {
    if (!this.busqueda.trim()) return this.usuarios;
    const q = this.busqueda.toLowerCase();
    return this.usuarios.filter(u =>
      u.codusuario?.toLowerCase().includes(q) ||
      u.rol?.toLowerCase().includes(q)
    );
  }

  getRolColor(rol: string): string {
    const map: any = {
      'ADMIN':     'rol-admin',
      'SECRETARIA':'rol-sec',
      'DIRECTIVO': 'rol-dir',
      'PADRE':     'rol-padre'
    };
    return map[rol] || 'rol-padre';
  }

  get totalActivos():   number { return this.usuarios.filter(u => u.codestado === 'ACTIVO').length; }
  get totalInactivos(): number { return this.usuarios.filter(u => u.codestado !== 'ACTIVO').length; }
  get totalAdmins():    number { return this.usuarios.filter(u => u.rol === 'ADMIN').length; }

  private formVacio() {
    return { codusuario: '', clave: '', rol: 'PADRE', codestado: 'ACTIVO' };
  }
}
