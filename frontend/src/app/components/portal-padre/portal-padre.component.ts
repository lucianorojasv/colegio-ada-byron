import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiService } from '../../services/api.service';
import { Seccion, GradoAcademico } from '../../models/models';
import { PasarelaPagoComponent } from '../pasarela-pago/pasarela-pago.component';

interface Documento {
  id: string;
  nombre: string;
  descripcion: string;
  obligatorio: boolean;
  niveles: string[]; // 'INICIAL' | 'PRIMARIA' | 'SECUNDARIA' | 'TODOS'
  archivo: File | null;
  previsualizacion: string;
  cargado: boolean;
}

@Component({
  selector: 'app-portal-padre',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PasarelaPagoComponent],
  templateUrl: './portal-padre.component.html',
  styleUrls: ['./portal-padre.component.css']
})
export class PortalPadreComponent implements OnInit {

  // 5 pasos: 1=Apoderado, 2=Estudiante, 3=Sección, 4=Documentos, 5=Confirmar, 6=Éxito
  paso = 1;
  loading = false;
  error = '';
  solicitudCreada: any = null;

  grados: GradoAcademico[] = [];
  secciones: Seccion[] = [];
  seccionesFiltradas: Seccion[] = [];

  // Consulta
  modoConsulta = false;
  idConsulta = '';
  resultadoConsulta: any = null;

  // Pago
  mostrarPasarela = false;
  pagoRealizado = false;
  codigoTransaccion = '';

  // Documentos requeridos
  documentos: Documento[] = [
    {
      id: 'dni_estudiante',
      nombre: 'DNI del Estudiante',
      descripcion: 'Documento Nacional de Identidad o Carné de Extranjería del estudiante',
      obligatorio: true,
      niveles: ['TODOS'],
      archivo: null, previsualizacion: '', cargado: false
    },
    {
      id: 'dni_apoderado',
      nombre: 'DNI del Apoderado',
      descripcion: 'DNI o Carné de Extranjería del padre, madre o tutor legal',
      obligatorio: true,
      niveles: ['TODOS'],
      archivo: null, previsualizacion: '', cargado: false
    },
    {
      id: 'partida_nacimiento',
      nombre: 'Partida / Acta de Nacimiento',
      descripcion: 'Partida o Acta de Nacimiento del menor (original o copia legalizada)',
      obligatorio: true,
      niveles: ['TODOS'],
      archivo: null, previsualizacion: '', cargado: false
    },
    {
      id: 'vacunacion',
      nombre: 'Cartilla de Vacunación',
      descripcion: 'Cartilla de vacunación actualizada (obligatorio para Inicial)',
      obligatorio: false,
      niveles: ['INICIAL'],
      archivo: null, previsualizacion: '', cargado: false
    },
    {
      id: 'certificado_estudios',
      nombre: 'Certificado de Estudios',
      descripcion: 'Certificado del grado anterior aprobado',
      obligatorio: true,
      niveles: ['PRIMARIA', 'SECUNDARIA'],
      archivo: null, previsualizacion: '', cargado: false
    },
    {
      id: 'constancia_siagie',
      nombre: 'Constancia SIAGIE',
      descripcion: 'Constancia de matrícula del SIAGIE (si proviene de otra institución)',
      obligatorio: false,
      niveles: ['PRIMARIA', 'SECUNDARIA'],
      archivo: null, previsualizacion: '', cargado: false
    }
  ];

  form = {
    apoderadoPaterno: '', apoderadoMaterno: '', apoderadoNombre: '',
    apoderadoCelular: '', apoderadoCorreo: '', apoderadoDni: '',
    estudiantePaterno: '', estudianteMaterno: '', estudianteNombre: '',
    estudianteFechanac: '', estudianteDni: '', estudianteDireccion: '',
    idseccion: 0, idgrado: 0, aniolectivo: '2026', comentario: ''
  };

  constructor(private http: HttpClient, private api: ApiService) {}

  ngOnInit(): void {
    this.api.getGrados().subscribe({ next: g => this.grados = g, error: () => {} });
    this.api.getSecciones().subscribe({ next: s => this.secciones = s, error: () => {} });
  }

  onGradoChange(): void {
    this.seccionesFiltradas = this.secciones.filter(
      s => s.grado?.idgrado === Number(this.form.idgrado));
    this.form.idseccion = 0;
  }

  get nivelSeleccionado(): string {
    const sec = this.seccionSeleccionada;
    if (!sec) return 'TODOS';
    const nombre = sec.grado?.nombre?.toLowerCase() || '';
    if (nombre.includes('inicial')) return 'INICIAL';
    if (nombre.includes('primaria')) return 'PRIMARIA';
    if (nombre.includes('secundaria')) return 'SECUNDARIA';
    return 'TODOS';
  }

  get documentosFiltrados(): Documento[] {
    const nivel = this.nivelSeleccionado;
    return this.documentos.filter(d =>
      d.niveles.includes('TODOS') || d.niveles.includes(nivel)
    );
  }

  get documentosObligatoriosFaltantes(): Documento[] {
    return this.documentosFiltrados.filter(d => d.obligatorio && !d.cargado);
  }

  onArchivoSeleccionado(event: Event, doc: Documento): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      alert('El archivo no debe superar 5MB');
      return;
    }

    doc.archivo = file;
    doc.cargado = true;

    // Preview para imágenes
    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = (e) => doc.previsualizacion = e.target?.result as string;
      reader.readAsDataURL(file);
    } else {
      doc.previsualizacion = '';
    }
  }

  eliminarArchivo(doc: Documento): void {
    doc.archivo = null;
    doc.previsualizacion = '';
    doc.cargado = false;
  }

  get totalDocsCargados(): number {
    return this.documentosFiltrados.filter(d => d.cargado).length;
  }

  get totalDocsRequeridos(): number {
    return this.documentosFiltrados.filter(d => d.obligatorio).length;
  }

  siguientePaso(): void {
    this.error = '';
    if (this.paso === 1 && !this.validarPaso1()) return;
    if (this.paso === 2 && !this.validarPaso2()) return;
    if (this.paso === 3 && !this.validarPaso3()) return;
    if (this.paso === 4 && !this.validarPaso4()) return;
    this.paso++;
  }

  pasoAnterior(): void { this.paso--; this.error = ''; }

  validarPaso1(): boolean {
    if (!this.form.apoderadoNombre || !this.form.apoderadoPaterno ||
        !this.form.apoderadoDni || !this.form.apoderadoCelular || !this.form.apoderadoCorreo) {
      this.error = 'Complete todos los campos del apoderado'; return false;
    }
    if (this.form.apoderadoDni.length < 8) {
      this.error = 'El DNI debe tener 8 dígitos'; return false;
    }
    return true;
  }

  validarPaso2(): boolean {
    if (!this.form.estudianteNombre || !this.form.estudiantePaterno || !this.form.estudianteFechanac) {
      this.error = 'Complete los datos del estudiante'; return false;
    }
    return true;
  }

  validarPaso3(): boolean {
    if (!this.form.idseccion || this.form.idseccion === 0) {
      this.error = 'Seleccione una sección'; return false;
    }
    return true;
  }

  validarPaso4(): boolean {
    const faltantes = this.documentosObligatoriosFaltantes;
    if (faltantes.length > 0) {
      this.error = `Falta subir: ${faltantes.map(d => d.nombre).join(', ')}`;
      return false;
    }
    return true;
  }

  get seccionSeleccionada(): Seccion | undefined {
    return this.secciones.find(s => s.idseccion === Number(this.form.idseccion));
  }

  enviarSolicitud(): void {
    this.loading = true;
    this.error = '';

    // Paso 1: Registrar la solicitud (sin archivos)
    const payload = { ...this.form, idseccion: Number(this.form.idseccion) };
    this.http.post(`${environment.apiUrl}/solicitudes/registrar`, payload).subscribe({
      next: (res: any) => {
        this.solicitudCreada = res;
        const idsolicitud = res.idsolicitud;

        // Paso 2: Subir cada documento vinculado al idsolicitud
        const archivosParaSubir = this.documentos.filter(d => d.archivo !== null);

        if (archivosParaSubir.length === 0) {
          this.loading = false;
          this.paso = 6;
          return;
        }

        let subidos = 0;
        archivosParaSubir.forEach(doc => {
          const fd = new FormData();
          fd.append('tipo',    doc.id);
          fd.append('archivo', doc.archivo!, doc.archivo!.name);

          this.http.post(
            `${environment.apiUrl}/documentos/subir/${idsolicitud}`, fd
          ).subscribe({
            next: () => {
              subidos++;
              if (subidos === archivosParaSubir.length) {
                this.loading = false;
                this.paso = 6;
              }
            },
            error: () => {
              subidos++;
              if (subidos === archivosParaSubir.length) {
                this.loading = false;
                this.paso = 6; // igual avanzar aunque falle alguno
              }
            }
          });
        });
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.error || 'Error al enviar la solicitud.';
      }
    });
  }

  consultarEstado(): void {
    if (!this.idConsulta) { this.error = 'Ingrese el número de solicitud'; return; }
    this.loading = true;
    this.resultadoConsulta = null;
    this.pagoRealizado = false;
    this.http.get(`${environment.apiUrl}/solicitudes/estado/${this.idConsulta}`).subscribe({
      next: (res: any) => { this.loading = false; this.resultadoConsulta = res; },
      error: () => { this.loading = false; this.error = 'Solicitud no encontrada'; }
    });
  }

  abrirPasarela(): void { this.mostrarPasarela = true; }

  onPagoExitoso(resultado: any): void {
    this.mostrarPasarela = false;
    this.pagoRealizado = true;
    this.codigoTransaccion = resultado.codigoTransaccion;
  }

  getColorEstado(estado: string): string {
    const map: any = { 'PEND':'estado-pend','APRO':'estado-apro','OBS':'estado-obs','REC':'estado-rec' };
    return map[estado] || '';
  }

  getIconoArchivo(doc: Documento): string {
    if (!doc.archivo) return '📄';
    const type = doc.archivo.type;
    if (type.includes('pdf')) return '📕';
    if (type.includes('image')) return '🖼️';
    return '📄';
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024*1024)).toFixed(1) + ' MB';
  }
}
