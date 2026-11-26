import { useEffect, useState } from "react";
import { format, parseISO } from "date-fns";
import {
  AlignLeft,
  ArrowRight,
  Calendar,
  ChevronRight,
  Flag,
  Loader2,
  Save,
  Trash2,
  X,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useToast } from "@/components/ui/toast";
import { DeleteTaskDialog } from "@/features/tasks/components/DeleteTaskDialog";
import { TaskPriorityBadge, TaskStatusBadge } from "@/features/tasks/components/TaskBadges";
import {
  useTaskQuery,
  useUpdateTaskMutation,
} from "@/features/tasks/hooks/useTasksQuery";
import type { TaskPriority, TaskResponse, TaskStatus } from "@/features/tasks/types";
import { normalizeApiError } from "@/lib/api/errors";

interface TaskDetailDrawerProps {
  task: TaskResponse | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const NEXT_STATUS: Record<TaskStatus, { label: string; status: TaskStatus }> = {
  PENDING: { label: "Move to In Progress", status: "IN_PROGRESS" },
  IN_PROGRESS: { label: "Mark Completed", status: "COMPLETED" },
  COMPLETED: { label: "Reopen to Pending", status: "PENDING" },
};

export function TaskDetailDrawer({ task, open, onOpenChange }: TaskDetailDrawerProps) {
  const { toast } = useToast();
  const updateMutation = useUpdateTaskMutation();

  const taskId = task?.id ?? 0;
  const { data: freshTask } = useTaskQuery(open ? taskId : 0);
  const currentTask = freshTask ?? task;

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [status, setStatus] = useState<TaskStatus>("PENDING");
  const [priority, setPriority] = useState<TaskPriority>("MEDIUM");
  const [dueDate, setDueDate] = useState("");
  const [deleteOpen, setDeleteOpen] = useState(false);

  useEffect(() => {
    if (open && currentTask) {
      setTitle(currentTask.title);
      setDescription(currentTask.description ?? "");
      setStatus(currentTask.status);
      setPriority(currentTask.priority);
      setDueDate(currentTask.dueDate ?? "");
    }
  }, [open, currentTask]);

  useEffect(() => {
    if (!open) return;
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !deleteOpen) {
        onOpenChange(false);
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open, deleteOpen, onOpenChange]);

  if (!open || !currentTask) return null;

  const nextTransition = NEXT_STATUS[status];

  const handleSave = async (overrideStatus?: TaskStatus) => {
    const trimmedTitle = title.trim();
    if (!trimmedTitle) {
      toast({ title: "Title is required", variant: "error" });
      return;
    }

    const finalStatus = overrideStatus ?? status;
    try {
      await updateMutation.mutateAsync({
        taskId: currentTask.id,
        payload: {
          title: trimmedTitle,
          description: description.trim() ? description.trim() : null,
          status: finalStatus,
          priority,
          dueDate: dueDate || null,
        },
      });
      if (overrideStatus) {
        setStatus(overrideStatus);
      }
      toast({ title: "Task updated", variant: "success" });
    } catch (err) {
      toast({
        title: "Failed to update task",
        description: normalizeApiError(err).message,
        variant: "error",
      });
    }
  };

  const todayIso = new Date().toISOString().slice(0, 10);
  const isOverdue = Boolean(dueDate && status !== "COMPLETED" && dueDate < todayIso);

  return (
    <>
      <div
        className="fixed inset-0 z-40 bg-surface-container-lowest/70 backdrop-blur-md transition-opacity"
        onClick={() => onOpenChange(false)}
      />

      <aside
        role="dialog"
        aria-modal="true"
        aria-label={`Task details: ${currentTask.title}`}
        className="fixed top-0 right-0 z-50 flex h-screen w-full max-w-[780px] flex-col justify-between border-l border-border bg-surface-container-low shadow-2xl"
      >
        {/* 1. Drawer Header */}
        <header className="flex flex-col gap-3 border-b border-border bg-surface-container/90 px-6 py-4 backdrop-blur-md">
          <div className="flex items-center justify-between gap-4">
            <nav className="flex items-center gap-1.5 text-xs font-medium text-on-surface-variant">
              <span className="hover:text-on-surface">Tasks</span>
              <ChevronRight className="h-3.5 w-3.5" />
              <span className="rounded bg-surface-container-high px-2 py-0.5 font-mono text-[11px] font-semibold text-primary">
                TASK-{currentTask.id}
              </span>
            </nav>

            <div className="flex items-center gap-1.5">
              <button
                type="button"
                onClick={() => setDeleteOpen(true)}
                className="rounded-lg p-2 text-on-surface-variant transition-colors hover:bg-error-container/30 hover:text-error cursor-pointer"
                title="Delete task"
                aria-label="Delete task"
              >
                <Trash2 className="h-4 w-4" />
              </button>
              <div className="mx-1 h-4 w-px bg-outline-variant/40" />
              <button
                type="button"
                onClick={() => onOpenChange(false)}
                className="group rounded-lg p-2 text-on-surface-variant transition-colors hover:bg-surface-container-high hover:text-on-surface cursor-pointer"
                title="Close drawer (Esc)"
                aria-label="Close drawer"
              >
                <X className="h-4 w-4 transition-transform duration-200 group-hover:rotate-90" />
              </button>
            </div>
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap items-center gap-2">
              <TaskStatusBadge status={status} />
              <ArrowRight className="h-3.5 w-3.5 text-outline" />
              <button
                type="button"
                disabled={updateMutation.isPending}
                onClick={() => handleSave(nextTransition.status)}
                className="inline-flex items-center gap-1.5 rounded-full bg-surface-container px-2.5 py-1 text-[11px] font-semibold text-on-surface-variant transition-colors hover:bg-surface-container-high hover:text-primary cursor-pointer"
              >
                <span className="h-1.5 w-1.5 rounded-full bg-primary" />
                {nextTransition.label}
              </button>
            </div>

            <span className="text-[11px] text-on-surface-variant tabular-nums">
              Created {format(parseISO(currentTask.createdAt), "MMM d, yyyy")}
            </span>
          </div>
        </header>

        {/* 2. Scrollable Body */}
        <div className="flex-1 space-y-6 overflow-y-auto px-6 py-6">
          {/* Editable Large Title */}
          <div>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Task title..."
              maxLength={150}
              className="w-full rounded-lg bg-transparent px-2 py-1.5 -mx-2 font-display text-2xl font-semibold tracking-tight text-on-surface placeholder:text-outline focus:bg-surface-container/60 focus:outline-none sm:text-3xl"
            />
          </div>

          {/* Metadata Bento Grid */}
          <div className="rounded-xl border border-border bg-surface-container p-4 space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              {/* Status */}
              <div className="space-y-1.5">
                <label
                  htmlFor="drawer-status"
                  className="block text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant"
                >
                  Status
                </label>
                <Select
                  id="drawer-status"
                  value={status}
                  onChange={(e) => setStatus(e.target.value as TaskStatus)}
                  className="bg-surface-container-low"
                >
                  <option value="PENDING">Pending</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                </Select>
              </div>

              {/* Priority */}
              <div className="space-y-1.5">
                <label
                  htmlFor="drawer-priority"
                  className="flex items-center gap-1 text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant"
                >
                  <Flag className="h-3 w-3" /> Priority
                </label>
                <Select
                  id="drawer-priority"
                  value={priority}
                  onChange={(e) => setPriority(e.target.value as TaskPriority)}
                  className="bg-surface-container-low"
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="URGENT">Urgent</option>
                </Select>
              </div>

              {/* Due Date */}
              <div className="space-y-1.5">
                <label
                  htmlFor="drawer-dueDate"
                  className="flex items-center gap-1 text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant"
                >
                  <Calendar className="h-3 w-3" /> Due Date
                </label>
                <Input
                  id="drawer-dueDate"
                  type="date"
                  value={dueDate}
                  onChange={(e) => setDueDate(e.target.value)}
                  className="bg-surface-container-low"
                />
                {isOverdue ? (
                  <p className="text-[11px] font-medium text-error">Overdue</p>
                ) : null}
              </div>
            </div>

            <div className="h-px w-full bg-surface-container-highest" />

            <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-on-surface-variant">
              <div className="flex items-center gap-2">
                <span>Current Priority:</span>
                <TaskPriorityBadge priority={priority} />
              </div>
              <span className="tabular-nums">
                Last updated {format(parseISO(currentTask.updatedAt), "MMM d, yyyy · h:mm a")}
              </span>
            </div>
          </div>

          {/* Description Editor */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label
                htmlFor="drawer-description"
                className="flex items-center gap-1.5 text-sm font-semibold text-on-surface"
              >
                <AlignLeft className="h-4 w-4 text-primary" />
                Description
              </label>
              <span className="text-[11px] text-on-surface-variant tabular-nums">
                {description.length}/2000
              </span>
            </div>
            <div className="overflow-hidden rounded-xl border border-border bg-surface-container">
              <Textarea
                id="drawer-description"
                rows={7}
                maxLength={2000}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Add technical details, acceptance criteria, or notes..."
                className="border-0 bg-transparent p-4 text-sm leading-relaxed focus-visible:ring-0"
              />
            </div>
          </div>
        </div>

        {/* 3. Drawer Footer */}
        <footer className="flex items-center justify-between border-t border-border bg-surface-container/70 px-6 py-4 backdrop-blur-md">
          <Button
            type="button"
            variant="ghost"
            className="text-error hover:bg-error-container/30 hover:text-error"
            onClick={() => setDeleteOpen(true)}
            disabled={updateMutation.isPending}
          >
            <Trash2 className="h-4 w-4" />
            Delete Task
          </Button>

          <div className="flex items-center gap-2.5">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={updateMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              type="button"
              onClick={() => handleSave()}
              disabled={updateMutation.isPending}
            >
              {updateMutation.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Save className="h-4 w-4" />
              )}
              Save Changes
            </Button>
          </div>
        </footer>
      </aside>

      <DeleteTaskDialog
        open={deleteOpen}
        onOpenChange={(isOpen) => {
          setDeleteOpen(isOpen);
          if (!isOpen && !currentTask) {
            onOpenChange(false);
          }
        }}
        task={currentTask}
        onDeleted={() => onOpenChange(false)}
      />
    </>
  );
}
