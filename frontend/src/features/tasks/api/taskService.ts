import { apiClient } from "@/lib/api/client";
import type {
  CreateTaskRequest,
  TaskPageResponse,
  TaskQueryParams,
  TaskResponse,
  UpdateTaskRequest,
} from "@/features/tasks/types";

function buildQueryString(params: TaskQueryParams): string {
  const search = new URLSearchParams();
  if (params.page !== undefined) search.set("page", String(params.page));
  if (params.size !== undefined) search.set("size", String(params.size));
  if (params.sortBy) search.set("sortBy", params.sortBy);
  if (params.direction) search.set("direction", params.direction);
  if (params.search?.trim()) search.set("search", params.search.trim());
  if (params.status) search.set("status", params.status);
  if (params.priority) search.set("priority", params.priority);
  if (params.dueFrom) search.set("dueFrom", params.dueFrom);
  if (params.dueTo) search.set("dueTo", params.dueTo);
  return search.toString();
}

export const taskService = {
  async getTasks(params: TaskQueryParams = {}): Promise<TaskPageResponse> {
    const qs = buildQueryString(params);
    const { data } = await apiClient.get<TaskPageResponse>(`/tasks${qs ? `?${qs}` : ""}`);
    return data;
  },

  async getTask(taskId: number): Promise<TaskResponse> {
    const { data } = await apiClient.get<TaskResponse>(`/tasks/${taskId}`);
    return data;
  },

  async createTask(payload: CreateTaskRequest): Promise<TaskResponse> {
    const { data } = await apiClient.post<TaskResponse>("/tasks", payload);
    return data;
  },

  async updateTask(taskId: number, payload: UpdateTaskRequest): Promise<TaskResponse> {
    const { data } = await apiClient.put<TaskResponse>(`/tasks/${taskId}`, payload);
    return data;
  },

  async deleteTask(taskId: number): Promise<void> {
    await apiClient.delete(`/tasks/${taskId}`);
  },
};
