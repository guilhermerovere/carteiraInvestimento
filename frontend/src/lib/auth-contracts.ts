export type Role = "ROLE_USER" | "ROLE_ADMIN";

export type CurrentUser = {
  id: string;
  nome: string;
  email: string;
  role: Role;
  ativo: boolean;
};

export type LoginInput = { email: string; senha: string };
export type RegisterInput = { nome: string; email: string; senha: string };
