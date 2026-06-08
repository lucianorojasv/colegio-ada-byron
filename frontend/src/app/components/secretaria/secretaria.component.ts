// secretaria.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-secretaria',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './secretaria.component.html',
  styleUrls: ['./secretaria.component.css']
})
export class SecretariaComponent implements OnInit {

  solicitudes: any[] = [];
  loading = false;
  error = '';
  exito = '';
  documentosSolicitud: any[] = [];
  loadingDocs = false;
  filtroEstado = 'TODOS';
  solicitudDetalle: any = null;
  modalAccion = '';
  comentarioAccion = '';

  constructor(private http: HttpClient) {}

  cargarDocumentos(idsolicitud: number): void {
    this.loadingDocs = true;
    this.documentosSolicitud = [];
    this.http.get<any[]>(`${environment.apiUrl}/documentos/solicitud/${idsolicitud}`)
      .subscribe({
        next: docs => { this.documentosSolicitud = docs; this.loadingDocs = false; },
        error: () => { this.loadingDocs = false; }
      });
  }

  verDocumento(iddocumento: number): void {
    window.open(`http://localhost:8080/api/documentos/ver/${iddocumento}`, '_blank');
  }

  revisarDocumento(iddocumento: number, estado: string): void {
    this.http.put(`${environment.apiUrl}/documentos/${iddocumento}/revisar`,
      { estado }).subscribe({
        next: () => {
          const doc = this.documentosSolicitud.find(d => d.iddocumento === iddocumento);
          if (doc) doc.estado = estado;
        },
        error: () => {}
      });
  }

  getNombreDoc(tipo: string): string {
    const map: any = {
      'dni_estudiante':      'DNI del Estudiante',
      'dni_apoderado':       'DNI del Apoderado',
      'partida_nacimiento':  'Partida de Nacimiento',
      'certificado_estudios':'Certificado de Estudios',
      'constancia_siagie':   'Constancia SIAGIE',
      'vacunacion':          'Cartilla de Vacunación'
    };
    return map[tipo] || tipo;
  }

  getDocEstadoClass(estado: string): string {
    const map: any = {
      'CONFORME': 'doc-conforme',
      'OBSERVADO': 'doc-observado',
      'PENDIENTE': 'doc-pendiente'
    };
    return map[estado] || 'doc-pendiente';
  }

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading = true;
    this.http.get(`${environment.apiUrl}/solicitudes`).subscribe({
      next: (list: any) => { this.solicitudes = list; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Error al cargar solicitudes'; }
    });
  }

  get solicitudesFiltradas(): any[] {
    if (this.filtroEstado === 'TODOS') return this.solicitudes;
    return this.solicitudes.filter(s => s.codestado === this.filtroEstado);
  }

  verDetalle(s: any): void { this.solicitudDetalle = s; this.modalAccion = ''; this.comentarioAccion = ''; this.cargarDocumentos(s.idsolicitud); }

  abrirAccion(accion: string): void { this.modalAccion = accion; }

  ejecutarAccion(): void {
    if (!this.solicitudDetalle) return;
    const id = this.solicitudDetalle.idsolicitud;
    const url = `${environment.apiUrl}/solicitudes/${id}/${this.modalAccion}`;
    const body = { comentario: this.comentarioAccion };

    this.http.put(url, body).subscribe({
      next: () => {
        this.exito = `Solicitud ${this.getAccionLabel(this.modalAccion)} correctamente`;
        this.solicitudDetalle = null;
        this.modalAccion = '';
        this.cargar();
        setTimeout(() => this.exito = '', 4000);
      },
      error: () => { this.error = 'Error al procesar la acción'; }
    });
  }

  getAccionLabel(a: string): string {
    const map: any = { 'aprobar': 'aprobada', 'observar': 'observada', 'rechazar': 'rechazada' };
    return map[a] || a;
  }

  getBadgeClass(estado: string): string {
    const map: any = { 'PEND': 'badge-pend', 'APRO': 'badge-apro', 'OBS': 'badge-obs', 'REC': 'badge-rec' };
    return map[estado] || '';
  }

  getEstadoLabel(estado: string): string {
    const map: any = { 'PEND': 'Pendiente', 'APRO': 'Aprobada', 'OBS': 'Observada', 'REC': 'Rechazada' };
    return map[estado] || estado;
  }

  get totalPend(): number { return this.solicitudes.filter(s => s.codestado === 'PEND').length; }
  get totalApro(): number { return this.solicitudes.filter(s => s.codestado === 'APRO').length; }
  get totalObs():  number { return this.solicitudes.filter(s => s.codestado === 'OBS').length; }
  get totalRec():  number { return this.solicitudes.filter(s => s.codestado === 'REC').length; }

  cerrarModal(e: Event): void {
    if ((e.target as HTMLElement).classList.contains('modal-overlay')) {
      this.solicitudDetalle = null;
    }
  }
}
