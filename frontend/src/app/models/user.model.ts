// User model for frontend
export enum UserRole {
  ADMIN = 'ADMIN',
  SUPERVISOR = 'SUPERVISOR',
  CUSTOMER = 'CUSTOMER'
}

export interface User {
  id: number;
  userId: string;
  name: string;
  email: string;
  phone?: string;
  role: UserRole;
  avatarUrl?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string;
}

export interface UserDto {
  userId?: string;
  name: string;
  email: string;
  phone?: string;
  role: UserRole;
  avatarUrl?: string;
  active?: boolean;
  password?: string;
}

export interface UserStats {
  totalAdmins: number;
  totalSupervisors: number;
  totalCustomers: number;
}
