import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { CalendarDays, Loader2, Save } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useToast } from "@/components/ui/toast";
import { normalizeApiError } from "@/lib/api/errors";
import {
  useCreateTaskMutation,
  useUpdateTaskMutation,
} from "@/features/tasks/hooks/useTasksQuery";
import {
  createTaskSchema,
  updateTaskSchema,
} from "@/features/tasks/schemas/taskSchemas";
import type { CreateTaskFormValues, UpdateTaskFormValues } from "@/features/tasks/schemas/taskSchemas";
import type { TaskResponse } from "@/features/tasks/types";

interface TaskFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  editingTask?: TaskResponse | null;
}

export function TaskFormDialog({ open, onOpenChange, editingTask }: TaskFormDialogProps) {
  const { toast } = useToast();
  const isEdit = Boolean(editingTask);
  const createMutation = useCreateTaskMutation();
  const updateMutation = useUpdateTaskMutation();
  const isPending = createMutation.isPending || updateMutation.isPending;

  const createForm = useForm<CreateTaskFormValues>({
    resolver: zodResolver(createTaskSchema) as never,
    defaultValues: { title: "", description: "", priority: "MEDIUM", dueDate: "" },
  });

  const updateForm = useForm<UpdateTaskFormValues>({
    resolver: zodResolver(updateTaskSchema) as never,
    defaultValues: {
      title: editingTask?.title ?? "",
      description: editingTask?.description ?? "",
      status: editingTask?.status ?? "PENDING",
      priority: editingTask?.priority ?? "MEDIUM",
      dueDate: editingTask?.dueDate ?? "",
    },
  });

  useEffect(() => {
    if (open) {
      if (editingTask) {
        updateForm.reset({
          title: editingTask.title,
          description: editingTask.description ?? "",
          status: editingTask.status,
          priority: editingTask.priority,
          dueDate: editingTask.dueDate ?? "",
        });
      } else {
        createForm.reset({ title: "", description: "", priority: "MEDIUM", dueDate: "" });
      }
    }
  }, [open, editingTask, createForm, updateForm]);

  const handleCreate = async (values: CreateTaskFormValues) => {
    try {
      await createMutation.mutateAsync({
        title: values.title,
        description: values.description || null,
        priority: values.priority,
        dueDate: values.dueDate || null,
      });
      toast({ title: "Task created", variant: "success" });
      onOpenChange(false);
    } catch (err) {
      toast({
        title: "Failed to create task",
        description: normalizeApiError(err).message,
        variant: "error",
      });
    }
  };

  const handleUpdate = async (values: UpdateTaskFormValues) => {
    if (!editingTask) return;
    try {
      await updateMutation.mutateAsync({
        taskId: editingTask.id,
        payload: {
          title: values.title,
          description: values.description || null,
          status: values.status,
          priority: values.priority,
          dueDate: values.dueDate || null,
        },
      });
      toast({ title: "Task updated", variant: "success" });
      onOpenChange(false);
    } catch (err) {
      toast({
        title: "Failed to update task",
        description: normalizeApiError(err).message,
        variant: "error",
      });
    }
  };

  if (isEdit) {
    const { register, handleSubmit, formState: { errors } } = updateForm;
    return (
      <Dialog
        open={open}
        onOpenChange={onOpenChange}
        title="Edit Task"
        description="Update the task details below."
      >
        <form onSubmit={handleSubmit(handleUpdate as never)} className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="edit-title">Title *</Label>
            <Input id="edit-title" placeholder="Task title" {...register("title")} aria-invalid={Boolean(errors.title)} />
            {errors.title ? <p className="text-xs text-rose-500">{errors.title.message}</p> : null}
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="edit-description">Description</Label>
            <Textarea id="edit-description" placeholder="Optional description" rows={3} {...register("description")} />
            {errors.description ? <p className="text-xs text-rose-500">{errors.description.message}</p> : null}
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="edit-status">Status *</Label>
              <Select id="edit-status" {...register("status")} aria-invalid={Boolean(errors.status)}>
                <option value="PENDING">Pending</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="COMPLETED">Completed</option>
              </Select>
              {errors.status ? <p className="text-xs text-rose-500">{errors.status.message}</p> : null}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="edit-priority">Priority *</Label>
              <Select id="edit-priority" {...register("priority")} aria-invalid={Boolean(errors.priority)}>
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </Select>
              {errors.priority ? <p className="text-xs text-rose-500">{errors.priority.message}</p> : null}
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="edit-dueDate">Due Date</Label>
            <div className="relative">
              <Input id="edit-dueDate" type="date" {...register("dueDate")} />
              <CalendarDays className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            </div>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isPending}>
              Cancel
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
              Save Changes
            </Button>
          </div>
        </form>
      </Dialog>
    );
  }

  const { register, handleSubmit, formState: { errors } } = createForm;
  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
      title="Create Task"
      description="Add a new task to your workspace."
    >
      <form onSubmit={handleSubmit(handleCreate as never)} className="space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="create-title">Title *</Label>
          <Input id="create-title" placeholder="What needs to be done?" {...register("title")} aria-invalid={Boolean(errors.title)} />
          {errors.title ? <p className="text-xs text-rose-500">{errors.title.message}</p> : null}
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="create-description">Description</Label>
          <Textarea id="create-description" placeholder="Optional description" rows={3} {...register("description")} />
          {errors.description ? <p className="text-xs text-rose-500">{errors.description.message}</p> : null}
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-1.5">
            <Label htmlFor="create-priority">Priority</Label>
            <Select id="create-priority" {...register("priority")}>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="create-dueDate">Due Date</Label>
            <div className="relative">
              <Input id="create-dueDate" type="date" {...register("dueDate")} />
              <CalendarDays className="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            </div>
          </div>
        </div>
        <div className="flex justify-end gap-2 pt-2">
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isPending}>
            Cancel
          </Button>
          <Button type="submit" disabled={isPending}>
            {isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
            Create Task
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
