// Pasarela de Pago integrada al Portal - Luciano Rojas - U23271185
// pasarela-pago.component.ts
import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-pasarela-pago',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './pasarela-pago.component.html',
  styleUrls: ['./pasarela-pago.component.css']
})
export class PasarelaPagoComponent implements OnInit {
  @Input() idestudiante!: number;
  @Input() nombreEstudiante = '';
  @Input() monto = 500;
  @Output() pagoExitoso = new EventEmitter<any>();
  @Output() cerrar = new EventEmitter<void>();

  // Paso: 'metodo' | 'tarjeta' | 'yape' | 'plin' | 'confirmacion'
  paso: 'metodo' | 'tarjeta' | 'yape' | 'plin' | 'confirmacion' = 'metodo';

  metodoPago = ''; // TARJETA | YAPE | PLIN
  loading = false;
  error = '';
  resultadoPago: any = null;

  // Tarjeta
  numeroTarjeta = '';
  fechaVenc = '';
  cvv = '';
  nombreTitular = '';
  apellidoTitular = '';
  correoTitular = '';
  mostrarCvv = false;
  tipoDetectado = '';

  // Yape
  celularYape = '';
  codigoYape = '';

  // Plin
  celularPlin = '';
  bancoPlin = '';

  bancos = [
    { id: 'INTERBANK', nombre: 'Interbank', color: '#00A650' },
    { id: 'BBVA',      nombre: 'BBVA',      color: '#004481' },
    { id: 'SCOTIABANK',nombre: 'Scotiabank',color: '#EC111A' }
  ];

  get numeroFormateado(): string {
    return this.numeroTarjeta.replace(/\s/g, '').replace(/(.{4})/g, '$1 ').trim();
  }

  onNumeroChange(): void {
    const n = this.numeroTarjeta.replace(/\D/g, '').substring(0, 16);
    this.numeroTarjeta = n.replace(/(.{4})/g, '$1 ').trim();
    if (n.startsWith('4'))      this.tipoDetectado = 'VISA';
    else if (n.startsWith('5')) this.tipoDetectado = 'MASTERCARD';
    else if (n.startsWith('3')) this.tipoDetectado = 'AMEX';
    else                        this.tipoDetectado = '';
  }

  onFechaChange(): void {
    let val = this.fechaVenc.replace(/\D/g, '').substring(0, 4);
    if (val.length > 2) val = val.substring(0, 2) + '/' + val.substring(2);
    this.fechaVenc = val;
  }

  seleccionarMetodo(m: string): void { this.metodoPago = m; }

  continuarMetodo(): void {
    if (!this.metodoPago) { this.error = 'Selecciona un método de pago'; return; }
    this.error = '';
    this.paso = this.metodoPago.toLowerCase() as any;
  }

  procesarPago(): void {
    this.error = '';
    if (!this.validarCampos()) return;

    this.loading = true;
    const payload: any = {
      idestudiante: this.idestudiante,
      monto: this.monto,
      metodoPago: this.metodoPago,
      nombreTitular:   this.nombreTitular,
      apellidoTitular: this.apellidoTitular,
      correoTitular:   this.correoTitular,
      numeroTarjeta:   this.numeroTarjeta?.replace(/\s/g, ''),
      cvv:             this.cvv,
      fechaVencimiento:this.fechaVenc,
      celularYape:     this.celularYape,
      codigoYape:      this.codigoYape,
      celularPlin:     this.celularPlin,
      bancoPlin:       this.bancoPlin
    };

    this.http.post(`${environment.apiUrl}/pagos/pasarela/procesar`, payload).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.resultadoPago = res;
        this.paso = 'confirmacion';
        if (res.success) this.pagoExitoso.emit(res);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.mensaje || 'Error al procesar el pago';
      }
    });
  }

  private validarCampos(): boolean {
    if (this.metodoPago === 'TARJETA') {
      if (!this.numeroTarjeta.replace(/\s/g, '') || this.numeroTarjeta.replace(/\D/g,'').length < 13)
        { this.error = 'Número de tarjeta inválido'; return false; }
      if (!this.fechaVenc.match(/^\d{2}\/\d{2}$/))
        { this.error = 'Fecha de vencimiento inválida (MM/AA)'; return false; }
      if (!this.cvv || this.cvv.length < 3)
        { this.error = 'CVV inválido'; return false; }
      if (!this.nombreTitular)
        { this.error = 'Ingresa el nombre del titular'; return false; }
    }
    if (this.metodoPago === 'YAPE') {
      if (!this.celularYape || this.celularYape.replace(/\D/g,'').length !== 9)
        { this.error = 'Número de celular Yape inválido'; return false; }
      if (!this.codigoYape)
        { this.error = 'Ingresa el código de aprobación de Yape'; return false; }
    }
    if (this.metodoPago === 'PLIN') {
      if (!this.celularPlin || this.celularPlin.replace(/\D/g,'').length !== 9)
        { this.error = 'Número de celular Plin inválido'; return false; }
      if (!this.bancoPlin)
        { this.error = 'Selecciona tu banco para Plin'; return false; }
    }
    return true;
  }

  get bancoSeleccionado() {
    return this.bancos.find(b => b.id === this.bancoPlin);
  }

  ngOnInit(): void {}
  constructor(private http: HttpClient) {}
}
