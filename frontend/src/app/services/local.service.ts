import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Local, LocalDto, LocalPageResponse } from '../models/local.model';

/**
 * Servicio para gestión de Locales
 */
@Injectable({
  providedIn: 'root'
})
export class LocalService {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/locals';

  getAll(): Observable<Local[]> {
    return this.http.get<Local[]>(this.apiUrl);
  }

  getPage(page: number, size: number): Observable<LocalPageResponse> {
    return this.http.get<LocalPageResponse>(`${this.apiUrl}/page?page=${page}&size=${size}`);
  }

  getById(localId: string): Observable<Local> {
    return this.http.get<Local>(`${this.apiUrl}/${localId}`);
  }

  create(dto: LocalDto): Observable<Local> {
    return this.http.post<Local>(this.apiUrl, dto);
  }

  update(localId: string, dto: LocalDto): Observable<Local> {
    return this.http.put<Local>(`${this.apiUrl}/${localId}`, dto);
  }

  delete(localId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${localId}`);
  }

  activate(localId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${localId}/activate`, {});
  }

  deactivate(localId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${localId}/deactivate`, {});
  }

  searchByName(name: string): Observable<Local[]> {
    return this.http.get<Local[]>(`${this.apiUrl}/search?name=${encodeURIComponent(name)}`);
  }

  getByCity(city: string): Observable<Local[]> {
    return this.http.get<Local[]>(`${this.apiUrl}/city/${encodeURIComponent(city)}`);
  }
}
