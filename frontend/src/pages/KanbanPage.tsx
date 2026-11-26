import { useState } from "react";
import { format, parseISO } from "date-fns";
import {
  Calendar,
  Edit2,
  Kanban,
  List,
  Loader2,
  Plus,
  Search,
  Trash2,
} from "lucide-react";
import { Link } from "react-router-dom";
import { Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/components/ui/toast";
import { DeleteTaskDialog } from "@/features/tasks/components/DeleteTaskDialog";
import { TaskPriorityBadge } from "@/features/tasks/components/TaskBadges";
import { TaskDetailDrawer } from "@/features/tasks/components/TaskDetailDrawer";
import { TaskFormDialog } from "@/features/tasks/components/TaskFormDialog";
import {
  useTasksQuery,
  useUpdateTaskMutation,
} from "@/features/tasks/hooks/useTasksQuery";
import type {
  TaskPriority,
  TaskResponse,
  TaskStatus,
  UpdateTaskRequest,
} from "@/features/tasks/types";
import { normalizeApiError } from "@/lib/api/errors";
import { cn } from "@/lib/utils";

interface ColumnDef {
  status: TaskStatus;
  label: string;
  dotClass: string;
}

const COLUMNS: ColumnDef[] = [
  {
    status: "PENDING",
    label: "Pending",
    dotClass: "bg-primary-container shadow-[0_0_8px_rgba(128,131,255,0.6)]",
  },
  {
    status: "IN_PROGRESS",
    label: "In Progress",
    dotClass: "bg-secondary shadow-[0_0_8px_rgba(173,198,255,0.6)]",
  },
  {
    status: "COMPLETED",
    label: "Completed",
    dotClass: "bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.6)]",
  },
];

const MOVE_OPTIONS: Record<TaskStatus, { label: string; status: TaskStatus }[]> = {
  PENDING: [
    { label: "→ In Progress", status: "IN_PROGRESS" },
    { label: "→ Completed", status: "COMPLETED" },
  ],
  IN_PROGRESS: [
    { label: "→ Pending", status: "PENDING" },
    { label: "→ Completed", status: "COMPLETED" },
  ],
  COMPLETED: [
    { label: "→ Pending", status: "PENDING" },
    { label: "→ In Progress", status: "IN_PROGRESS" },
  ],
};

const PRIORITY_FILTERS: { value: TaskPriority | ""; label: string }[] = [
  { value: "", label: "All" },
  { value: "URGENT", label: "Urgent" },
  { value: "HIGH", label: "High" },
  { value: "MEDIUM", label: "Medium" },
  { value: "LOW", label: "Low" },
];

export default function KanbanPage() {
  const { toast } = useToast();
  const updateMutation = useUpdateTaskMutation();

  const [search, setSearch] = useState("");
  const [priorityFilter, setPriorityFilter] = useState<TaskPriority | "">("");
  const [createOpen, setCreateOpen] = useState(false);
  const [drawerTask, setDrawerTask] = useState<TaskResponse | null>(null);
  const [deleteTask, setDeleteTask] = useState<TaskResponse | null>(null);
  const [movingId, setMovingId] = useState<number | null>(null);

  const sharedParams = {
    page: 0,
    size: 50,
    search: search.trim() || undefined,
    priority: priorityFilter || undefined,
  };

  const pendingQ = useTasksQuery({ ...sharedParams, status: "PENDING" });
  const inProgressQ = useTasksQuery({ ...sharedParams, status: "IN_PROGRESS" });
  const completedQ = useTasksQuery({ ...sharedParams, status: "COMPLETED" });

  const columnData: Record<
    TaskStatus,
    { tasks: TaskResponse[]; total: number; isLoading: boolean }
  > = {
    PENDING: {
      tasks: pendingQ.data?.content ?? [],
      total: pendingQ.data?.totalElements ?? 0,
      isLoading: pendingQ.isLoading,
    },
    IN_PROGRESS: {
      tasks: inProgressQ.data?.content ?? [],
      total: inProgressQ.data?.totalElements ?? 0,
      isLoading: inProgressQ.isLoading,
    },
    COMPLETED: {
      tasks: completedQ.data?.content ?? [],
      total: completedQ.data?.totalElements ?? 0,
      isLoading: completedQ.isLoading,
    },
  };

  const totalAcrossBoard =
    columnData.PENDING.total + columnData.IN_PROGRESS.total + columnData.COMPLETED.total;
  const completedPct =
    totalAcrossBoard > 0
      ? Math.round((columnData.COMPLETED.total / totalAcrossBoard) * 100)
      : 0;
  const todayIso = new Date().toISOString().slice(0, 10);

  async function handleMove(task: TaskResponse, newStatus: TaskStatus) {
    setMovingId(task.id);
    const payload: UpdateTaskRequest = {
      title: task.title,
      description: task.description,
      status: newStatus,
      priority: task.priority,
      dueDate: task.dueDate,
    };
    try {
      await updateMutation.mutateAsync({ taskId: task.id, payload });
      toast({
        title: "Task moved",
        description: `"${task.title}" → ${newStatus.replace("_", " ")}`,
        variant: "success",
      });
    } catch (err) {
      toast({
        title: "Move failed",
        description: normalizeApiError(err).message,
        variant: "error",
      });
    } finally {
      setMovingId(null);
    }
  }

  return (
    <div className="relative space-y-6">
      {/* Ambient background glows */}
      <div className="pointer-events-none absolute -top-12 left-1/4 h-72 w-72 rounded-full bg-primary-container/10 blur-3xl opacity-60" />
      <div className="pointer-events-none absolute top-40 right-10 h-72 w-72 rounded-full bg-tertiary-container/10 blur-3xl opacity-50" />

      {/* 1. Board Header */}
      <header className="relative z-10 space-y-4">
        <div className="flex flex-col justify-between gap-4 lg:flex-row lg:items-center">
          <div className="space-y-1">
            <div className="flex items-center gap-2 text-[11px] font-semibold">
              <span className="text-primary">Workspace</span>
              <span className="text-outline-variant">/</span>
              <span className="text-on-surface-variant">Kanban Board</span>
              <span className="ml-1 inline-flex items-center gap-1 rounded-full bg-secondary-container/20 px-2 py-0.5 text-secondary">
                <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-secondary" />
                Live Sync
              </span>
            </div>

            <h1 className="font-display text-2xl font-semibold tracking-tight text-on-surface sm:text-3xl">
              Task Workflow Board
            </h1>

            <div className="flex flex-wrap items-center gap-x-3 gap-y-1 pt-0.5 text-xs text-on-surface-variant">
              <span className="tabular-nums">{totalAcrossBoard} Total Tasks</span>
              <span className="h-1 w-1 rounded-full bg-outline-variant" />
              <div className="flex items-center gap-2 rounded-md bg-surface-container px-2.5 py-0.5">
                <span className="text-[11px] font-medium text-on-surface tabular-nums">
                  {completedPct}% completed
                </span>
                <div className="h-1.5 w-16 overflow-hidden rounded-full bg-surface-container-highest">
                  <div
                    className="h-full rounded-full bg-primary-container transition-all"
                    style={{ width: `${completedPct}%` }}
                  />
                </div>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2.5 self-start lg:self-center">
            <div className="flex items-center rounded-lg bg-surface-container-low p-1 border border-border">
              <span className="flex items-center gap-1 rounded bg-surface-container-high px-2.5 py-1 text-xs font-medium text-on-surface shadow-sm">
                <Kanban className="h-3.5 w-3.5" />
                Board
              </span>
              <Link
                to="/tasks"
                className="flex items-center gap-1 rounded px-2.5 py-1 text-xs font-medium text-on-surface-variant transition-colors hover:text-on-surface"
              >
                <List className="h-3.5 w-3.5" />
                List
              </Link>
            </div>

            <button
              type="button"
              onClick={() => setCreateOpen(true)}
              className="inline-flex items-center gap-1.5 rounded-lg bg-primary-container px-3.5 py-2 text-xs font-semibold text-on-primary-container shadow-md transition-all hover:bg-primary hover:text-primary-foreground cursor-pointer"
            >
              <Plus className="h-4 w-4" />
              <span>Add Task</span>
            </button>
          </div>
        </div>

        {/* Filter Bar */}
        <div className="flex flex-col justify-between gap-3 rounded-xl border border-border bg-surface-container-low/90 p-3 backdrop-blur-md md:flex-row md:items-center">
          <div className="flex flex-wrap items-center gap-3">
            <div className="relative min-w-[220px] flex-1 sm:flex-initial">
              <Search className="pointer-events-none absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-on-surface-variant" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Filter board tasks..."
                className="h-8 w-full rounded-lg bg-surface-container pl-8 pr-3 text-xs text-on-surface placeholder:text-on-surface-variant transition-colors focus:bg-surface-container-high focus:outline-none"
              />
            </div>

            <div className="flex flex-wrap items-center gap-1">
              <span className="mr-1 text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
                Priority:
              </span>
              {PRIORITY_FILTERS.map((f) => {
                const active = priorityFilter === f.value;
                return (
                  <button
                    key={f.label}
                    type="button"
                    onClick={() => setPriorityFilter(f.value)}
                    className={cn(
                      "rounded-md px-2.5 py-1 text-[11px] font-medium transition-colors cursor-pointer",
                      active
                        ? "bg-primary-container text-on-primary-container font-semibold"
                        : "bg-surface-container text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
                    )}
                  >
                    {f.label}
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      </header>

      {/* 2. Three-Column Kanban Grid */}
      <div className="relative z-10 grid grid-cols-1 items-start gap-5 md:grid-cols-2 lg:grid-cols-3">
        {COLUMNS.map((col) => {
          const { tasks, total, isLoading } = columnData[col.status];
          return (
            <section
              key={col.status}
              className="flex min-h-[540px] flex-col rounded-2xl border border-border bg-surface-container-low/70 p-3 shadow-lg backdrop-blur-xl"
            >
              {/* Column Header */}
              <div className="flex items-center justify-between px-2 pt-1 pb-3">
                <div className="flex items-center gap-2.5">
                  <span className={cn("h-2.5 w-2.5 rounded-full", col.dotClass)} />
                  <h2 className="font-display text-base font-semibold tracking-tight text-on-surface">
                    {col.label}
                  </h2>
                  {!isLoading ? (
                    <span className="rounded-full bg-surface-container px-2 py-0.5 text-[11px] font-medium text-on-surface-variant tabular-nums">
                      {total}
                    </span>
                  ) : null}
                </div>
              </div>

              {/* Quick Add Trigger (POST /tasks creates PENDING) */}
              {col.status === "PENDING" ? (
                <button
                  type="button"
                  onClick={() => setCreateOpen(true)}
                  className="group mb-3 flex w-full items-center justify-center gap-1.5 rounded-xl bg-surface-container/60 py-2 text-xs font-medium text-on-surface-variant shadow-sm transition-all hover:bg-surface-container hover:text-on-surface cursor-pointer"
                >
                  <Plus className="h-3.5 w-3.5 transition-transform group-hover:scale-110" />
                  <span>Quick Add Task</span>
                </button>
              ) : null}

              {/* Cards Stack */}
              <div className="flex flex-col space-y-3">
                {isLoading ? (
                  Array.from({ length: 3 }).map((_, i) => (
                    <Skeleton key={i} className="h-32 w-full rounded-xl" />
                  ))
                ) : tasks.length === 0 ? (
                  <div className="rounded-xl border border-dashed border-border/70 py-10 text-center text-xs text-on-surface-variant">
                    No {col.label.toLowerCase()} tasks
                  </div>
                ) : (
                  tasks.map((task) => {
                    const isOverdue = Boolean(
                      task.dueDate && task.status !== "COMPLETED" && task.dueDate < todayIso,
                    );
                    const isMoving = movingId === task.id;
                    const moveOptions = MOVE_OPTIONS[task.status];

                    return (
                      <article
                        key={task.id}
                        onClick={() => setDrawerTask(task)}
                        className="group relative flex flex-col rounded-xl border border-border bg-surface-container p-4 shadow-md transition-all duration-200 hover:-translate-y-0.5 hover:bg-surface-container-high cursor-pointer"
                      >
                        <div className="flex items-center justify-between pb-2">
                          <span className="font-mono text-[11px] font-semibold text-primary">
                            TASK-{task.id}
                          </span>
                          <div className="flex items-center gap-1">
                            <TaskPriorityBadge priority={task.priority} />
                          </div>
                        </div>

                        <h3
                          className={cn(
                            "font-display text-sm font-semibold leading-snug line-clamp-2 transition-colors group-hover:text-primary",
                            task.status === "COMPLETED"
                              ? "text-on-surface-variant line-through"
                              : "text-on-surface",
                          )}
                        >
                          {task.title}
                        </h3>

                        {task.description ? (
                          <p className="mt-1.5 line-clamp-2 text-xs text-on-surface-variant">
                            {task.description}
                          </p>
                        ) : null}

                        <div className="mt-3 flex items-center justify-between pt-2 border-t border-border/50">
                          {task.dueDate ? (
                            <span
                              className={cn(
                                "inline-flex items-center gap-1 rounded px-1.5 py-0.5 text-[11px] tabular-nums",
                                isOverdue
                                  ? "bg-error-container/30 font-medium text-error"
                                  : "bg-surface-container-highest/60 text-on-surface-variant",
                              )}
                            >
                              <Calendar className="h-3 w-3" />
                              {format(parseISO(task.dueDate), "MMM d")}
                              {isOverdue ? " · Overdue" : ""}
                            </span>
                          ) : (
                            <span className="text-[11px] text-on-surface-variant">No due date</span>
                          )}

                          <div
                            className="flex items-center gap-1 opacity-0 transition-opacity group-hover:opacity-100"
                            onClick={(e) => e.stopPropagation()}
                          >
                            <button
                              type="button"
                              onClick={() => setDrawerTask(task)}
                              className="rounded p-1 text-on-surface-variant hover:bg-surface-bright hover:text-on-surface cursor-pointer"
                              aria-label={`Inspect "${task.title}"`}
                            >
                              <Edit2 className="h-3.5 w-3.5" />
                            </button>
                            <button
                              type="button"
                              onClick={() => setDeleteTask(task)}
                              className="rounded p-1 text-error hover:bg-error-container/30 cursor-pointer"
                              aria-label={`Delete "${task.title}"`}
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                            </button>
                          </div>
                        </div>

                        {/* Inline Move Actions */}
                        <div
                          className="mt-2.5 flex flex-wrap gap-1.5"
                          onClick={(e) => e.stopPropagation()}
                        >
                          {isMoving ? (
                            <span className="inline-flex items-center gap-1 text-[11px] text-on-surface-variant">
                              <Loader2 className="h-3 w-3 animate-spin" /> Moving...
                            </span>
                          ) : (
                            moveOptions.map((opt) => (
                              <button
                                key={opt.status}
                                type="button"
                                onClick={() => handleMove(task, opt.status)}
                                className="rounded-md border border-border bg-surface-container-low px-2 py-0.5 text-[11px] text-on-surface-variant transition-colors hover:border-primary/40 hover:bg-primary/10 hover:text-primary cursor-pointer"
                              >
                                {opt.label}
                              </button>
                            ))
                          )}
                        </div>
                      </article>
                    );
                  })
                )}
              </div>
            </section>
          );
        })}
      </div>

      <TaskFormDialog open={createOpen} onOpenChange={setCreateOpen} />
      <TaskDetailDrawer
        task={drawerTask}
        open={Boolean(drawerTask)}
        onOpenChange={(isOpen) => {
          if (!isOpen) setDrawerTask(null);
        }}
      />
      <DeleteTaskDialog
        open={Boolean(deleteTask)}
        onOpenChange={(isOpen) => {
          if (!isOpen) setDeleteTask(null);
        }}
        task={deleteTask}
      />
    </div>
  );
}
