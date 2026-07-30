import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, UserDto, UserStats, UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = '/api/v1/users';

  constructor(private http: HttpClient) {}

  /**
   * Obtiene todos los usuarios activos
   */
  getAll(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  /**
   * Obtiene usuarios paginados
   */
  getPage(page: number = 0, size: number = 10, sortBy: string = 'name', direction: string = 'asc'): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    
    return this.http.get<any>(`${this.apiUrl}/page`, { params });
  }

  /**
   * Busca usuarios por nombre o email
   */
  search(query: string, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<any>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Obtiene un usuario por ID
   */
  getById(userId: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${userId}`);
  }

  /**
   * Obtiene usuarios por rol
   */
  getByRole(role: UserRole): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/role/${role}`);
  }

  /**
   * Crea un nuevo usuario
   */
  create(user: UserDto): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  /**
   * Actualiza un usuario
   */
  update(userId: string, user: UserDto): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${userId}`, user);
  }

  /**
   * Actualiza el rol de un usuario
   */
  updateRole(userId: string, role: UserRole): Observable<User> {
    const params = new HttpParams().set('role', role);
    return this.http.patch<User>(`${this.apiUrl}/${userId}/role`, null, { params });
  }

  /**
   * Desactiva un usuario
   */
  deactivate(userId: string): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${userId}`);
  }

  /**
   * Activa un usuario
   */
  activate(userId: string): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/${userId}/activate`, null);
  }

  /**
   * Obtiene estadísticas de usuarios
   */
  getStats(): Observable<UserStats> {
    return this.http.get<UserStats>(`${this.apiUrl}/stats`);
  }
}
