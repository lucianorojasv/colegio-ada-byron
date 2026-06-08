import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  codusuario = '';
  clave = '';
  error = '';
  loading = false;
  mostrarClave = false;

  constructor(private auth: AuthService, private router: Router) {
    if (this.auth.isLoggedIn()) this.router.navigate(['/dashboard']);
  }

  login(): void {
    this.loading = true;
    this.error = '';
    this.auth.login({ codusuario: this.codusuario, clave: this.clave }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.token) this.router.navigate(['/dashboard']);
        else this.error = res.mensaje;
      },
      error: () => {
        this.loading = false;
        this.error = 'Credenciales inválidas. Inténtalo de nuevo.';
      }
    });
  }
}
