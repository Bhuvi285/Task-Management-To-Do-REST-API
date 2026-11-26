export type TaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";
export type TaskPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";
export type SortDirection = "asc" | "desc";
export type SortField =
  | "id"
  | "createdAt"
  | "updatedAt"
  | "dueDate"
  | "title"
  | "status"
  | "priority";

export interface TaskResponse {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null; // ISO date yyyy-MM-dd
  createdAt: string; // ISO datetime
  updatedAt: string; // ISO datetime
}

export interface TaskPageResponse {
  content: TaskResponse[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface CreateTaskRequest {
  title: string;
  description?: string | null;
  dueDate?: string | null; // yyyy-MM-dd
  priority?: TaskPriority;
}

export interface UpdateTaskRequest {
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null; // yyyy-MM-dd
}

export interface TaskQueryParams {
  page?: number;
  size?: number;
  sortBy?: SortField;
  direction?: SortDirection;
  search?: string;
  status?: TaskStatus | "";
  priority?: TaskPriority | "";
  dueFrom?: string;
  dueTo?: string;
}
