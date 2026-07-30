import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { LineBreaksPipe } from '../pipes/line-breaks.pipe';

interface ChatMessage {
  type: 'user' | 'bot';
  content: string;
  timestamp: Date;
}

interface ChatResponse {
  status: string;
  question: string;
  answer: string;
}

interface SuggestionsResponse {
  status: string;
  suggestions: string[];
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, LineBreaksPipe],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit {
  messages: ChatMessage[] = [];
  currentQuestion = '';
  isLoading = false;
  suggestions: string[] = [];
  isOpen = false;

  private apiUrl = '/api/v1/chat';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadSuggestions();
    // Mensaje de bienvenida
    this.messages.push({
      type: 'bot',
      content: '¡Hola! 👋 Soy OrderBot, tu asistente virtual. Puedo ayudarte con consultas sobre órdenes, estadísticas y más. ¿En qué puedo ayudarte?',
      timestamp: new Date()
    });
  }

  loadSuggestions(): void {
    this.http.get<SuggestionsResponse>(`${this.apiUrl}/suggestions`).subscribe({
      next: (response) => {
        this.suggestions = response.suggestions || [];
      },
      error: (err) => {
        console.error('Error loading suggestions:', err);
        // Sugerencias por defecto
        this.suggestions = [
          '¿Cuántas órdenes pendientes hay?',
          '¿Cuál es el ingreso total?',
          '¿Cuáles son las últimas órdenes?'
        ];
      }
    });
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
  }

  askQuestion(question?: string): void {
    const questionText = question || this.currentQuestion.trim();
    if (!questionText || this.isLoading) return;

    // Agregar mensaje del usuario
    this.messages.push({
      type: 'user',
      content: questionText,
      timestamp: new Date()
    });

    this.currentQuestion = '';
    this.isLoading = true;

    this.http.post<ChatResponse>(`${this.apiUrl}/ask`, { question: questionText }).subscribe({
      next: (response) => {
        this.messages.push({
          type: 'bot',
          content: response.answer,
          timestamp: new Date()
        });
        this.isLoading = false;
        this.scrollToBottom();
      },
      error: (err) => {
        this.messages.push({
          type: 'bot',
          content: 'Lo siento, hubo un error al procesar tu pregunta. Por favor intenta nuevamente. 🙁',
          timestamp: new Date()
        });
        this.isLoading = false;
        console.error('Error asking question:', err);
      }
    });
  }

  useSuggestion(suggestion: string): void {
    this.askQuestion(suggestion);
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.askQuestion();
    }
  }

  clearChat(): void {
    this.messages = [{
      type: 'bot',
      content: '¡Chat reiniciado! ¿En qué puedo ayudarte?',
      timestamp: new Date()
    }];
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const container = document.querySelector('.chat-messages');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }, 100);
  }
}
