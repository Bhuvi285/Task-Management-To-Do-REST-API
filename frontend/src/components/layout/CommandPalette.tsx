import { useEffect, useState } from "react";
import {
  ArrowRight,
  CalendarDays,
  CheckCircle2,
  Kanban,
  LayoutDashboard,
  ListTodo,
  Plus,
  Search,
  Settings,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import { TaskPriorityBadge, TaskStatusBadge } from "@/features/tasks/components/TaskBadges";
import { useTasksQuery } from "@/features/tasks/hooks/useTasksQuery";
import type { TaskResponse } from "@/features/tasks/types";

interface CommandPaletteProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onCreateTask: () => void;
  onSelectTask?: (task: TaskResponse) => void;
}

const NAV_COMMANDS = [
  { to: "/dashboard", label: "Go to Dashboard", icon: LayoutDashboard },
  { to: "/tasks", label: "Go to My Tasks", icon: ListTodo },
  { to: "/kanban", label: "Go to Kanban Board", icon: Kanban },
  { to: "/calendar", label: "Go to Calendar", icon: CalendarDays },
  { to: "/settings", label: "Go to Settings", icon: Settings },
];

export function CommandPalette({
  open,
  onOpenChange,
  onCreateTask,
  onSelectTask,
}: CommandPaletteProps) {
  const [query, setQuery] = useState("");
  const navigate = useNavigate();

  const { data, isFetching } = useTasksQuery({
    search: query.trim() || undefined,
    page: 0,
    size: 6,
    sortBy: "updatedAt",
    direction: "desc",
  });

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        onOpenChange(!open);
      } else if (e.key === "Escape" && open) {
        onOpenChange(false);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [open, onOpenChange]);

  if (!open) return null;

  const tasks = data?.content ?? [];
  const filteredNav = NAV_COMMANDS.filter((c) =>
    c.label.toLowerCase().includes(query.trim().toLowerCase()),
  );

  return (
    <div
      className="fixed inset-0 z-[70] flex items-start justify-center pt-[12vh] px-4"
      role="dialog"
      aria-modal="true"
      aria-label="Command palette"
    >
      <div
        className="fixed inset-0 bg-surface-container-lowest/75 backdrop-blur-md"
        onClick={() => onOpenChange(false)}
      />

      <div className="relative z-10 w-full max-w-[640px] overflow-hidden rounded-2xl border border-border bg-surface-container-low/95 shadow-2xl backdrop-blur-2xl">
        {/* Search Input */}
        <div className="flex items-center gap-3 border-b border-border px-4 py-3">
          <Search className="h-4 w-4 shrink-0 text-on-surface-variant" />
          <input
            autoFocus
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search tasks by title or description, or jump to a view..."
            className="w-full bg-transparent text-sm text-on-surface placeholder:text-on-surface-variant focus:outline-none"
          />
          <kbd className="rounded bg-surface-container-high px-1.5 py-0.5 text-[11px] font-medium text-on-surface-variant border border-outline-variant/30">
            ESC
          </kbd>
        </div>

        <div className="max-h-[420px] overflow-y-auto p-2 space-y-3">
          {/* Quick Action */}
          <div>
            <p className="px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
              Quick Actions
            </p>
            <button
              type="button"
              onClick={() => {
                onOpenChange(false);
                onCreateTask();
              }}
              className="flex w-full items-center justify-between rounded-lg px-2.5 py-2 text-sm text-on-surface transition-colors hover:bg-surface-container-high cursor-pointer"
            >
              <span className="flex items-center gap-2.5">
                <Plus className="h-4 w-4 text-primary" />
                <span>Create New Task</span>
              </span>
              <ArrowRight className="h-3.5 w-3.5 text-on-surface-variant" />
            </button>
          </div>

          {/* Matching Tasks */}
          <div>
            <div className="flex items-center justify-between px-2.5 py-1">
              <p className="text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
                Tasks {isFetching ? "(Searching...)" : ""}
              </p>
            </div>
            {tasks.length === 0 ? (
              <p className="px-2.5 py-2 text-xs text-on-surface-variant">
                No matching tasks found.
              </p>
            ) : (
              <div className="space-y-0.5">
                {tasks.map((task) => (
                  <button
                    key={task.id}
                    type="button"
                    onClick={() => {
                      onOpenChange(false);
                      if (onSelectTask) {
                        onSelectTask(task);
                      } else {
                        navigate("/tasks");
                      }
                    }}
                    className="flex w-full items-center justify-between gap-3 rounded-lg px-2.5 py-2 text-left text-sm transition-colors hover:bg-surface-container-high cursor-pointer"
                  >
                    <div className="flex min-w-0 items-center gap-2.5">
                      <CheckCircle2 className="h-4 w-4 shrink-0 text-primary" />
                      <span className="truncate font-medium text-on-surface">
                        {task.title}
                      </span>
                      <span className="shrink-0 font-mono text-[11px] text-on-surface-variant">
                        TASK-{task.id}
                      </span>
                    </div>
                    <div className="flex shrink-0 items-center gap-1.5">
                      <TaskStatusBadge status={task.status} />
                      <TaskPriorityBadge priority={task.priority} />
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Navigation */}
          {filteredNav.length > 0 && (
            <div>
              <p className="px-2.5 py-1 text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
                Navigation
              </p>
              <div className="space-y-0.5">
                {filteredNav.map((item) => {
                  const Icon = item.icon;
                  return (
                    <button
                      key={item.to}
                      type="button"
                      onClick={() => {
                        onOpenChange(false);
                        navigate(item.to);
                      }}
                      className="flex w-full items-center justify-between rounded-lg px-2.5 py-2 text-sm text-on-surface-variant transition-colors hover:bg-surface-container-high hover:text-on-surface cursor-pointer"
                    >
                      <span className="flex items-center gap-2.5">
                        <Icon className="h-4 w-4" />
                        <span>{item.label}</span>
                      </span>
                      <ArrowRight className="h-3.5 w-3.5" />
                    </button>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
