import { useMemo, useState } from "react";
import {
  addMonths,
  eachDayOfInterval,
  endOfMonth,
  format,
  getDay,
  isSameDay,
  isSameMonth,
  isToday,
  startOfMonth,
  subMonths,
} from "date-fns";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { TaskPriorityBadge, TaskStatusBadge } from "@/features/tasks/components/TaskBadges";
import { useTasksQuery } from "@/features/tasks/hooks/useTasksQuery";
import type { TaskResponse } from "@/features/tasks/types";

// ─── Day Cell ────────────────────────────────────────────────────────────────

const PRIORITY_DOT: Record<string, string> = {
  URGENT: "bg-rose-500",
  HIGH: "bg-amber-400",
  MEDIUM: "bg-cyan-400",
  LOW: "bg-slate-400",
};

interface DayCellProps {
  date: Date;
  currentMonth: Date;
  tasks: TaskResponse[];
  isSelected: boolean;
  onSelect: (date: Date) => void;
}

function DayCell({ date, currentMonth, tasks, isSelected, onSelect }: DayCellProps) {
  const inMonth = isSameMonth(date, currentMonth);
  const today = isToday(date);
  const hasTasks = tasks.length > 0;

  return (
    <button
      onClick={() => onSelect(date)}
      className={`group relative flex min-h-[72px] flex-col items-start gap-0.5 rounded-lg p-1.5 text-left transition-colors sm:min-h-[80px]
        ${inMonth ? "text-foreground" : "text-muted-foreground/40"}
        ${isSelected ? "bg-primary/15 ring-1 ring-primary/40" : "hover:bg-muted/50"}
        ${today && !isSelected ? "ring-1 ring-primary/60" : ""}
      `}
    >
      {/* Day number */}
      <span
        className={`flex h-6 w-6 items-center justify-center rounded-full text-xs font-semibold
          ${today ? "bg-gradient-to-br from-violet-500 to-indigo-600 text-white" : ""}
        `}
      >
        {format(date, "d")}
      </span>

      {/* Task dots / previews */}
      {hasTasks && (
        <div className="flex w-full flex-col gap-0.5">
          {tasks.slice(0, 2).map((task) => (
            <div
              key={task.id}
              className={`flex items-center gap-1 overflow-hidden rounded px-1 py-0.5 text-[10px] leading-tight
                ${task.status === "COMPLETED" ? "opacity-50" : ""}
              `}
            >
              <div
                className={`h-1.5 w-1.5 shrink-0 rounded-full ${PRIORITY_DOT[task.priority] ?? "bg-primary"}`}
              />
              <span className="truncate text-foreground/70">{task.title}</span>
            </div>
          ))}
          {tasks.length > 2 && (
            <span className="pl-1 text-[10px] text-muted-foreground">
              +{tasks.length - 2} more
            </span>
          )}
        </div>
      )}
    </button>
  );
}

// ─── Day Detail Panel ────────────────────────────────────────────────────────

interface DayDetailProps {
  date: Date;
  tasks: TaskResponse[];
}

function DayDetail({ date, tasks }: DayDetailProps) {
  return (
    <Card className="glass-surface sticky top-4">
      <CardContent className="p-4">
        <p className="mb-3 text-sm font-semibold text-foreground">
          {format(date, "EEEE, MMMM d")}
        </p>
        {tasks.length === 0 ? (
          <p className="text-xs text-muted-foreground">No tasks due this day</p>
        ) : (
          <div className="space-y-3">
            {tasks.map((task) => (
              <div
                key={task.id}
                className="rounded-lg border border-border/50 bg-muted/30 p-3"
              >
                <p
                  className={`text-sm font-medium ${
                    task.status === "COMPLETED"
                      ? "text-muted-foreground line-through"
                      : "text-foreground"
                  }`}
                >
                  {task.title}
                </p>
                {task.description && (
                  <p className="mt-0.5 line-clamp-2 text-xs text-muted-foreground">
                    {task.description}
                  </p>
                )}
                <div className="mt-2 flex flex-wrap gap-1.5">
                  <TaskStatusBadge status={task.status} />
                  <TaskPriorityBadge priority={task.priority} />
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

// ─── Main Page ───────────────────────────────────────────────────────────────

const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

export default function CalendarPage() {
  const [currentMonth, setCurrentMonth] = useState(() => new Date());
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);

  // Compute dueFrom / dueTo for the displayed month window
  const monthStart = startOfMonth(currentMonth);
  const monthEnd = endOfMonth(currentMonth);

  const { data, isLoading } = useTasksQuery({
    page: 0,
    size: 100,
    dueFrom: format(monthStart, "yyyy-MM-dd"),
    dueTo: format(monthEnd, "yyyy-MM-dd"),
  });

  // Build a date → tasks map
  const tasksByDate = useMemo(() => {
    const map = new Map<string, TaskResponse[]>();
    for (const task of data?.content ?? []) {
      if (!task.dueDate) continue;
      const key = task.dueDate; // "yyyy-MM-dd"
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(task);
    }
    return map;
  }, [data]);

  // Grid days (fill leading/trailing blanks so grid starts on Sunday)
  const days = useMemo(() => {
    const start = monthStart;
    const end = monthEnd;
    const allDays = eachDayOfInterval({ start, end });
    const leadingBlanks = getDay(start); // 0=Sun
    return { allDays, leadingBlanks };
  }, [currentMonth]); // eslint-disable-line react-hooks/exhaustive-deps

  function tasksForDay(date: Date) {
    return tasksByDate.get(format(date, "yyyy-MM-dd")) ?? [];
  }

  const selectedTasks = selectedDate ? tasksForDay(selectedDate) : [];

  return (
    <div className="flex flex-col gap-6 p-6 lg:flex-row">
      {/* Calendar */}
      <div className="flex-1 space-y-4">
        {/* Month header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="tf-gradient-text text-2xl font-bold font-display">
              {format(currentMonth, "MMMM yyyy")}
            </h1>
            <p className="mt-0.5 text-sm text-muted-foreground">
              Tasks due this month
            </p>
          </div>
          <div className="flex items-center gap-1">
            <Button
              variant="outline"
              size="icon"
              onClick={() => { setCurrentMonth((m) => subMonths(m, 1)); setSelectedDate(null); }}
              aria-label="Previous month"
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => { setCurrentMonth(new Date()); setSelectedDate(new Date()); }}
            >
              Today
            </Button>
            <Button
              variant="outline"
              size="icon"
              onClick={() => { setCurrentMonth((m) => addMonths(m, 1)); setSelectedDate(null); }}
              aria-label="Next month"
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>

        {/* Calendar grid */}
        <Card>
          <CardContent className="p-3">
            {/* Weekday headers */}
            <div className="mb-1 grid grid-cols-7">
              {WEEKDAYS.map((d) => (
                <div
                  key={d}
                  className="py-2 text-center text-xs font-medium text-muted-foreground"
                >
                  {d}
                </div>
              ))}
            </div>

            {/* Day grid */}
            {isLoading ? (
              <div className="grid grid-cols-7 gap-1">
                {Array.from({ length: 35 }).map((_, i) => (
                  <Skeleton key={i} className="h-16 rounded-lg" />
                ))}
              </div>
            ) : (
              <div className="grid grid-cols-7 gap-1">
                {/* Leading blanks */}
                {Array.from({ length: days.leadingBlanks }).map((_, i) => (
                  <div key={`blank-${i}`} />
                ))}
                {/* Actual days */}
                {days.allDays.map((date) => (
                  <DayCell
                    key={date.toISOString()}
                    date={date}
                    currentMonth={currentMonth}
                    tasks={tasksForDay(date)}
                    isSelected={!!selectedDate && isSameDay(date, selectedDate)}
                    onSelect={setSelectedDate}
                  />
                ))}
              </div>
            )}

            {/* Legend */}
            <div className="mt-4 flex flex-wrap items-center gap-3 border-t border-border/50 pt-3">
              <span className="text-xs text-muted-foreground">Priority:</span>
              {(["URGENT", "HIGH", "MEDIUM", "LOW"] as const).map((p) => (
                <span key={p} className="flex items-center gap-1 text-xs text-muted-foreground">
                  <span className={`h-2 w-2 rounded-full ${PRIORITY_DOT[p]}`} />
                  {p.charAt(0) + p.slice(1).toLowerCase()}
                </span>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Detail panel */}
      <div className="lg:w-72">
        {selectedDate ? (
          <DayDetail date={selectedDate} tasks={selectedTasks} />
        ) : (
          <Card className="glass-surface">
            <CardContent className="p-6 text-center">
              <p className="text-sm text-muted-foreground">
                Click a day to see tasks due on that date
              </p>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  );
}
