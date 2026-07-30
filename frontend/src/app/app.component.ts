import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { ChatComponent } from './chat/chat.component';
import { AuthService } from './auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ChatComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'MS-Event-Processor';
  authService = inject(AuthService);
  private router = inject(Router);

  logout(): void {
    this.authService.logout();
  }
  
  getUserInitial(): string {
    const name = this.authService.getCurrentUser()?.name;
    return name ? name.charAt(0).toUpperCase() : 'U';
  }
  
  getUserRole(): string {
    return this.authService.getRoleName();
  }
}
