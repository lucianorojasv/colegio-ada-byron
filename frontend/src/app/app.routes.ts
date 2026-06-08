import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  // ── PÚBLICAS (sin login) ──────────────────────────────────
  { path: '',
    loadComponent: () => import('./components/landing/landing.component').then(m => m.LandingComponent)
  },
  { path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  { path: 'registro',
    loadComponent: () => import('./components/registro-alumno/registro-alumno.component').then(m => m.RegistroAlumnoComponent)
  },
  { path: 'portal-padre',
    loadComponent: () => import('./components/portal-padre/portal-padre.component').then(m => m.PortalPadreComponent)
  },

  // ── PRIVADAS (requieren login) ────────────────────────────
  { path: 'dashboard', canActivate: [authGuard],
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  { path: 'alumnos', canActivate: [authGuard],
    loadComponent: () => import('./components/alumnos/alumnos.component').then(m => m.AlumnosComponent)
  },
  { path: 'secciones', canActivate: [authGuard],
    loadComponent: () => import('./components/secciones/secciones.component').then(m => m.SeccionesComponent)
  },
  { path: 'grados', canActivate: [authGuard],
    loadComponent: () => import('./components/grados/grados.component').then(m => m.GradosComponent)
  },
  { path: 'matricula', canActivate: [authGuard],
    loadComponent: () => import('./components/matricula/matricula.component').then(m => m.MatriculaComponent)
  },
  { path: 'pagos', canActivate: [authGuard],
    loadComponent: () => import('./components/pagos/pagos.component').then(m => m.PagosComponent)
  },
  { path: 'secretaria', canActivate: [authGuard],
    loadComponent: () => import('./components/secretaria/secretaria.component').then(m => m.SecretariaComponent)
  },
  { path: 'usuarios', canActivate: [authGuard],
    loadComponent: () => import('./components/usuarios/usuarios.component').then(m => m.UsuariosComponent)
  },
  { path: '**', redirectTo: '' }
];
