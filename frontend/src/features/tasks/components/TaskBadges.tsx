import { AlertCircle, CheckCircle2, Clock, Flag } from "lucide-react";
import type { TaskPriority, TaskStatus } from "@/features/tasks/types";
import { cn } from "@/lib/utils";

const STATUS_STYLES: Record<
  TaskStatus,
  { label: string; badgeClass: string; dotClass: string }
> = {
  PENDING: {
    label: "Pending",
    badgeClass: "bg-surface-container-high text-on-surface-variant border-outline-variant/40",
    dotClass: "bg-outline",
  },
  IN_PROGRESS: {
    label: "In Progress",
    badgeClass: "bg-secondary-container/20 text-secondary border-secondary/20",
    dotClass: "bg-secondary",
  },
  COMPLETED: {
    label: "Completed",
    badgeClass: "bg-emerald-500/15 text-emerald-500 border-emerald-500/20",
    dotClass: "bg-emerald-500",
  },
};

const PRIORITY_STYLES: Record<
  TaskPriority,
  { label: string; badgeClass: string; dotClass: string }
> = {
  URGENT: {
    label: "Urgent",
    badgeClass: "bg-error-container/40 text-error border-error/20",
    dotClass: "bg-error",
  },
  HIGH: {
    label: "High",
    badgeClass: "bg-primary-container/20 text-primary border-primary/20",
    dotClass: "bg-primary",
  },
  MEDIUM: {
    label: "Medium",
    badgeClass: "bg-secondary-container/25 text-secondary border-secondary/20",
    dotClass: "bg-secondary",
  },
  LOW: {
    label: "Low",
    badgeClass: "bg-surface-container-highest text-on-surface-variant border-outline-variant/30",
    dotClass: "bg-outline",
  },
};

export function TaskStatusBadge({
  status,
  className,
}: {
  status: TaskStatus;
  className?: string;
}) {
  const cfg = STATUS_STYLES[status];
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-[11px] font-semibold tracking-wide",
        cfg.badgeClass,
        className,
      )}
    >
      <span className={cn("h-1.5 w-1.5 rounded-full", cfg.dotClass)} />
      {cfg.label}
    </span>
  );
}

export function TaskPriorityBadge({
  priority,
  className,
}: {
  priority: TaskPriority;
  className?: string;
}) {
  const cfg = PRIORITY_STYLES[priority];
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-[11px] font-semibold uppercase tracking-wide",
        cfg.badgeClass,
        className,
      )}
    >
      {priority === "URGENT" ? (
        <AlertCircle className="h-3 w-3" />
      ) : priority === "HIGH" ? (
        <Flag className="h-3 w-3" />
      ) : (
        <span className={cn("h-1.5 w-1.5 rounded-full", cfg.dotClass)} />
      )}
      {cfg.label}
    </span>
  );
}

export function TaskStatusIcon({ status }: { status: TaskStatus }) {
  if (status === "COMPLETED") {
    return <CheckCircle2 className="h-4 w-4 text-emerald-500" />;
  }
  if (status === "IN_PROGRESS") {
    return <Clock className="h-4 w-4 text-secondary" />;
  }
  return <span className="h-2.5 w-2.5 rounded-full bg-outline inline-block" />;
}
