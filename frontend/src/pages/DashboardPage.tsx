import { useMemo, useState } from "react";
import { format, parseISO } from "date-fns";
import {
  AlertTriangle,
  CalendarClock,
  Check,
  ChevronRight,
  Flag,
  Kanban,
  ListTodo,
  Plus,
  RefreshCw,
  Sparkles,
} from "lucide-react";
import { Link } from "react-router-dom";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Skeleton } from "@/components/ui/skeleton";
import { useToast } from "@/components/ui/toast";
import { useAuth } from "@/features/auth/AuthContext";
import { TaskPriorityBadge, TaskStatusBadge } from "@/features/tasks/components/TaskBadges";
import { TaskDetailDrawer } from "@/features/tasks/components/TaskDetailDrawer";
import { TaskFormDialog } from "@/features/tasks/components/TaskFormDialog";
import {
  useTasksQuery,
  useUpdateTaskMutation,
} from "@/features/tasks/hooks/useTasksQuery";
import type { TaskResponse } from "@/features/tasks/types";
import { normalizeApiError } from "@/lib/api/errors";

function ChartTooltip({
  active,
  payload,
  label,
}: {
  active?: boolean;
  payload?: { value: number }[];
  label?: string;
}) {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded-lg border border-border bg-surface-container-highest px-3 py-2 shadow-lg">
      <p className="text-[11px] font-medium text-on-surface-variant">{label}</p>
      <p className="text-sm font-semibold text-on-surface tabular-nums">
        {payload[0]!.value} tasks
      </p>
    </div>
  );
}

const PRIORITY_COLORS: Record<string, string> = {
  LOW: "#908fa0",
  MEDIUM: "#0566d9",
  HIGH: "#8083ff",
  URGENT: "#ffb4ab",
};

export default function DashboardPage() {
  const { user } = useAuth();
  const { toast } = useToast();
  const updateMutation = useUpdateTaskMutation();

  const [createOpen, setCreateOpen] = useState(false);
  const [drawerTask, setDrawerTask] = useState<TaskResponse | null>(null);

  const { data, isLoading } = useTasksQuery({
    page: 0,
    size: 100,
    sortBy: "createdAt",
    direction: "desc",
  });

  const tasks = data?.content ?? [];
  const total = data?.totalElements ?? 0;
  const todayIso = new Date().toISOString().slice(0, 10);

  const stats = useMemo(() => {
    const pending = tasks.filter((t) => t.status === "PENDING").length;
    const inProgress = tasks.filter((t) => t.status === "IN_PROGRESS").length;
    const completed = tasks.filter((t) => t.status === "COMPLETED").length;
    const urgent = tasks.filter((t) => t.priority === "URGENT").length;
    const high = tasks.filter((t) => t.priority === "HIGH").length;
    const medium = tasks.filter((t) => t.priority === "MEDIUM").length;
    const low = tasks.filter((t) => t.priority === "LOW").length;
    const overdue = tasks.filter(
      (t) => t.dueDate && t.status !== "COMPLETED" && t.dueDate < todayIso,
    ).length;
    const dueToday = tasks.filter(
      (t) => t.dueDate === todayIso && t.status !== "COMPLETED",
    ).length;

    const completionPct = tasks.length > 0 ? Math.round((completed / tasks.length) * 100) : 0;

    return {
      pending,
      inProgress,
      completed,
      urgent,
      high,
      medium,
      low,
      overdue,
      dueToday,
      completionPct,
    };
  }, [tasks, todayIso]);

  const priorityChartData = useMemo(
    () => [
      { name: "Low", value: stats.low, key: "LOW" },
      { name: "Medium", value: stats.medium, key: "MEDIUM" },
      { name: "High", value: stats.high, key: "HIGH" },
      { name: "Urgent", value: stats.urgent, key: "URGENT" },
    ],
    [stats],
  );

  // Top active tasks prioritized by URGENT/HIGH then dueDate
  const priorityTasks = useMemo(() => {
    const rank: Record<string, number> = { URGENT: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };
    return tasks
      .filter((t) => t.status !== "COMPLETED")
      .sort((a, b) => rank[a.priority]! - rank[b.priority]!)
      .slice(0, 5);
  }, [tasks]);

  // Upcoming deadlines
  const upcomingDeadlines = useMemo(() => {
    return tasks
      .filter((t) => t.dueDate && t.status !== "COMPLETED")
      .sort((a, b) => (a.dueDate ?? "").localeCompare(b.dueDate ?? ""))
      .slice(0, 4);
  }, [tasks]);

  const displayName = user?.email ? user.email.split("@")[0] : "there";

  const handleToggleComplete = async (task: TaskResponse) => {
    const nextStatus = task.status === "COMPLETED" ? "PENDING" : "COMPLETED";
    try {
      await updateMutation.mutateAsync({
        taskId: task.id,
        payload: {
          title: task.title,
          description: task.description,
          status: nextStatus,
          priority: task.priority,
          dueDate: task.dueDate,
        },
      });
      toast({
        title: nextStatus === "COMPLETED" ? "Task completed" : "Task reopened",
        variant: "success",
      });
    } catch (err) {
      toast({
        title: "Failed to update task",
        description: normalizeApiError(err).message,
        variant: "error",
      });
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Greeting Banner with Layered Ambient Depth */}
      <section className="relative overflow-hidden rounded-2xl border border-border bg-surface-container-low p-6 shadow-xl">
        <div className="pointer-events-none absolute -top-24 -right-16 h-80 w-80 rounded-full bg-primary-container/20 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-20 left-1/3 h-64 w-64 rounded-full bg-secondary-container/15 blur-3xl" />

        <div className="relative z-10 flex flex-col justify-between gap-5 md:flex-row md:items-center">
          <div className="max-w-2xl space-y-2">
            <div className="inline-flex items-center gap-2 rounded-full bg-surface-container-high/80 px-3 py-1 backdrop-blur-md">
              <span className="h-2 w-2 animate-pulse rounded-full bg-primary" />
              <span className="text-[11px] font-semibold uppercase tracking-widest text-primary">
                Workspace
              </span>
              <span className="text-[11px] text-on-surface-variant">•</span>
              <span className="text-[11px] font-medium text-on-surface-variant">
                {format(new Date(), "EEEE, MMM d")}
              </span>
            </div>

            <h1 className="font-display text-2xl font-semibold tracking-tight text-on-surface sm:text-4xl">
              Welcome back, {displayName}
            </h1>

            <p className="text-sm text-on-surface-variant">
              You have{" "}
              <span className="font-semibold text-primary tabular-nums">
                {stats.urgent} urgent
              </span>{" "}
              {stats.urgent === 1 ? "task" : "tasks"} and{" "}
              <span className="font-semibold text-secondary tabular-nums">
                {stats.inProgress} in progress
              </span>
              {total > 100 ? ` (metrics from latest 100 of ${total} tasks)` : "."}
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            <Link
              to="/tasks"
              className="inline-flex items-center gap-1.5 rounded-lg bg-surface-container px-3.5 py-2 text-xs font-medium text-on-surface shadow-sm transition-colors hover:bg-surface-container-high"
            >
              <ListTodo className="h-4 w-4 text-on-surface-variant" />
              <span>My Tasks</span>
            </Link>
            <Link
              to="/kanban"
              className="inline-flex items-center gap-1.5 rounded-lg bg-surface-container px-3.5 py-2 text-xs font-medium text-on-surface shadow-sm transition-colors hover:bg-surface-container-high"
            >
              <Kanban className="h-4 w-4 text-on-surface-variant" />
              <span>Kanban Board</span>
            </Link>
            <button
              type="button"
              onClick={() => setCreateOpen(true)}
              className="inline-flex items-center gap-2 rounded-lg bg-primary-container px-4 py-2 text-xs font-semibold text-on-primary-container shadow-[0_4px_20px_rgba(128,131,255,0.35)] transition-all hover:bg-primary hover:text-primary-foreground cursor-pointer"
            >
              <Plus className="h-4 w-4" />
              <span>Create Task</span>
            </button>
          </div>
        </div>
      </section>

      {/* 2. Executive KPI Metrics Grid */}
      <section className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {/* Completed */}
        <div className="rounded-xl border border-border bg-surface-container-low p-4 shadow-md transition-all duration-200 hover:-translate-y-0.5 hover:shadow-xl">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
              Completed Tasks
            </span>
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-secondary-container/20 text-secondary">
              <Sparkles className="h-4 w-4" />
            </div>
          </div>
          {isLoading ? (
            <Skeleton className="h-8 w-24" />
          ) : (
            <div className="mb-2 flex items-baseline justify-between">
              <span className="font-display text-3xl font-semibold text-on-surface tabular-nums">
                {stats.completed}
                <span className="text-lg text-on-surface-variant">/{tasks.length}</span>
              </span>
              <span className="rounded-full bg-surface-container-high px-2 py-0.5 text-[11px] font-semibold text-secondary tabular-nums">
                {stats.completionPct}%
              </span>
            </div>
          )}
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-surface-container-highest">
            <div
              className="h-full rounded-full bg-secondary transition-all"
              style={{ width: `${stats.completionPct}%` }}
            />
          </div>
          <span className="mt-2 block text-xs text-on-surface-variant">
            {stats.completionPct}% overall completion rate
          </span>
        </div>

        {/* In Progress */}
        <div className="rounded-xl border border-border bg-surface-container-low p-4 shadow-md transition-all duration-200 hover:-translate-y-0.5 hover:shadow-xl">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
              In Progress
            </span>
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-container/20 text-primary">
              <RefreshCw className="h-4 w-4" />
            </div>
          </div>
          {isLoading ? (
            <Skeleton className="h-8 w-24" />
          ) : (
            <div className="mb-2 flex items-baseline justify-between">
              <span className="font-display text-3xl font-semibold text-on-surface tabular-nums">
                {stats.inProgress}{" "}
                <span className="font-sans text-sm font-normal text-on-surface-variant">
                  active
                </span>
              </span>
              <span className="rounded-full bg-surface-container-high px-2 py-0.5 text-[11px] font-semibold text-primary tabular-nums">
                {stats.pending} pending
              </span>
            </div>
          )}
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-surface-container-highest">
            <div
              className="h-full rounded-full bg-primary transition-all"
              style={{
                width: `${tasks.length ? Math.round((stats.inProgress / tasks.length) * 100) : 0}%`,
              }}
            />
          </div>
          <span className="mt-2 block text-xs text-on-surface-variant">
            Active tasks currently underway
          </span>
        </div>

        {/* Urgent & Overdue */}
        <div className="rounded-xl border border-border bg-surface-container-low p-4 shadow-md transition-all duration-200 hover:-translate-y-0.5 hover:shadow-xl">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
              Urgent &amp; Overdue
            </span>
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-error-container/40 text-error">
              <AlertTriangle className="h-4 w-4" />
            </div>
          </div>
          {isLoading ? (
            <Skeleton className="h-8 w-24" />
          ) : (
            <div className="mb-2 flex items-baseline justify-between">
              <span className="font-display text-3xl font-semibold text-error tabular-nums">
                {stats.urgent}{" "}
                <span className="font-sans text-sm font-normal text-on-surface-variant">
                  urgent
                </span>
              </span>
              <span className="rounded-full bg-error-container/30 px-2 py-0.5 text-[11px] font-semibold text-error tabular-nums">
                {stats.overdue} overdue
              </span>
            </div>
          )}
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-surface-container-highest">
            <div
              className="h-full rounded-full bg-error transition-all"
              style={{
                width: `${tasks.length ? Math.min(100, Math.round(((stats.urgent + stats.overdue) / tasks.length) * 100)) : 0}%`,
              }}
            />
          </div>
          <span className="mt-2 block text-xs text-on-surface-variant">
            {stats.dueToday} due today
          </span>
        </div>

        {/* High Priority */}
        <div className="rounded-xl border border-border bg-surface-container-low p-4 shadow-md transition-all duration-200 hover:-translate-y-0.5 hover:shadow-xl">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
              High Priority
            </span>
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-tertiary-container/30 text-tertiary">
              <Flag className="h-4 w-4" />
            </div>
          </div>
          {isLoading ? (
            <Skeleton className="h-8 w-24" />
          ) : (
            <div className="mb-2 flex items-baseline justify-between">
              <span className="font-display text-3xl font-semibold text-on-surface tabular-nums">
                {stats.high}
              </span>
              <span className="rounded-full bg-surface-container-high px-2 py-0.5 text-[11px] font-semibold text-tertiary tabular-nums">
                {stats.medium + stats.low} std
              </span>
            </div>
          )}
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-surface-container-highest">
            <div
              className="h-full rounded-full bg-tertiary transition-all"
              style={{
                width: `${tasks.length ? Math.round((stats.high / tasks.length) * 100) : 0}%`,
              }}
            />
          </div>
          <span className="mt-2 block text-xs text-on-surface-variant">
            Total workspace tasks: {total}
          </span>
        </div>
      </section>

      {/* 3. Asymmetric Two-Column Core Workspace Grid */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
        {/* Left Column (8 cols) */}
        <div className="space-y-6 lg:col-span-8">
          {/* Chart Module */}
          <section className="rounded-xl border border-border bg-surface-container-low p-5 shadow-md">
            <div className="mb-4 flex flex-col justify-between gap-2 sm:flex-row sm:items-center">
              <div>
                <div className="flex items-center gap-2">
                  <h2 className="font-display text-lg font-semibold text-on-surface">
                    Task Priority Distribution
                  </h2>
                  <span className="rounded-full bg-primary-container/20 px-2 py-0.5 text-[11px] font-semibold text-primary tabular-nums">
                    {tasks.length} Total
                  </span>
                </div>
                <p className="mt-0.5 text-xs text-on-surface-variant">
                  Breakdown of workspace tasks across priority tiers
                </p>
              </div>
            </div>

            {isLoading ? (
              <Skeleton className="h-56 w-full rounded-xl" />
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={priorityChartData} barCategoryGap="28%">
                  <CartesianGrid
                    strokeDasharray="3 3"
                    stroke="var(--outline-variant)"
                    strokeOpacity={0.35}
                    vertical={false}
                  />
                  <XAxis
                    dataKey="name"
                    tick={{ fontSize: 12, fill: "var(--on-surface-variant)" }}
                    axisLine={false}
                    tickLine={false}
                  />
                  <YAxis
                    tick={{ fontSize: 12, fill: "var(--on-surface-variant)" }}
                    axisLine={false}
                    tickLine={false}
                    allowDecimals={false}
                  />
                  <Tooltip
                    content={<ChartTooltip />}
                    cursor={{ fill: "var(--surface-container-high)", opacity: 0.5 }}
                  />
                  <Bar dataKey="value" radius={[6, 6, 0, 0]}>
                    {priorityChartData.map((entry) => (
                      <Cell key={entry.key} fill={PRIORITY_COLORS[entry.key] ?? "#8083ff"} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </section>

          {/* Priority Tasks List */}
          <section className="rounded-xl border border-border bg-surface-container-low p-5 shadow-md">
            <div className="mb-4 flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <h2 className="font-display text-lg font-semibold text-on-surface">
                  Priority Tasks
                </h2>
                <span className="rounded-full bg-surface-container-high px-2.5 py-0.5 text-[11px] font-medium text-on-surface-variant tabular-nums">
                  {stats.pending + stats.inProgress} Remaining
                </span>
              </div>
              <Link
                to="/tasks"
                className="inline-flex items-center gap-1 text-xs font-medium text-on-surface-variant transition-colors hover:text-on-surface"
              >
                <span>View All</span>
                <ChevronRight className="h-4 w-4" />
              </Link>
            </div>

            {isLoading ? (
              <div className="space-y-2">
                {Array.from({ length: 4 }).map((_, i) => (
                  <Skeleton key={i} className="h-14 w-full rounded-lg" />
                ))}
              </div>
            ) : priorityTasks.length === 0 ? (
              <p className="py-8 text-center text-sm text-on-surface-variant">
                No open tasks! Create one to get started.
              </p>
            ) : (
              <div className="space-y-2">
                {priorityTasks.map((task) => {
                  const isOverdue = Boolean(
                    task.dueDate && task.status !== "COMPLETED" && task.dueDate < todayIso,
                  );
                  return (
                    <div
                      key={task.id}
                      className="group flex items-center justify-between gap-3 rounded-lg bg-surface-container p-3 transition-all hover:bg-surface-container-high"
                    >
                      <div className="flex min-w-0 flex-1 items-center gap-3">
                        <button
                          type="button"
                          onClick={() => handleToggleComplete(task)}
                          aria-label={`Mark "${task.title}" completed`}
                          className="flex h-4 w-4 shrink-0 items-center justify-center rounded border border-outline-variant bg-surface-container-highest text-on-primary-container transition-colors hover:border-primary hover:bg-primary-container cursor-pointer"
                        >
                          <Check className="h-3 w-3 opacity-0 transition-opacity group-hover:opacity-80" />
                        </button>

                        <button
                          type="button"
                          onClick={() => setDrawerTask(task)}
                          className="flex min-w-0 flex-1 flex-col text-left cursor-pointer"
                        >
                          <span className="truncate text-sm font-medium text-on-surface transition-colors group-hover:text-primary">
                            {task.title}
                          </span>
                          <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-on-surface-variant">
                            <span className="font-mono text-[11px] text-primary">
                              TASK-{task.id}
                            </span>
                            {task.dueDate ? (
                              <>
                                <span>•</span>
                                <span className={isOverdue ? "font-medium text-error" : ""}>
                                  {format(parseISO(task.dueDate), "MMM d, yyyy")}
                                  {isOverdue ? " (Overdue)" : ""}
                                </span>
                              </>
                            ) : null}
                          </div>
                        </button>
                      </div>

                      <div className="flex shrink-0 items-center gap-2">
                        <TaskStatusBadge status={task.status} className="hidden sm:inline-flex" />
                        <TaskPriorityBadge priority={task.priority} />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>
        </div>

        {/* Right Column (4 cols) */}
        <div className="space-y-6 lg:col-span-4">
          {/* Status Breakdown */}
          <section className="rounded-xl border border-border bg-surface-container-low p-5 shadow-md space-y-4">
            <h2 className="font-display text-lg font-semibold text-on-surface">
              Status Breakdown
            </h2>

            {isLoading ? (
              <div className="space-y-3">
                <Skeleton className="h-12 w-full rounded-lg" />
                <Skeleton className="h-12 w-full rounded-lg" />
                <Skeleton className="h-12 w-full rounded-lg" />
              </div>
            ) : (
              <div className="space-y-3">
                {[
                  {
                    label: "Pending",
                    count: stats.pending,
                    color: "bg-outline",
                  },
                  {
                    label: "In Progress",
                    count: stats.inProgress,
                    color: "bg-secondary",
                  },
                  {
                    label: "Completed",
                    count: stats.completed,
                    color: "bg-emerald-500",
                  },
                ].map((row) => {
                  const pct = tasks.length ? Math.round((row.count / tasks.length) * 100) : 0;
                  return (
                    <div
                      key={row.label}
                      className="rounded-lg bg-surface-container p-3 space-y-2"
                    >
                      <div className="flex items-center justify-between text-xs">
                        <span className="font-medium text-on-surface">{row.label}</span>
                        <span className="font-semibold text-on-surface-variant tabular-nums">
                          {row.count} ({pct}%)
                        </span>
                      </div>
                      <div className="h-1.5 w-full overflow-hidden rounded-full bg-surface-container-highest">
                        <div
                          className={`h-full rounded-full ${row.color}`}
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </section>

          {/* Upcoming Deadlines */}
          <section className="rounded-xl border border-border bg-surface-container-low p-5 shadow-md space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="font-display text-lg font-semibold text-on-surface">
                Upcoming Deadlines
              </h2>
              <CalendarClock className="h-4 w-4 text-on-surface-variant" />
            </div>

            {isLoading ? (
              <div className="space-y-2">
                <Skeleton className="h-14 w-full rounded-lg" />
                <Skeleton className="h-14 w-full rounded-lg" />
              </div>
            ) : upcomingDeadlines.length === 0 ? (
              <p className="py-4 text-xs text-on-surface-variant">
                No upcoming deadlines scheduled.
              </p>
            ) : (
              <div className="space-y-2.5">
                {upcomingDeadlines.map((task) => {
                  const isOverdue = Boolean(task.dueDate && task.dueDate < todayIso);
                  return (
                    <button
                      key={task.id}
                      type="button"
                      onClick={() => setDrawerTask(task)}
                      className="flex w-full items-start justify-between gap-3 rounded-lg bg-surface-container p-3 text-left transition-colors hover:bg-surface-container-high cursor-pointer"
                    >
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-medium text-on-surface">
                          {task.title}
                        </p>
                        <p
                          className={`mt-1 text-xs tabular-nums ${
                            isOverdue ? "font-medium text-error" : "text-on-surface-variant"
                          }`}
                        >
                          {task.dueDate ? format(parseISO(task.dueDate), "MMM d, yyyy") : ""}
                          {isOverdue ? " · Overdue" : ""}
                        </p>
                      </div>
                      <TaskPriorityBadge priority={task.priority} />
                    </button>
                  );
                })}
              </div>
            )}
          </section>
        </div>
      </div>

      <TaskFormDialog open={createOpen} onOpenChange={setCreateOpen} />
      <TaskDetailDrawer
        task={drawerTask}
        open={Boolean(drawerTask)}
        onOpenChange={(isOpen) => {
          if (!isOpen) setDrawerTask(null);
        }}
      />
    </div>
  );
}
