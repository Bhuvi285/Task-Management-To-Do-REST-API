import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { taskService } from "@/features/tasks/api/taskService";
import type {
  CreateTaskRequest,
  TaskQueryParams,
  UpdateTaskRequest,
} from "@/features/tasks/types";

export const taskKeys = {
  all: ["tasks"] as const,
  lists: () => [...taskKeys.all, "list"] as const,
  list: (params: TaskQueryParams) => [...taskKeys.lists(), params] as const,
  details: () => [...taskKeys.all, "detail"] as const,
  detail: (id: number) => [...taskKeys.details(), id] as const,
};

export function useTasksQuery(params: TaskQueryParams) {
  return useQuery({
    queryKey: taskKeys.list(params),
    queryFn: () => taskService.getTasks(params),
  });
}

export function useTaskQuery(taskId: number) {
  return useQuery({
    queryKey: taskKeys.detail(taskId),
    queryFn: () => taskService.getTask(taskId),
    enabled: taskId > 0,
  });
}

export function useCreateTaskMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateTaskRequest) => taskService.createTask(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: taskKeys.lists() });
    },
  });
}

export function useUpdateTaskMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ taskId, payload }: { taskId: number; payload: UpdateTaskRequest }) =>
      taskService.updateTask(taskId, payload),
    onSuccess: (data) => {
      queryClient.setQueryData(taskKeys.detail(data.id), data);
      void queryClient.invalidateQueries({ queryKey: taskKeys.lists() });
    },
  });
}

export function useDeleteTaskMutation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (taskId: number) => taskService.deleteTask(taskId),
    onSuccess: (_data, taskId) => {
      queryClient.removeQueries({ queryKey: taskKeys.detail(taskId) });
      void queryClient.invalidateQueries({ queryKey: taskKeys.lists() });
    },
  });
}
