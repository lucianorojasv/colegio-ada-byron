import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { PasarelaPagoComponent } from '../pasarela-pago/pasarela-pago.component';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-pagos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PasarelaPagoComponent],
  templateUrl: './pagos.component.html',
  styleUrls: ['./pagos.component.css']
})
export class PagosComponent implements OnInit {

  pagos: any[] = [];
  loading = false;
  error = '';
  exito = '';
  busqueda = '';

  // Pasarela
  mostrarPasarela = false;
  estudianteSeleccionado: any = null;
  montoPago = 500;

  constructor(private http: HttpClient) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.loading = true;
    this.http.get<any[]>(`${environment.apiUrl}/pagos`).subscribe({
      next: p => { this.pagos = p; this.loading = false; },
      error: () => { this.loading = false; this.error = 'Error al cargar pagos'; }
    });
  }

  abrirPasarela(estudiante?: any): void {
    this.estudianteSeleccionado = estudiante || { idestudiante: 1, nombre: 'Estudiante', paterno: '' };
    this.mostrarPasarela = true;
  }

  onPagoExitoso(resultado: any): void {
    this.mostrarPasarela = false;
    this.exito = `✅ Pago registrado correctamente. Código: ${resultado.codigoTransaccion}`;
    this.cargar();
    setTimeout(() => this.exito = '', 6000);
  }

  getEstadoClass(estado: string): string {
    const m: any = { 'PAGADO': 'estado-pagado', 'PENDIENTE': 'estado-pend', 'ANULADO': 'estado-anul' };
    return m[estado] || '';
  }

  get pagosFiltrados(): any[] {
    if (!this.busqueda.trim()) return this.pagos;
    const q = this.busqueda.toLowerCase();
    return this.pagos.filter(p =>
      p.estudiante?.nombre?.toLowerCase().includes(q) ||
      p.estudiante?.paterno?.toLowerCase().includes(q) ||
      p.metodopago?.toLowerCase().includes(q)
    );
  }
}
