import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { DashboardStats, Matricula } from '../../models/models';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  stats: DashboardStats = { totalAlumnos:0, totalMatriculas:0, pendientes:0, totalIngresos:0 };
  ultimasMatriculas: Matricula[] = [];
  ultimosPagos: any[] = [];
  totalPagos = 0;
  pendientes = 0;
  usuario = localStorage.getItem('usuario') || 'Admin';
  rol     = localStorage.getItem('rol')     || 'ADMIN';

  constructor(
    private api:  ApiService,
    public  auth: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    // Stats generales
    this.api.getStats('2026').subscribe({ next: s => this.stats = s, error: () => {} });

    // Últimas matrículas
    this.api.getMatriculas('2026').subscribe({
      next: list => this.ultimasMatriculas = list.slice(0, 5),
      error: () => {}
    });

    // Solicitudes pendientes
    this.http.get<any[]>(`${environment.apiUrl}/solicitudes`).subscribe({
      next: list => this.pendientes = list.filter((s:any) => s.codestado === 'PEND').length,
      error: () => {}
    });

    // Últimos pagos
    this.http.get<any[]>(`${environment.apiUrl}/pagos`).subscribe({
      next: list => {
        this.totalPagos = list.length;
        this.ultimosPagos = list.slice(0, 6);
      },
      error: () => {}
    });
  }

  logout(): void { this.auth.logout(); }

  exportarExcel(): void {
    this.api.exportarExcel('2026').subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'matriculas_2026.xlsx'; a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  copiarUrl(e?: Event): void {
    if (e) e.stopPropagation();
    navigator.clipboard.writeText('http://localhost:4200/portal-padre').then(() => {
      alert('URL copiada:\nhttp://localhost:4200/portal-padre');
    });
  }
}
