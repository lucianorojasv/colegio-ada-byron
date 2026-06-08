// landing.component.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit, OnDestroy {

  menuAbierto = false;
  scrolled = false;
  anioActual = new Date().getFullYear();

  // Contador animado
  contadores = [
    { valor: 0, objetivo: 850, sufijo: '+', label: 'Estudiantes', icono: '👤' },
    { valor: 0, objetivo: 45,  sufijo: '+', label: 'Docentes',    icono: '👨‍🏫' },
    { valor: 0, objetivo: 30,  sufijo: '',   label: 'Años de exp.', icono: '🏆' },
    { valor: 0, objetivo: 98,  sufijo: '%', label: 'Satisfacción', icono: '⭐' }
  ];

  private scrollListener: any;
  private animacionIniciada = false;

  ngOnInit(): void {
    this.scrollListener = () => {
      this.scrolled = window.scrollY > 60;
      if (window.scrollY > 400 && !this.animacionIniciada) {
        this.animacionIniciada = true;
        this.animarContadores();
      }
    };
    window.addEventListener('scroll', this.scrollListener);
  }

  ngOnDestroy(): void {
    window.removeEventListener('scroll', this.scrollListener);
  }

  toggleMenu(): void { this.menuAbierto = !this.menuAbierto; }
  cerrarMenu(): void { this.menuAbierto = false; }

  scrollTo(id: string): void {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
    this.cerrarMenu();
  }

  private animarContadores(): void {
    this.contadores.forEach(c => {
      const duracion = 2000;
      const pasos = 60;
      const incremento = c.objetivo / pasos;
      let paso = 0;
      const intervalo = setInterval(() => {
        paso++;
        c.valor = Math.min(Math.round(incremento * paso), c.objetivo);
        if (paso >= pasos) clearInterval(intervalo);
      }, duracion / pasos);
    });
  }
}
