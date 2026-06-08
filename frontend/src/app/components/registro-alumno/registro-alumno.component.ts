// registro-alumno.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-registro-alumno',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './registro-alumno.component.html',
  styleUrls: ['./registro-alumno.component.css']
})
export class RegistroAlumnoComponent {

  loading = false;
  error = '';
  exito = false;
  mostrarClave = false;

  form = {
    paterno: '', materno: '', nombre: '',
    fechanac: '', docidentidad: '',
    direccion: '', sexo: '',
    correo: '', telefono: ''
  };

  // Para crear usuario
  clave = '';
  confirmarClave = '';

  constructor(private http: HttpClient, private router: Router) {}

  registrar(): void {
    if (!this.validar()) return;
    this.loading = true;
    this.error = '';

    // Crear estudiante
    this.http.post(`${environment.apiUrl}/estudiantes`, this.form).subscribe({
      next: (est: any) => {
        // Crear usuario para el alumno
        const usuario = {
          codusuario: this.form.correo || this.form.docidentidad,
          clave: this.clave,
          rol: 'PADRE',
          codestado: 'ACTIVO'
        };
        this.http.post(`${environment.apiUrl}/auth/register`, usuario).subscribe({
          next: () => { this.loading = false; this.exito = true; },
          error: () => { this.loading = false; this.exito = true; } // igual mostramos éxito
        });
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error || 'Error al registrar. El DNI o correo ya existe.';
      }
    });
  }

  private validar(): boolean {
    if (!this.form.docidentidad || this.form.docidentidad.length !== 8) {
      this.error = 'El DNI debe tener exactamente 8 dígitos'; return false;
    }
    if (!this.form.nombre || !this.form.paterno || !this.form.materno) {
      this.error = 'Nombre y apellidos son requeridos'; return false;
    }
    if (!this.form.fechanac) {
      this.error = 'La fecha de nacimiento es requerida'; return false;
    }
    if (!this.clave || this.clave.length < 6) {
      this.error = 'La contraseña debe tener al menos 6 caracteres'; return false;
    }
    if (this.clave !== this.confirmarClave) {
      this.error = 'Las contraseñas no coinciden'; return false;
    }
    return true;
  }
}
