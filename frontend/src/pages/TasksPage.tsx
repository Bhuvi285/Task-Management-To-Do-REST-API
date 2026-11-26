import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { format, parseISO } from "date-fns";
import {
  AlertCircle,
  Calendar,
  CheckCircle2,
  Clock,
  Edit2,
  FilterX,
  LayoutGrid,
  List,
  Plus,
  RefreshCw,
  Search,
  SortAsc,
  SortDesc,
  Trash2,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/components/ui/toast";
import { DeleteTaskDialog } from "@/features/tasks/components/DeleteTaskDialog";
import { PaginationBar } from "@/features/tasks/components/PaginationBar";
import { TaskPriorityBadge, TaskStatusBadge } from "@/features/tasks/components/TaskBadges";
import { TaskCard, TaskCardSkeleton } from "@/features/tasks/components/TaskCard";
import { TaskDetailDrawer } from "@/features/tasks/components/TaskDetailDrawer";
import { TaskFormDialog } from "@/features/tasks/components/TaskFormDialog";
import {
  useDeleteTaskMutation,
  useTasksQuery,
  useUpdateTaskMutation,
} from "@/features/tasks/hooks/useTasksQuery";
import type {
  SortField,
  TaskPriority,
  TaskQueryParams,
  TaskResponse,
  TaskStatus,
} from "@/features/tasks/types";
import { normalizeApiError } from "@/lib/api/errors";
import { cn } from "@/lib/utils";

const PAGE_SIZE = 12;

const PRIORITY_PILLS: { value: TaskPriority | ""; label: string; dotClass?: string }[] = [
  { value: "", label: "All" },
  { value: "URGENT", label: "Urgent", dotClass: "bg-error" },
  { value: "HIGH", label: "High", dotClass: "bg-primary" },
  { value: "MEDIUM", label: "Medium", dotClass: "bg-secondary" },
  { value: "LOW", label: "Low", dotClass: "bg-outline" },
];

export function TasksPage() {
  const { toast } = useToast();
  const searchInputRef = useRef<HTMLInputElement>(null);

  const [params, setParams] = useState<TaskQueryParams>({
    page: 0,
    size: PAGE_SIZE,
    sortBy: "createdAt",
    direction: "desc",
    search: "",
    status: "",
    priority: "",
  });
  const [draftSearch, setDraftSearch] = useState("");
  const [viewMode, setViewMode] = useState<"list" | "grid">("list");
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [createOpen, setCreateOpen] = useState(false);
  const [drawerTask, setDrawerTask] = useState<TaskResponse | null>(null);
  const [deletingTask, setDeletingTask] = useState<TaskResponse | null>(null);
  const [isBatchPending, setIsBatchPending] = useState(false);

  const { data, isLoading, isError, error, refetch, isFetching } = useTasksQuery(params);
  const updateMutation = useUpdateTaskMutation();
  const deleteMutation = useDeleteTaskMutation();

  const tasks = data?.content ?? [];
  const todayIso = new Date().toISOString().slice(0, 10);

  // Keyboard shortcuts: "/" focuses search, "n" opens New Task
  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      const target = e.target as HTMLElement | null;
      const isTyping =
        target?.tagName === "INPUT" ||
        target?.tagName === "TEXTAREA" ||
        target?.tagName === "SELECT" ||
        target?.isContentEditable;
      if (isTyping || e.metaKey || e.ctrlKey || e.altKey) return;

      if (e.key === "/") {
        e.preventDefault();
        searchInputRef.current?.focus();
      } else if (e.key.toLowerCase() === "n") {
        e.preventDefault();
        setCreateOpen(true);
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, []);

  const commitSearch = useCallback(
    (value: string) => setParams((p) => ({ ...p, search: value.trim(), page: 0 })),
    [],
  );

  const clearFilters = () => {
    setDraftSearch("");
    setParams({
      page: 0,
      size: PAGE_SIZE,
      sortBy: "createdAt",
      direction: "desc",
      search: "",
      status: "",
      priority: "",
    });
  };

  const hasActiveFilters =
    Boolean(params.search) || Boolean(params.status) || Boolean(params.priority);

  const toggleSelect = (id: number) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const handleBatchMarkDone = async () => {
    const selectedTasks = tasks.filter((t) => selectedIds.has(t.id) && t.status !== "COMPLETED");
    if (selectedTasks.length === 0) {
      setSelectedIds(new Set());
      return;
    }
    setIsBatchPending(true);
    try {
      await Promise.all(
        selectedTasks.map((t) =>
          updateMutation.mutateAsync({
            taskId: t.id,
            payload: {
              title: t.title,
              description: t.description,
              status: "COMPLETED",
              priority: t.priority,
              dueDate: t.dueDate,
            },
          }),
        ),
      );
      toast({
        title: `${selectedTasks.length} task${selectedTasks.length > 1 ? "s" : ""} marked completed`,
        variant: "success",
      });
      setSelectedIds(new Set());
    } catch (err) {
      toast({
        title: "Batch update failed",
        description: normalizeApiError(err).message,
        variant: "error",
      });
    } finally {
      setIsBatchPending(false);
    }
  };

  const handleBatchDelete = async () => {
    const ids = Array.from(selectedIds);
    if (ids.length === 0) return;
    setIsBatchPending(true);
    try {
      await Promise.all(ids.map((id) => deleteMutation.mutateAsync(id)));
      toast({
        title: `${ids.length} task${ids.length > 1 ? "s" : ""} deleted`,
        variant: "success",
      });
      setSelectedIds(new Set());
    } catch (err) {
      toast({
        title: "Batch delete failed",
        description: normalizeApiError(err).message,
        variant: "error",
      });
    } finally {
      setIsBatchPending(false);
    }
  };

  // Group tasks for Stitch List View
  const groups = useMemo(() => {
    const overdue: TaskResponse[] = [];
    const dueToday: TaskResponse[] = [];
    const upcoming: TaskResponse[] = [];
    const completed: TaskResponse[] = [];

    for (const task of tasks) {
      if (task.status === "COMPLETED") {
        completed.push(task);
      } else if (task.dueDate && task.dueDate < todayIso) {
        overdue.push(task);
      } else if (task.dueDate === todayIso) {
        dueToday.push(task);
      } else {
        upcoming.push(task);
      }
    }

    return [
      {
        key: "overdue",
        label: "Overdue",
        subtitle: "Requires immediate attention",
        icon: AlertCircle,
        titleClass: "text-error",
        badgeClass: "bg-error-container/40 text-error",
        items: overdue,
      },
      {
        key: "dueToday",
        label: "Due Today",
        subtitle: "Scheduled for today",
        icon: Clock,
        titleClass: "text-primary",
        badgeClass: "bg-primary-container/20 text-primary",
        items: dueToday,
      },
      {
        key: "upcoming",
        label: "Active & Upcoming",
        subtitle: "Open workspace tasks",
        icon: Calendar,
        titleClass: "text-on-surface",
        badgeClass: "bg-surface-container-high text-on-surface-variant",
        items: upcoming,
      },
      {
        key: "completed",
        label: "Completed",
        subtitle: "Finished tasks",
        icon: CheckCircle2,
        titleClass: "text-emerald-500",
        badgeClass: "bg-emerald-500/15 text-emerald-500",
        items: completed,
      },
    ].filter((g) => g.items.length > 0);
  }, [tasks, todayIso]);

  return (
    <div className="space-y-5">
      {/* 1. Header Bar */}
      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-end">
        <div className="space-y-1">
          <div className="flex items-center gap-2 text-[11px] font-semibold uppercase tracking-wider text-primary">
            <span>Workspace Hub</span>
          </div>
          <div className="flex flex-wrap items-baseline gap-3">
            <h1 className="font-display text-3xl font-semibold tracking-tight text-on-surface sm:text-4xl">
              My Tasks
            </h1>
            {data ? (
              <div className="inline-flex items-center gap-1.5 rounded-full bg-surface-container-high px-2.5 py-0.5 shadow-inner">
                <span className="h-2 w-2 animate-pulse rounded-full bg-primary" />
                <span className="text-[11px] font-medium text-on-surface-variant tabular-nums">
                  {data.totalElements} task{data.totalElements !== 1 ? "s" : ""}
                </span>
              </div>
            ) : null}
          </div>
        </div>

        {/* Right Controls: View Switcher & New Task */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="inline-flex rounded-xl bg-surface-container p-1 shadow-inner">
            <button
              type="button"
              onClick={() => setViewMode("list")}
              className={cn(
                "flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-all cursor-pointer",
                viewMode === "list"
                  ? "bg-surface-container-highest text-on-surface shadow-sm"
                  : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
              )}
            >
              <List className="h-4 w-4" />
              <span>List</span>
            </button>
            <button
              type="button"
              onClick={() => setViewMode("grid")}
              className={cn(
                "flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-all cursor-pointer",
                viewMode === "grid"
                  ? "bg-surface-container-highest text-on-surface shadow-sm"
                  : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
              )}
            >
              <LayoutGrid className="h-4 w-4" />
              <span>Grid</span>
            </button>
          </div>

          <button
            type="button"
            onClick={() => setCreateOpen(true)}
            className="group inline-flex items-center gap-2 rounded-xl bg-primary px-4 py-2 text-xs font-semibold text-primary-foreground shadow-[0_0_20px_rgba(192,193,255,0.25)] transition-all hover:brightness-110 cursor-pointer"
          >
            <Plus className="h-4 w-4 transition-transform group-hover:rotate-90" />
            <span>New Task</span>
            <kbd className="hidden rounded bg-primary-foreground/15 px-1.5 py-0.5 font-mono text-[11px] sm:inline-block">
              N
            </kbd>
          </button>
        </div>
      </div>

      {/* 2. Comprehensive Filter & Query Toolbar */}
      <div className="space-y-3 rounded-2xl border border-border bg-surface-container-low p-4 shadow-sm">
        <div className="grid grid-cols-1 items-center gap-3 lg:grid-cols-12">
          {/* Search Input */}
          <div className="relative flex items-center lg:col-span-5">
            <Search className="pointer-events-none absolute left-3.5 h-4 w-4 text-on-surface-variant" />
            <input
              ref={searchInputRef}
              type="text"
              value={draftSearch}
              onChange={(e) => {
                setDraftSearch(e.target.value);
                if (e.target.value === "") commitSearch("");
              }}
              onKeyDown={(e) => {
                if (e.key === "Enter") commitSearch(draftSearch);
              }}
              placeholder="Search tasks by title or description (Enter)..."
              className="h-10 w-full rounded-xl bg-surface-container pl-10 pr-10 text-xs text-on-surface placeholder:text-on-surface-variant shadow-inner transition-colors focus:bg-surface-container-high focus:outline-none"
            />
            <kbd className="absolute right-3 rounded bg-surface-container-high px-1.5 py-0.5 font-mono text-[11px] text-on-surface-variant">
              /
            </kbd>
          </div>

          {/* Selectors */}
          <div className="flex flex-wrap items-center justify-start gap-2 lg:col-span-7 lg:justify-end">
            <Select
              aria-label="Filter by status"
              value={params.status ?? ""}
              onChange={(e) =>
                setParams((p) => ({ ...p, status: e.target.value as TaskStatus | "", page: 0 }))
              }
              className="h-10 w-auto min-w-[140px] rounded-xl bg-surface-container text-xs"
            >
              <option value="">Status: All</option>
              <option value="PENDING">Pending</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
            </Select>

            <Select
              aria-label="Sort by field"
              value={params.sortBy ?? "createdAt"}
              onChange={(e) =>
                setParams((p) => ({ ...p, sortBy: e.target.value as SortField, page: 0 }))
              }
              className="h-10 w-auto min-w-[140px] rounded-xl bg-surface-container text-xs"
            >
              <option value="createdAt">Sort: Created</option>
              <option value="dueDate">Sort: Due Date</option>
              <option value="priority">Sort: Priority</option>
              <option value="status">Sort: Status</option>
              <option value="title">Sort: Title</option>
              <option value="updatedAt">Sort: Updated</option>
            </Select>

            <Button
              variant="outline"
              size="icon"
              className="h-10 w-10 rounded-xl bg-surface-container"
              onClick={() =>
                setParams((p) => ({
                  ...p,
                  direction: p.direction === "asc" ? "desc" : "asc",
                  page: 0,
                }))
              }
              title={`Sort direction: ${params.direction === "asc" ? "Ascending" : "Descending"}`}
              aria-label="Toggle sort direction"
            >
              {params.direction === "asc" ? (
                <SortAsc className="h-4 w-4" />
              ) : (
                <SortDesc className="h-4 w-4" />
              )}
            </Button>

            <Button
              variant="outline"
              size="icon"
              className="h-10 w-10 rounded-xl bg-surface-container"
              onClick={() => void refetch()}
              disabled={isFetching}
              title="Refresh tasks"
              aria-label="Refresh tasks"
            >
              <RefreshCw className={cn("h-4 w-4", isFetching && "animate-spin")} />
            </Button>

            {hasActiveFilters ? (
              <Button
                variant="outline"
                size="icon"
                className="h-10 w-10 rounded-xl bg-surface-container text-error hover:text-error"
                onClick={clearFilters}
                title="Clear filters"
                aria-label="Clear filters"
              >
                <FilterX className="h-4 w-4" />
              </Button>
            ) : null}
          </div>
        </div>

        {/* Bottom Row: Priority Filter Pills + Batch Actions */}
        <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
          <div className="flex flex-wrap items-center gap-1.5">
            <span className="mr-1 text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
              Priority:
            </span>
            {PRIORITY_PILLS.map((pill) => {
              const active = (params.priority ?? "") === pill.value;
              return (
                <button
                  key={pill.label}
                  type="button"
                  onClick={() => setParams((p) => ({ ...p, priority: pill.value, page: 0 }))}
                  className={cn(
                    "inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-[11px] font-semibold transition-all cursor-pointer",
                    active
                      ? "bg-primary-container text-on-primary-container shadow-sm"
                      : "bg-surface-container text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
                  )}
                >
                  {pill.dotClass ? (
                    <span className={cn("h-1.5 w-1.5 rounded-full", pill.dotClass)} />
                  ) : null}
                  <span>{pill.label}</span>
                </button>
              );
            })}
          </div>

          {selectedIds.size > 0 ? (
            <div className="flex items-center gap-2 rounded-xl bg-surface-container-high px-3 py-1.5 shadow-lg">
              <span className="text-xs font-semibold text-on-surface tabular-nums">
                {selectedIds.size} selected
              </span>
              <div className="h-4 w-px bg-outline-variant/50" />
              <button
                type="button"
                disabled={isBatchPending}
                onClick={handleBatchMarkDone}
                className="inline-flex items-center gap-1 rounded-lg bg-surface-container px-2.5 py-1 text-[11px] font-medium text-on-surface transition-colors hover:bg-surface-bright cursor-pointer"
              >
                <CheckCircle2 className="h-3.5 w-3.5 text-emerald-500" />
                Mark Done
              </button>
              <button
                type="button"
                disabled={isBatchPending}
                onClick={handleBatchDelete}
                className="inline-flex items-center gap-1 rounded-lg p-1 text-error transition-colors hover:bg-error-container/30 cursor-pointer"
                title="Delete selected tasks"
              >
                <Trash2 className="h-3.5 w-3.5" />
              </button>
            </div>
          ) : null}
        </div>
      </div>

      {/* 3. Content Area */}
      {isError ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-error/30 bg-surface-container-low p-10 text-center">
          <AlertCircle className="mb-3 h-8 w-8 text-error" />
          <h3 className="font-display text-base font-semibold text-on-surface">
            Failed to load tasks
          </h3>
          <p className="mt-1 text-xs text-on-surface-variant">
            {normalizeApiError(error).message}
          </p>
          <Button variant="outline" size="sm" className="mt-4" onClick={() => void refetch()}>
            Retry
          </Button>
        </div>
      ) : isLoading ? (
        viewMode === "grid" ? (
          <div className="grid gap-4 sm:grid-cols-2">
            {Array.from({ length: 6 }).map((_, i) => (
              <TaskCardSkeleton key={i} />
            ))}
          </div>
        ) : (
          <div className="space-y-2.5">
            {Array.from({ length: 6 }).map((_, i) => (
              <Skeleton key={i} className="h-16 w-full rounded-xl" />
            ))}
          </div>
        )
      ) : tasks.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-2xl border border-border bg-surface-container-low p-12 text-center">
          <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-surface-container-high text-primary">
            <CheckCircle2 className="h-6 w-6" />
          </div>
          <h3 className="font-display text-base font-semibold text-on-surface">
            {hasActiveFilters ? "No tasks match your filters" : "Zero tasks remaining"}
          </h3>
          <p className="mt-1 max-w-sm text-xs text-on-surface-variant">
            {hasActiveFilters
              ? "Try clearing or adjusting your search query, status, or priority filters."
              : "Create your first task to start organizing your workflow."}
          </p>
          <div className="mt-4 flex gap-2">
            {hasActiveFilters ? (
              <Button variant="outline" size="sm" onClick={clearFilters}>
                Clear Filters
              </Button>
            ) : (
              <Button size="sm" onClick={() => setCreateOpen(true)}>
                <Plus className="h-4 w-4" />
                Create Task
              </Button>
            )}
          </div>
        </div>
      ) : viewMode === "grid" ? (
        <div className="grid gap-4 sm:grid-cols-2">
          {tasks.map((task) => (
            <div key={task.id} onClick={() => setDrawerTask(task)} className="cursor-pointer">
              <TaskCard
                task={task}
                onEdit={(t) => setDrawerTask(t)}
                onDelete={(t) => setDeletingTask(t)}
              />
            </div>
          ))}
        </div>
      ) : (
        <div className="space-y-6">
          {groups.map((group) => {
            const GroupIcon = group.icon;
            return (
              <section key={group.key} className="space-y-2">
                <div className="flex items-center justify-between px-2 py-1">
                  <div className="flex items-center gap-2">
                    <GroupIcon className={cn("h-4 w-4", group.titleClass)} />
                    <h2 className={cn("font-display text-base font-semibold", group.titleClass)}>
                      {group.label}
                    </h2>
                    <span
                      className={cn(
                        "rounded-full px-2 py-0.5 text-[11px] font-semibold tabular-nums",
                        group.badgeClass,
                      )}
                    >
                      {group.items.length}
                    </span>
                  </div>
                  <span className="hidden text-[11px] text-on-surface-variant sm:inline">
                    {group.subtitle}
                  </span>
                </div>

                <div className="space-y-2">
                  {group.items.map((task) => {
                    const isOverdue = Boolean(
                      task.dueDate && task.status !== "COMPLETED" && task.dueDate < todayIso,
                    );
                    const isSelected = selectedIds.has(task.id);
                    return (
                      <div
                        key={task.id}
                        className="group flex flex-col justify-between gap-3 rounded-xl border border-border bg-surface-container p-3.5 shadow-sm transition-all hover:bg-surface-container-high md:flex-row md:items-center"
                      >
                        <div className="flex min-w-0 flex-1 items-start gap-3 md:items-center">
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={() => toggleSelect(task.id)}
                            aria-label={`Select "${task.title}"`}
                            className="mt-1 h-4 w-4 shrink-0 cursor-pointer rounded border-outline-variant accent-primary md:mt-0"
                          />

                          <button
                            type="button"
                            onClick={() => setDrawerTask(task)}
                            className="flex min-w-0 flex-1 flex-col text-left cursor-pointer"
                          >
                            <div className="flex flex-wrap items-center gap-2">
                              <span
                                className={cn(
                                  "truncate text-sm font-medium transition-colors group-hover:text-primary",
                                  task.status === "COMPLETED"
                                    ? "text-on-surface-variant line-through"
                                    : "text-on-surface",
                                )}
                              >
                                {task.title}
                              </span>
                              <TaskPriorityBadge priority={task.priority} />
                            </div>
                            <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-on-surface-variant">
                              <span className="font-mono text-[11px] text-primary">
                                TASK-{task.id}
                              </span>
                              {task.dueDate ? (
                                <>
                                  <span>•</span>
                                  <span
                                    className={cn(
                                      "inline-flex items-center gap-1 tabular-nums",
                                      isOverdue && "font-medium text-error",
                                    )}
                                  >
                                    <Clock className="h-3 w-3" />
                                    {format(parseISO(task.dueDate), "MMM d, yyyy")}
                                    {isOverdue ? " (Overdue)" : ""}
                                  </span>
                                </>
                              ) : null}
                            </div>
                          </button>
                        </div>

                        <div className="flex items-center justify-between gap-2.5 pl-7 md:justify-end md:pl-0">
                          <TaskStatusBadge status={task.status} />
                          <div className="flex items-center gap-1">
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-8 w-8 text-on-surface-variant hover:text-on-surface"
                              onClick={() => setDrawerTask(task)}
                              aria-label={`Inspect "${task.title}"`}
                            >
                              <Edit2 className="h-3.5 w-3.5" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-8 w-8 text-error hover:bg-error-container/30 hover:text-error"
                              onClick={() => setDeletingTask(task)}
                              aria-label={`Delete "${task.title}"`}
                            >
                              <Trash2 className="h-3.5 w-3.5" />
                            </Button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </section>
            );
          })}
        </div>
      )}

      {/* 4. Pagination */}
      {data && data.totalPages > 1 ? (
        <PaginationBar
          pageNumber={data.pageNumber}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          pageSize={data.pageSize}
          first={data.first}
          last={data.last}
          onPageChange={(page) => setParams((p) => ({ ...p, page }))}
        />
      ) : null}

      <TaskFormDialog open={createOpen} onOpenChange={setCreateOpen} />
      <TaskDetailDrawer
        task={drawerTask}
        open={Boolean(drawerTask)}
        onOpenChange={(isOpen) => {
          if (!isOpen) setDrawerTask(null);
        }}
      />
      <DeleteTaskDialog
        open={Boolean(deletingTask)}
        onOpenChange={(isOpen) => {
          if (!isOpen) setDeletingTask(null);
        }}
        task={deletingTask}
      />
    </div>
  );
}
