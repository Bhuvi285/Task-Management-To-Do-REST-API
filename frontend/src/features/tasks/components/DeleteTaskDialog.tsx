import { Loader2, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Dialog } from "@/components/ui/dialog";
import { useToast } from "@/components/ui/toast";
import { normalizeApiError } from "@/lib/api/errors";
import { useDeleteTaskMutation } from "@/features/tasks/hooks/useTasksQuery";
import type { TaskResponse } from "@/features/tasks/types";

interface DeleteTaskDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  task: TaskResponse | null;
  onDeleted?: () => void;
}

export function DeleteTaskDialog({ open, onOpenChange, task, onDeleted }: DeleteTaskDialogProps) {
  const { toast } = useToast();
  const deleteMutation = useDeleteTaskMutation();

  const handleDelete = async () => {
    if (!task) return;
    try {
      await deleteMutation.mutateAsync(task.id);
      toast({ title: "Task deleted", variant: "success" });
      onOpenChange(false);
      onDeleted?.();
    } catch (err) {
      toast({
        title: "Failed to delete task",
        description: normalizeApiError(err).message,
        variant: "error",
      });
    }
  };

  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
      title="Delete Task"
      description={`Are you sure you want to delete "${task?.title ?? ""}"? This action cannot be undone.`}
    >
      <div className="flex justify-end gap-2 pt-2">
        <Button
          type="button"
          variant="outline"
          onClick={() => onOpenChange(false)}
          disabled={deleteMutation.isPending}
        >
          Cancel
        </Button>
        <Button
          type="button"
          variant="destructive"
          onClick={handleDelete}
          disabled={deleteMutation.isPending}
        >
          {deleteMutation.isPending ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <Trash2 className="h-4 w-4" />
          )}
          Delete Task
        </Button>
      </div>
    </Dialog>
  );
}
