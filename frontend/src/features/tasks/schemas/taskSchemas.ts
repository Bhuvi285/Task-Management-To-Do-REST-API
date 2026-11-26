import { z } from "zod";

export const createTaskSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, "Title is required.")
    .max(255, "Title must not exceed 255 characters."),
  description: z
    .string()
    .max(255, "Description must not exceed 255 characters.")
    .optional(),
  priority: z.enum(["LOW", "MEDIUM", "HIGH", "URGENT"]).optional(),
  dueDate: z.string().optional(),
});

export interface CreateTaskFormValues {
  title: string;
  description?: string;
  priority?: "LOW" | "MEDIUM" | "HIGH" | "URGENT";
  dueDate?: string;
}

export const updateTaskSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, "Title is required.")
    .max(255, "Title must not exceed 255 characters."),
  description: z
    .string()
    .max(255, "Description must not exceed 255 characters.")
    .optional(),
  status: z.enum(["PENDING", "IN_PROGRESS", "COMPLETED"]),
  priority: z.enum(["LOW", "MEDIUM", "HIGH", "URGENT"]),
  dueDate: z.string().optional(),
});

export interface UpdateTaskFormValues {
  title: string;
  description?: string;
  status: "PENDING" | "IN_PROGRESS" | "COMPLETED";
  priority: "LOW" | "MEDIUM" | "HIGH" | "URGENT";
  dueDate?: string;
}
