import { useState, type ReactNode } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  CalendarDays,
  CheckCircle2,
  Home,
  Kanban,
  LogOut,
  Menu,
  Plus,
  Search,
  Settings,
  UserCircle2,
  X,
} from "lucide-react";
import { BrandLogo } from "@/components/layout/BrandLogo";
import { CommandPalette } from "@/components/layout/CommandPalette";
import { ThemeToggle } from "@/components/layout/ThemeToggle";
import { Button } from "@/components/ui/button";
import { useToast } from "@/components/ui/toast";
import { useAuth } from "@/features/auth/AuthContext";
import { TaskDetailDrawer } from "@/features/tasks/components/TaskDetailDrawer";
import { TaskFormDialog } from "@/features/tasks/components/TaskFormDialog";
import { useTasksQuery } from "@/features/tasks/hooks/useTasksQuery";
import type { TaskResponse } from "@/features/tasks/types";
import { cn } from "@/lib/utils";

interface AppShellProps {
  children?: ReactNode;
}

export function AppShell({ children }: AppShellProps) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [commandOpen, setCommandOpen] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [drawerTask, setDrawerTask] = useState<TaskResponse | null>(null);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const { user, logout } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();

  // Live task count badge for sidebar
  const { data: taskSummary } = useTasksQuery({ page: 0, size: 1 });
  const totalTasks = taskSummary?.totalElements;

  const navItems = [
    { to: "/dashboard", label: "Dashboard", icon: Home },
    { to: "/tasks", label: "My Tasks", icon: CheckCircle2, badge: totalTasks },
    { to: "/kanban", label: "Kanban Board", icon: Kanban },
    { to: "/calendar", label: "Calendar", icon: CalendarDays },
  ] as const;

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
      toast({
        title: "Signed out",
        description: "Your session has been revoked.",
        variant: "info",
      });
      navigate("/auth/login", { replace: true });
    } finally {
      setIsLoggingOut(false);
    }
  };

  const renderNavLinks = (onItemClick?: () => void) => (
    <div className="space-y-1">
      {navItems.map((item) => {
        const Icon = item.icon;
        return (
          <NavLink
            key={item.to}
            to={item.to}
            onClick={onItemClick}
            className={({ isActive }) =>
              cn(
                "flex items-center justify-between rounded-lg px-3 py-2 text-sm transition-colors",
                isActive
                  ? "bg-primary-container text-on-primary-container font-semibold shadow-sm"
                  : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
              )
            }
          >
            {({ isActive }) => (
              <>
                <span className="flex items-center gap-2.5">
                  <Icon className="h-4 w-4 shrink-0" />
                  <span>{item.label}</span>
                </span>
                {"badge" in item && typeof item.badge === "number" ? (
                  <span
                    className={cn(
                      "rounded-full px-2 py-0.5 text-[11px] font-semibold tabular-nums",
                      isActive
                        ? "bg-on-primary-container/15 text-on-primary-container"
                        : "bg-surface-container-high text-on-surface-variant",
                    )}
                  >
                    {item.badge}
                  </span>
                ) : null}
              </>
            )}
          </NavLink>
        );
      })}
    </div>
  );

  return (
    <div className="min-h-screen bg-surface text-on-surface antialiased">
      {/* Desktop Sidebar (Stitch w-64 bg-surface-container-low) */}
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col justify-between overflow-y-auto border-r border-border bg-surface-container-low lg:flex">
        <div className="flex flex-col">
          <div className="flex h-16 items-center px-4">
            <BrandLogo />
          </div>

          <div className="px-4 pt-4 pb-2">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
              Main
            </span>
          </div>

          <nav className="px-2" aria-label="Main navigation">
            {renderNavLinks()}
          </nav>
        </div>

        {/* Bottom Workspace Identity & Settings */}
        <div className="space-y-2 p-2">
          <nav className="space-y-1" aria-label="Account navigation">
            <NavLink
              to="/settings"
              className={({ isActive }) =>
                cn(
                  "flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm transition-colors",
                  isActive
                    ? "bg-primary-container text-on-primary-container font-semibold"
                    : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
                )
              }
            >
              <Settings className="h-4 w-4 shrink-0" />
              <span>Settings</span>
            </NavLink>
          </nav>

          <div className="rounded-xl bg-surface-container p-3 space-y-2.5 border border-border">
            <div className="flex items-center gap-2.5">
              <div className="relative flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary-container/20 text-primary">
                <UserCircle2 className="h-5 w-5" />
                <span className="absolute right-0 bottom-0 h-2.5 w-2.5 rounded-full bg-secondary-container ring-2 ring-surface-container" />
              </div>
              <div className="min-w-0 flex-1">
                <p className="truncate text-xs font-medium text-on-surface">
                  {user?.email ?? "Authenticated"}
                </p>
                <p className="text-[11px] text-on-surface-variant">Active session</p>
              </div>
            </div>
            <Button
              variant="outline"
              size="sm"
              className="w-full justify-center bg-surface-container-high/50 hover:bg-surface-container-high"
              onClick={handleLogout}
              disabled={isLoggingOut}
            >
              <LogOut className="h-3.5 w-3.5" />
              Sign out
            </Button>
          </div>
        </div>
      </aside>

      {/* Mobile Drawer */}
      {mobileMenuOpen ? (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div
            className="fixed inset-0 bg-surface-container-lowest/70 backdrop-blur-xs"
            onClick={() => setMobileMenuOpen(false)}
          />
          <aside className="fixed inset-y-0 left-0 z-50 flex w-64 flex-col justify-between border-r border-border bg-surface-container-low p-3 shadow-2xl">
            <div>
              <div className="mb-4 flex items-center justify-between px-1">
                <BrandLogo />
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => setMobileMenuOpen(false)}
                  aria-label="Close navigation menu"
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
              <p className="px-2 pb-2 text-[11px] font-semibold uppercase tracking-wider text-on-surface-variant">
                Main
              </p>
              <nav aria-label="Mobile navigation">
                {renderNavLinks(() => setMobileMenuOpen(false))}
              </nav>
            </div>

            <div className="space-y-2 border-t border-border pt-3">
              <NavLink
                to="/settings"
                onClick={() => setMobileMenuOpen(false)}
                className={({ isActive }) =>
                  cn(
                    "flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm transition-colors",
                    isActive
                      ? "bg-primary-container text-on-primary-container font-semibold"
                      : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
                  )
                }
              >
                <Settings className="h-4 w-4" />
                <span>Settings</span>
              </NavLink>
              <p className="truncate px-2 text-xs text-on-surface-variant">{user?.email}</p>
              <Button
                variant="outline"
                size="sm"
                className="w-full"
                onClick={handleLogout}
                disabled={isLoggingOut}
              >
                <LogOut className="h-4 w-4" />
                Sign out
              </Button>
            </div>
          </aside>
        </div>
      ) : null}

      {/* Main Content Area */}
      <div className="flex min-h-screen flex-col lg:pl-64">
        {/* Stitch Fixed-Style Translucent Top Header */}
        <header className="sticky top-0 z-20 flex h-16 items-center justify-between gap-3 border-b border-border bg-surface/80 px-4 backdrop-blur-xl sm:px-6">
          <div className="flex flex-1 items-center gap-3">
            <Button
              variant="outline"
              size="icon"
              className="lg:hidden"
              onClick={() => setMobileMenuOpen(true)}
              aria-label="Open navigation menu"
            >
              <Menu className="h-4 w-4" />
            </Button>

            {/* Global Search Trigger (⌘K) */}
            <button
              type="button"
              onClick={() => setCommandOpen(true)}
              className="relative flex h-9 w-full max-w-md items-center rounded-lg bg-surface-container px-3 text-left text-xs text-on-surface-variant transition-colors hover:bg-surface-container-high cursor-pointer"
            >
              <Search className="mr-2.5 h-4 w-4 shrink-0" />
              <span className="truncate">Search tasks or jump to view...</span>
              <kbd className="ml-auto hidden items-center gap-0.5 rounded border border-outline-variant/30 bg-surface-container-high px-1.5 py-0.5 font-mono text-[11px] text-on-surface-variant sm:inline-flex">
                ⌘K
              </kbd>
            </button>
          </div>

          <div className="flex items-center gap-2.5">
            <button
              type="button"
              onClick={() => setCreateOpen(true)}
              className="inline-flex h-9 items-center gap-1.5 rounded-lg bg-primary-container px-3 text-xs font-semibold text-on-primary-container shadow-sm transition-all hover:bg-primary hover:text-primary-foreground cursor-pointer"
            >
              <Plus className="h-4 w-4" />
              <span className="hidden sm:inline">New Task</span>
            </button>
            <div className="hidden h-5 w-px bg-outline-variant/40 sm:block" />
            <ThemeToggle />
          </div>
        </header>

        <main className="mx-auto w-full max-w-[1440px] flex-1 p-4 sm:p-6 lg:p-8">
          {children ?? <Outlet />}
        </main>
      </div>

      {/* Global Command Palette, Drawer & New Task Modal */}
      <CommandPalette
        open={commandOpen}
        onOpenChange={setCommandOpen}
        onCreateTask={() => setCreateOpen(true)}
        onSelectTask={(task) => setDrawerTask(task)}
      />
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
