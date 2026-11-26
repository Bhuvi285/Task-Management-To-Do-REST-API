import { format } from "date-fns";
import { CalendarDays, Edit2, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { TaskPriorityBadge, TaskStatusBadge } from "@/features/tasks/components/TaskBadges";
import type { TaskResponse } from "@/features/tasks/types";

interface TaskCardProps {
  task: TaskResponse;
  onEdit: (task: TaskResponse) => void;
  onDelete: (task: TaskResponse) => void;
}

export function TaskCard({ task, onEdit, onDelete }: TaskCardProps) {
  const isOverdue =
    task.dueDate &&
    task.status !== "COMPLETED" &&
    new Date(task.dueDate) < new Date(new Date().toISOString().slice(0, 10));

  return (
    <Card className="group flex flex-col gap-0 transition-shadow hover:shadow-md">
      <CardHeader className="pb-2">
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-semibold text-foreground leading-snug">
              {task.title}
            </p>
            {task.description ? (
              <p className="mt-1 line-clamp-2 text-xs text-muted-foreground">
                {task.description}
              </p>
            ) : null}
          </div>
          <div className="flex shrink-0 items-center gap-1 opacity-0 transition-opacity group-hover:opacity-100">
            <Button
              variant="ghost"
              size="icon"
              className="h-7 w-7"
              onClick={() => onEdit(task)}
              aria-label={`Edit task: ${task.title}`}
            >
              <Edit2 className="h-3.5 w-3.5" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="h-7 w-7 text-destructive hover:bg-destructive/10 hover:text-destructive"
              onClick={() => onDelete(task)}
              aria-label={`Delete task: ${task.title}`}
            >
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent className="flex flex-wrap items-center gap-2">
        <TaskStatusBadge status={task.status} />
        <TaskPriorityBadge priority={task.priority} />
        {task.dueDate ? (
          <span
            className={`inline-flex items-center gap-1 text-xs ${
              isOverdue ? "text-rose-500" : "text-muted-foreground"
            }`}
          >
            <CalendarDays className="h-3 w-3" />
            {format(new Date(task.dueDate), "MMM d, yyyy")}
            {isOverdue ? " · Overdue" : ""}
          </span>
        ) : null}
      </CardContent>
    </Card>
  );
}

export function TaskCardSkeleton() {
  return (
    <Card className="flex flex-col gap-0">
      <CardHeader className="pb-2">
        <Skeleton className="h-4 w-3/4" />
        <Skeleton className="mt-1 h-3 w-full" />
      </CardHeader>
      <CardContent className="flex gap-2">
        <Skeleton className="h-5 w-16 rounded-full" />
        <Skeleton className="h-5 w-16 rounded-full" />
      </CardContent>
    </Card>
  );
}
